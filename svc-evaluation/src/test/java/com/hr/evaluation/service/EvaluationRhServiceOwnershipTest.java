package com.hr.evaluation.service;

import com.hr.evaluation.domain.SemestreEvaluationRh;
import com.hr.evaluation.domain.TypeEvaluationRh;
import com.hr.evaluation.entity.EvaluationRh;
import com.hr.evaluation.kafka.EvaluationEventPublisher;
import com.hr.evaluation.repository.EvaluationRhRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationRhServiceOwnershipTest {

	@Mock
	private EvaluationRhRepository repository;
	@Mock
	private EvaluationScoringService scoringService;
	@Mock
	private EvaluationEventPublisher eventPublisher;
	@Mock
	private EvaluationPdfGenerator pdfGenerator;
	@Mock
	private EvaluationArchiveStorage archiveStorage;

	@InjectMocks
	private EvaluationRhService service;

	@Test
	void validerCollaborateurRefuseActeurNull() {
		UUID id = UUID.randomUUID();
		EvaluationRh evaluation = baseEvaluation(id);
		when(repository.findById(id)).thenReturn(Optional.of(evaluation));

		assertThatThrownBy(() -> service.validerCollaborateur(id, null))
				.isInstanceOf(SecurityException.class);
	}

	@Test
	void validerCollaborateurRefuseAutreActeur() {
		UUID id = UUID.randomUUID();
		EvaluationRh evaluation = baseEvaluation(id);
		when(repository.findById(id)).thenReturn(Optional.of(evaluation));

		assertThatThrownBy(() -> service.validerCollaborateur(id, UUID.randomUUID()))
				.isInstanceOf(SecurityException.class);
	}

	@Test
	void validerSuperieurRefuseAutreActeur() {
		UUID id = UUID.randomUUID();
		EvaluationRh evaluation = baseEvaluation(id);
		when(repository.findById(id)).thenReturn(Optional.of(evaluation));

		assertThatThrownBy(() -> service.validerSuperieur(id, UUID.randomUUID()))
				.isInstanceOf(SecurityException.class);
	}

	private EvaluationRh baseEvaluation(UUID id) {
		EvaluationRh evaluation = new EvaluationRh();
		evaluation.setId(id);
		evaluation.setType(TypeEvaluationRh.SEMESTRIELLE);
		evaluation.setCollaborateurIdentifiant(UUID.randomUUID());
		evaluation.setSuperieurIdentifiant(UUID.randomUUID());
		evaluation.setAnnee(2026);
		evaluation.setSemestre(SemestreEvaluationRh.S1);
		return evaluation;
	}
}
