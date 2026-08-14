package com.hr.evaluation.kafka;

import com.hr.evaluation.entity.Evaluation;
import com.hr.evaluation.entity.EvaluationCampaign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationEventPublisherCampagneOuverteTest {

    @Mock
    private ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void publieSurRhNotificationsPasAlerte() {
        when(kafkaTemplateProvider.getIfAvailable()).thenReturn(kafkaTemplate);
        EvaluationEventPublisher publisher = new EvaluationEventPublisher(kafkaTemplateProvider);

        UUID collab = UUID.randomUUID();
        UUID campagneId = UUID.randomUUID();
        UUID evalId = UUID.randomUUID();

        EvaluationCampaign campaign = new EvaluationCampaign();
        campaign.setId(campagneId);
        campaign.setNom("Annuelle 2026");

        Evaluation evaluation = new Evaluation();
        evaluation.setId(evalId);
        evaluation.setCollaborateurIdentifiant(collab);
        evaluation.setCampaign(campaign);

        publisher.publierEvaluationCampagneOuverte(evaluation, "Annuelle 2026");

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq(RhEvaluationTopics.RH_NOTIFICATIONS), eq(collab.toString()), payload.capture());

        NotificationMessage msg = (NotificationMessage) payload.getValue();
        assertThat(msg.type()).isEqualTo("WEBSOCKET");
        assertThat(msg.recipient()).isEqualTo(collab.toString());
        assertThat(msg.subject()).isEqualTo("Votre évaluation est ouverte");
        assertThat(msg.content()).startsWith("EVALUATION_CAMPAGNE_OUVERTE|campagne=" + campagneId);
        assertThat(msg.content()).contains("|evaluation=" + evalId);
    }
}
