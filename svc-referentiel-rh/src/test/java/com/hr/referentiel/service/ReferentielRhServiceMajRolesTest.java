package com.hr.referentiel.service;

import com.hr.referentiel.dto.CollaborateurMiseAJourRequest;
import com.hr.referentiel.entity.Collaborateur;
import com.hr.referentiel.entity.UniteOrganisation;
import com.hr.referentiel.kafka.CollaborateurCompteDemandeEvent;
import com.hr.referentiel.kafka.CollaborateurCompteDemandePublisher;
import com.hr.referentiel.repository.CollaborateurRepository;
import com.hr.referentiel.repository.UniteOrganisationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentielRhServiceMajRolesTest {

	@Mock
	private UniteOrganisationRepository uniteRepository;

	@Mock
	private CollaborateurRepository collaborateurRepository;

	@Mock
	private CollaborateurConnecteService collaborateurConnecteService;

	@Mock
	private FamilleMetierService familleMetierService;

	@Mock
	private ObjectProvider<CollaborateurCompteDemandePublisher> publisherProvider;

	@Mock
	private CollaborateurCompteDemandePublisher publisher;

	private ReferentielRhService service;

	private UUID collaborateurId;
	private Collaborateur collaborateur;

	@BeforeEach
	void setUp() {
		service = new ReferentielRhService(
				uniteRepository, collaborateurRepository, collaborateurConnecteService,
				familleMetierService, publisherProvider);

		collaborateurId = UUID.randomUUID();
		UniteOrganisation unite = new UniteOrganisation();
		unite.setId(UUID.randomUUID());
		unite.setCode("IT");
		unite.setLibelle("Informatique");

		collaborateur = new Collaborateur();
		collaborateur.setId(collaborateurId);
		collaborateur.setMatricule("M001");
		collaborateur.setPrenom("Ada");
		collaborateur.setNom("Lovelace");
		collaborateur.setCourrielProfessionnel("ada@agua.test");
		collaborateur.setStatut("ACTIF");
		collaborateur.setProfilAcces("RO");
		collaborateur.setUnite(unite);

		when(collaborateurRepository.findById(collaborateurId)).thenReturn(Optional.of(collaborateur));
		when(collaborateurRepository.save(any(Collaborateur.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@AfterEach
	void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	@DisplayName("fiche sans compte : MAJ profil_acces 200, no-op identité, pas d'erreur")
	void mettreAJour_sansCompte_neThrowPas() {
		collaborateur.setCompteUtilisateurId(null);

		CollaborateurMiseAJourRequest req = new CollaborateurMiseAJourRequest();
		req.setProfilAcces("COLLABORATEUR");

		assertThatCode(() -> service.mettreAJourCollaborateur(collaborateurId, req))
				.doesNotThrowAnyException();

		assertThat(collaborateur.getProfilAcces()).isEqualTo("COLLABORATEUR");
		verify(publisherProvider, never()).getIfAvailable();
	}

	@Test
	@DisplayName("même profil qu'avant : pas d'événement Kafka")
	void mettreAJour_memeProfil_pasDeKafka() {
		UUID compteId = UUID.randomUUID();
		collaborateur.setCompteUtilisateurId(compteId);
		collaborateur.setProfilAcces("RO");

		CollaborateurMiseAJourRequest req = new CollaborateurMiseAJourRequest();
		req.setProfilAcces("RO");

		service.mettreAJourCollaborateur(collaborateurId, req);

		verify(publisherProvider, never()).getIfAvailable();
	}

	@Test
	@DisplayName("profil changé et compte lié : publie MAJ_ROLES après commit, mot de passe null")
	void mettreAJour_profilChangeAvecCompte_publieMajRoles() {
		UUID compteId = UUID.randomUUID();
		collaborateur.setCompteUtilisateurId(compteId);
		when(publisherProvider.getIfAvailable()).thenReturn(publisher);

		TransactionSynchronizationManager.initSynchronization();
		try {
			CollaborateurMiseAJourRequest req = new CollaborateurMiseAJourRequest();
			req.setProfilAcces("COLLABORATEUR");

			service.mettreAJourCollaborateur(collaborateurId, req);

			TransactionSynchronizationManager.getSynchronizations()
					.forEach(TransactionSynchronization::afterCommit);

			ArgumentCaptor<CollaborateurCompteDemandeEvent> captor =
					ArgumentCaptor.forClass(CollaborateurCompteDemandeEvent.class);
			verify(publisher).publishAsyncAfterCommit(eq(collaborateurId.toString()), captor.capture());

			CollaborateurCompteDemandeEvent event = captor.getValue();
			assertThat(event.operation()).isEqualTo(CollaborateurCompteDemandeEvent.OPERATION_MAJ_ROLES);
			assertThat(event.profilAcces()).isEqualTo("COLLABORATEUR");
			assertThat(event.motDePasseInitial()).isNull();
			assertThat(event.compteUtilisateurId()).isEqualTo(compteId);
			assertThat(event.matricule()).isEqualTo("M001");
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}
}
