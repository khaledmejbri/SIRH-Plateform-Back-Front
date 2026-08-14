package com.hr.evaluation.kafka;

import com.hr.evaluation.domain.CouleurAlerteEvaluationRh;
import com.hr.evaluation.entity.Evaluation;
import com.hr.evaluation.entity.EvaluationRh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EvaluationEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(EvaluationEventPublisher.class);

	private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

	public EvaluationEventPublisher(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {
		this.kafkaTemplateProvider = kafkaTemplateProvider;
	}

	public void publierAlerteSiNecessaire(EvaluationRh evaluation) {
		if (evaluation.getCouleurAlerte() == CouleurAlerteEvaluationRh.VERT) {
			return;
		}
		String message = evaluation.getCouleurAlerte() == CouleurAlerteEvaluationRh.ROUGE
				? "Plan d'action obligatoire et escalade DG."
				: "Alerte RH: deux criteres sont sous le seuil.";
		publier(RhEvaluationTopics.EVALUATION_ALERTE, evaluation.getId().toString(),
				new EvaluationAlerteEvent(
						evaluation.getId(),
						evaluation.getCollaborateurIdentifiant(),
						evaluation.getSuperieurIdentifiant(),
						evaluation.getType(),
						evaluation.getAnnee(),
						evaluation.getCouleurAlerte(),
						message,
						Instant.now()));
	}

	public void publierFormationRecommandee(EvaluationRh evaluation, String formation) {
		publier(RhEvaluationTopics.FORMATION_RECOMMANDEE, evaluation.getId().toString(),
				new FormationRecommandeeEvent(
						evaluation.getId(),
						evaluation.getCollaborateurIdentifiant(),
						evaluation.getSuperieurIdentifiant(),
						evaluation.getAnnee(),
						formation,
						"EVALUATION_ANNUELLE",
						Instant.now()));
	}

	/**
	 * E5 — notif in-app ouverture campagne (topic {@code rh.notifications}, pas alerte couleur).
	 */
	public void publierEvaluationCampagneOuverte(Evaluation evaluation, String campagneNom) {
		UUID collabId = evaluation.getCollaborateurIdentifiant();
		UUID campagneId = evaluation.getCampaign() != null ? evaluation.getCampaign().getId() : null;
		String nom = campagneNom != null ? campagneNom : "campagne";
		String content = "EVALUATION_CAMPAGNE_OUVERTE|campagne=" + campagneId
				+ "|evaluation=" + evaluation.getId()
				+ "|La campagne « " + nom + " » est active. Merci de compléter votre auto-évaluation (générale puis technique).";
		NotificationMessage msg = new NotificationMessage(
				"WEBSOCKET",
				collabId.toString(),
				"Votre évaluation est ouverte",
				content);
		try {
			publier(RhEvaluationTopics.RH_NOTIFICATIONS, collabId.toString(), msg);
		} catch (Exception ex) {
			log.warn("Échec publication notif EVALUATION_CAMPAGNE_OUVERTE pour éval {}: {}",
					evaluation.getId(), ex.getMessage());
		}
	}

	private void publier(String topic, String key, Object payload) {
		KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
		if (kafkaTemplate != null) {
			kafkaTemplate.send(topic, key, payload);
		}
	}
}
