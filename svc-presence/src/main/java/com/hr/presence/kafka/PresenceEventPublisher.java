package com.hr.presence.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PresenceEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(PresenceEventPublisher.class);

	private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

	public PresenceEventPublisher(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {
		this.kafkaTemplateProvider = kafkaTemplateProvider;
	}

	/**
	 * P3 Must — alerte J-30 à tous les comptes RH (destinataire spécial {@code RH}).
	 */
	public void publierQrExpireJ30(
			UUID eventId,
			UUID siteId,
			String libelle,
			Instant validUntil,
			long joursRestants,
			int qrVersion) {
		String content = "PRESENCE_QR_EXPIRE_J30"
				+ "|eventId=" + eventId
				+ "|siteId=" + siteId
				+ "|libelle=" + libelle
				+ "|validUntil=" + validUntil
				+ "|joursRestants=" + joursRestants
				+ "|qrVersion=" + qrVersion
				+ "|Le QR du site « " + libelle + " » expire dans " + joursRestants + " jour(s).";
		NotificationMessage msg = new NotificationMessage(
				"WEBSOCKET",
				"RH",
				"QR de pointage bientôt expiré",
				content);
		try {
			publier(RhPresenceTopics.RH_NOTIFICATIONS, siteId + ":" + qrVersion, msg);
		} catch (Exception ex) {
			log.warn("Échec publication PRESENCE_QR_EXPIRE_J30 siteId={}: {}", siteId, ex.getMessage());
		}
	}

	private void publier(String topic, String key, Object payload) {
		KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
		if (kafkaTemplate != null) {
			kafkaTemplate.send(topic, key, payload);
		} else {
			log.debug("KafkaTemplate indisponible — notif {} non publiée (stub)", topic);
		}
	}
}
