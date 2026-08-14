package com.hr.presence.service;

import com.hr.presence.config.PresenceProperties;
import com.hr.presence.domain.QrCredentialStatut;
import com.hr.presence.dto.QrGenerateResponse;
import com.hr.presence.entity.PresenceQrCredential;
import com.hr.presence.entity.PresenceSite;
import com.hr.presence.repository.PresenceQrCredentialRepository;
import com.hr.presence.security.PresenceAccessService;
import com.hr.presence.web.PresenceMetierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PresenceQrService {

	private static final Logger log = LoggerFactory.getLogger(PresenceQrService.class);

	private final PresenceSiteService siteService;
	private final PresenceQrCredentialRepository qrRepository;
	private final QrTokenService qrTokenService;
	private final QrPngService qrPngService;
	private final PresenceProperties properties;
	private final PresenceAccessService accessService;

	public PresenceQrService(
			PresenceSiteService siteService,
			PresenceQrCredentialRepository qrRepository,
			QrTokenService qrTokenService,
			QrPngService qrPngService,
			PresenceProperties properties,
			PresenceAccessService accessService) {
		this.siteService = siteService;
		this.qrRepository = qrRepository;
		this.qrTokenService = qrTokenService;
		this.qrPngService = qrPngService;
		this.properties = properties;
		this.accessService = accessService;
	}

	@Transactional
	public QrGenerateResponse generate(UUID siteId) {
		PresenceSite site = siteService.requireSite(siteId);
		if (!site.isActif()) {
			throw PresenceMetierException.conflict("SITE_INACTIF", "Impossible de générer un QR pour un site inactif");
		}

		revokeActifImmediate(siteId);

		Instant now = Instant.now();
		int nextVersion = qrRepository.findMaxVersionBySiteId(siteId) + 1;
		UUID jti = UUID.randomUUID();
		Instant validUntil = now.plus(properties.getQr().getValidityDays(), ChronoUnit.DAYS);

		PresenceQrCredential credential = new PresenceQrCredential();
		credential.setSite(site);
		credential.setQrVersion(nextVersion);
		credential.setJti(jti);
		credential.setValidFrom(now);
		credential.setValidUntil(validUntil);
		credential.setStatut(QrCredentialStatut.ACTIF);
		credential.setCreatedBy(accessService.currentActorLabel());
		qrRepository.save(credential);

		String token = qrTokenService.buildToken(siteId, nextVersion, jti, now, validUntil);
		log.info("QR généré siteId={} version={} validUntil={}", siteId, nextVersion, validUntil);

		return new QrGenerateResponse(
				siteId,
				nextVersion,
				jti,
				now,
				validUntil,
				token,
				"/api/rh/v1/admin/presence/sites/" + siteId + "/qr/download"
		);
	}

	@Transactional(readOnly = true)
	public byte[] downloadPng(UUID siteId) {
		siteService.requireSite(siteId);
		PresenceQrCredential actif = qrRepository.findBySiteIdAndStatut(siteId, QrCredentialStatut.ACTIF)
				.orElseThrow(() -> PresenceMetierException.notFound("QR_NOT_FOUND", "Aucun QR actif pour ce site"));
		expireIfNeeded(actif);
		if (actif.getStatut() != QrCredentialStatut.ACTIF) {
			throw PresenceMetierException.notFound("QR_NOT_FOUND", "Aucun QR actif pour ce site");
		}
		String token = qrTokenService.buildToken(
				siteId,
				actif.getQrVersion(),
				actif.getJti(),
				actif.getValidFrom(),
				actif.getValidUntil());
		return qrPngService.toPng(token);
	}

	@Transactional
	public void revoke(UUID siteId) {
		siteService.requireSite(siteId);
		PresenceQrCredential actif = qrRepository.findBySiteIdAndStatut(siteId, QrCredentialStatut.ACTIF)
				.orElseThrow(() -> PresenceMetierException.conflict("QR_AUCUN_ACTIF", "Aucun QR actif à révoquer"));
		actif.setStatut(QrCredentialStatut.REVOQUE);
		qrRepository.save(actif);
		log.info("QR révoqué siteId={} version={}", siteId, actif.getQrVersion());
	}

	@Transactional
	public void expireActifsDepasses() {
		Instant now = Instant.now();
		List<PresenceQrCredential> expires = qrRepository.findActifsExpires(now);
		for (PresenceQrCredential c : expires) {
			c.setStatut(QrCredentialStatut.EXPIRE);
		}
		if (!expires.isEmpty()) {
			qrRepository.saveAll(expires);
			log.info("Credentials expirés (lazy job): {}", expires.size());
		}
	}

	private void revokeActifImmediate(UUID siteId) {
		qrRepository.findBySiteIdAndStatut(siteId, QrCredentialStatut.ACTIF).ifPresent(c -> {
			c.setStatut(QrCredentialStatut.REVOQUE);
			qrRepository.save(c);
		});
	}

	void expireIfNeeded(PresenceQrCredential credential) {
		if (credential.getStatut() == QrCredentialStatut.ACTIF
				&& credential.getValidUntil().isBefore(Instant.now())) {
			credential.setStatut(QrCredentialStatut.EXPIRE);
			qrRepository.save(credential);
		}
	}
}
