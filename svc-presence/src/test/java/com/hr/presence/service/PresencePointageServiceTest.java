package com.hr.presence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.presence.config.PresenceProperties;
import com.hr.presence.domain.PointageStatut;
import com.hr.presence.domain.QrCredentialStatut;
import com.hr.presence.dto.CreatePointageRequest;
import com.hr.presence.dto.PointageResponse;
import com.hr.presence.entity.PresencePointage;
import com.hr.presence.entity.PresenceQrCredential;
import com.hr.presence.entity.PresenceSite;
import com.hr.presence.repository.PresencePointageRepository;
import com.hr.presence.repository.PresenceQrCredentialRepository;
import com.hr.presence.repository.PresenceSiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresencePointageServiceTest {

	@Mock
	private PresencePointageRepository pointageRepository;
	@Mock
	private PresenceSiteRepository siteRepository;
	@Mock
	private PresenceQrCredentialRepository qrRepository;
	@Mock
	private PresenceQrService qrService;

	private QrTokenService qrTokenService;
	private PresencePointageService pointageService;

	private PresenceSite site;
	private PresenceQrCredential credential;
	private String validToken;
	private Instant now;

	@BeforeEach
	void setUp() {
		PresenceProperties props = new PresenceProperties();
		props.getQr().setHmacSecret("test-secret-hmac-presence");
		props.getGeofence().setDefaultRadiusMetres(50);
		props.getGeofence().setMaxAccuracyMetres(100);
		qrTokenService = new QrTokenService(props, new ObjectMapper());

		pointageService = new PresencePointageService(
				pointageRepository,
				siteRepository,
				qrRepository,
				qrTokenService,
				new HaversineService(),
				props,
				qrService);

		now = Instant.now();
		UUID siteId = UUID.randomUUID();
		site = new PresenceSite();
		site.setId(siteId);
		site.setCode("HQ");
		site.setLibelle("HQ");
		site.setLatitude(36.8065);
		site.setLongitude(10.1815);
		site.setRayonMetres(50);
		site.setActif(true);

		UUID jti = UUID.randomUUID();
		credential = new PresenceQrCredential();
		credential.setId(UUID.randomUUID());
		credential.setSite(site);
		credential.setQrVersion(1);
		credential.setJti(jti);
		credential.setValidFrom(now.minus(1, ChronoUnit.HOURS));
		credential.setValidUntil(now.plus(90, ChronoUnit.DAYS));
		credential.setStatut(QrCredentialStatut.ACTIF);

		validToken = qrTokenService.buildToken(siteId, 1, jti, credential.getValidFrom(), credential.getValidUntil());

		org.mockito.Mockito.lenient().when(pointageRepository.save(any(PresencePointage.class))).thenAnswer(inv -> {
			PresencePointage p = inv.getArgument(0);
			if (p.getId() == null) {
				p.setId(UUID.randomUUID());
			}
			return p;
		});
	}

	@Test
	void happyPath_valide() {
		when(qrRepository.findByJti(credential.getJti())).thenReturn(Optional.of(credential));
		when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

		CreatePointageRequest req = new CreatePointageRequest(
				validToken, "ENTREE", 36.8065, 10.1815, 10.0, now, "device-1", null);

		PointageResponse resp = pointageService.pointer("collab-1", req);

		assertThat(resp.statut()).isEqualTo(PointageStatut.VALIDE);
		assertThat(resp.siteId()).isEqualTo(site.getId());
		assertThat(resp.distanceMetres()).isNotNull().isLessThanOrEqualTo(50.0);
	}

	@Test
	void horsZone_50_1m() {
		when(qrRepository.findByJti(credential.getJti())).thenReturn(Optional.of(credential));
		when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

		double latFar = 36.8065 + (55.0 / 111_320.0);
		CreatePointageRequest req = new CreatePointageRequest(
				validToken, "ENTREE", latFar, 10.1815, 10.0, now, null, null);

		PointageResponse resp = pointageService.pointer("collab-1", req);

		assertThat(resp.statut()).isEqualTo(PointageStatut.REJETE_HORS_ZONE);
		assertThat(resp.distanceMetres()).isGreaterThan(50.0);
	}

	@Test
	void qrInvalide_signature() {
		CreatePointageRequest req = new CreatePointageRequest(
				"p1.abc.def", "ENTREE", 36.8065, 10.1815, 10.0, now, null, null);

		PointageResponse resp = pointageService.pointer("collab-1", req);

		assertThat(resp.statut()).isEqualTo(PointageStatut.REJETE_QR_INVALIDE);
		ArgumentCaptor<PresencePointage> captor = ArgumentCaptor.forClass(PresencePointage.class);
		verify(pointageRepository).save(captor.capture());
		assertThat(captor.getValue().getStatut()).isEqualTo(PointageStatut.REJETE_QR_INVALIDE);
	}

	@Test
	void qrExpire() {
		credential.setValidUntil(now.minus(1, ChronoUnit.DAYS));
		credential.setStatut(QrCredentialStatut.EXPIRE);
		String expiredToken = qrTokenService.buildToken(
				site.getId(), 1, credential.getJti(),
				now.minus(100, ChronoUnit.DAYS), credential.getValidUntil());

		when(qrRepository.findByJti(credential.getJti())).thenReturn(Optional.of(credential));

		CreatePointageRequest req = new CreatePointageRequest(
				expiredToken, "SORTIE", 36.8065, 10.1815, 5.0, now, null, null);

		PointageResponse resp = pointageService.pointer("collab-1", req);

		assertThat(resp.statut()).isEqualTo(PointageStatut.REJETE_QR_EXPIRE);
	}

	@Test
	void idempotence_rejoueMemeReponse() {
		PresencePointage existing = new PresencePointage();
		existing.setId(UUID.randomUUID());
		existing.setCollaborateurId("collab-1");
		existing.setStatut(PointageStatut.VALIDE);
		existing.setType(com.hr.presence.domain.PointageType.ENTREE);
		existing.setServerTs(now);
		existing.setSiteId(site.getId());
		existing.setLatitude(36.8065);
		existing.setLongitude(10.1815);
		existing.setIdempotencyKey("idem-1");

		when(pointageRepository.findByCollaborateurIdAndIdempotencyKey("collab-1", "idem-1"))
				.thenReturn(Optional.of(existing));

		CreatePointageRequest req = new CreatePointageRequest(
				validToken, "ENTREE", 36.8065, 10.1815, 10.0, now, null, "idem-1");

		PointageResponse resp = pointageService.pointer("collab-1", req);

		assertThat(resp.id()).isEqualTo(existing.getId());
		assertThat(resp.statut()).isEqualTo(PointageStatut.VALIDE);
	}
}
