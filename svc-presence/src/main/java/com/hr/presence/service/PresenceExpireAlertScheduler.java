package com.hr.presence.service;

import com.hr.presence.config.PresenceProperties;
import com.hr.presence.entity.PresenceQrCredential;
import com.hr.presence.kafka.PresenceEventPublisher;
import com.hr.presence.repository.PresenceQrCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Job quotidien : expire les QR dépassés + alerte J-30 (1 notif / site / version).
 */
@Component
public class PresenceExpireAlertScheduler {

	private static final Logger log = LoggerFactory.getLogger(PresenceExpireAlertScheduler.class);

	private final PresenceQrCredentialRepository qrRepository;
	private final PresenceQrService qrService;
	private final PresenceEventPublisher eventPublisher;
	private final PresenceProperties properties;

	public PresenceExpireAlertScheduler(
			PresenceQrCredentialRepository qrRepository,
			PresenceQrService qrService,
			PresenceEventPublisher eventPublisher,
			PresenceProperties properties) {
		this.qrRepository = qrRepository;
		this.qrService = qrService;
		this.eventPublisher = eventPublisher;
		this.properties = properties;
	}

	@Scheduled(cron = "${presence.alert.j30-cron:0 0 8 * * *}")
	@Transactional
	public void runDaily() {
		qrService.expireActifsDepasses();
		if (!properties.getAlert().isJ30Enabled()) {
			return;
		}
		publierAlertesJ30();
	}

	void publierAlertesJ30() {
		Instant now = Instant.now();
		Instant from = now.plus(29, ChronoUnit.DAYS);
		Instant to = now.plus(31, ChronoUnit.DAYS);
		List<PresenceQrCredential> candidats = qrRepository.findActifsPourAlerteJ30(from, to);
		for (PresenceQrCredential c : candidats) {
			long jours = ChronoUnit.DAYS.between(now, c.getValidUntil());
			UUID eventId = UUID.nameUUIDFromBytes(
					("PRESENCE_QR_EXPIRE_J30:" + c.getSite().getId() + ":" + c.getQrVersion())
							.getBytes());
			eventPublisher.publierQrExpireJ30(
					eventId,
					c.getSite().getId(),
					c.getSite().getLibelle(),
					c.getValidUntil(),
					jours,
					c.getQrVersion());
			c.setAlerteJ30Envoyee(true);
			qrRepository.save(c);
			log.info("Alerte J-30 envoyée siteId={} qrVersion={}", c.getSite().getId(), c.getQrVersion());
		}
	}
}
