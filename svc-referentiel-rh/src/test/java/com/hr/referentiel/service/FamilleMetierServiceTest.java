package com.hr.referentiel.service;

import com.hr.referentiel.dto.FamilleMetierCreationRequest;
import com.hr.referentiel.dto.FamilleMetierMiseAJourRequest;
import com.hr.referentiel.entity.FamilleMetier;
import com.hr.referentiel.repository.FamilleMetierRepository;
import com.hr.referentiel.web.ReferentielMetierException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilleMetierServiceTest {

	@Mock
	private FamilleMetierRepository repository;

	private FamilleMetierService service;

	@BeforeEach
	void setUp() {
		service = new FamilleMetierService(repository);
	}

	@Test
	void creerNormaliseCodeUpper() {
		when(repository.existsById("DEV_LOGICIEL")).thenReturn(false);
		when(repository.existsByCodeIgnoreCase("DEV_LOGICIEL")).thenReturn(false);
		when(repository.save(any(FamilleMetier.class))).thenAnswer(inv -> inv.getArgument(0));

		FamilleMetierCreationRequest req = new FamilleMetierCreationRequest();
		req.setCode("dev_logiciel");
		req.setLibelle("Développement logiciel / SI");

		var resp = service.creer(req);

		assertThat(resp.code()).isEqualTo("DEV_LOGICIEL");
		ArgumentCaptor<FamilleMetier> captor = ArgumentCaptor.forClass(FamilleMetier.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().isSysteme()).isFalse();
	}

	@Test
	void exigerActiveInconnue422() {
		when(repository.findById("INCONNU")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.exigerActive("INCONNU"))
				.isInstanceOf(ReferentielMetierException.class)
				.satisfies(ex -> {
					ReferentielMetierException m = (ReferentielMetierException) ex;
					assertThat(m.getCode()).isEqualTo("FAMILLE_METIER_INCONNUE");
					assertThat(m.getHttpStatus()).isEqualTo(422);
				});
	}

	@Test
	void mettreAJourDesactiveSeed() {
		FamilleMetier existing = new FamilleMetier();
		existing.setCode("DEV_LOGICIEL");
		existing.setLibelle("Dev");
		existing.setActif(true);
		existing.setSysteme(true);
		when(repository.findById("DEV_LOGICIEL")).thenReturn(Optional.of(existing));
		when(repository.save(any(FamilleMetier.class))).thenAnswer(inv -> inv.getArgument(0));

		FamilleMetierMiseAJourRequest req = new FamilleMetierMiseAJourRequest();
		req.setLibelle("Développement logiciel / SI");
		req.setActif(false);

		var resp = service.mettreAJour("DEV_LOGICIEL", req);
		assertThat(resp.actif()).isFalse();
		assertThat(resp.systeme()).isTrue();
	}
}
