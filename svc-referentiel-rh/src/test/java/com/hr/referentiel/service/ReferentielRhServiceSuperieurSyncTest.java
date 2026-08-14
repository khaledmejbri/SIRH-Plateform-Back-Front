package com.hr.referentiel.service;

import com.hr.referentiel.dto.CollaborateurCreationRequest;
import com.hr.referentiel.dto.CollaborateurMiseAJourRequest;
import com.hr.referentiel.dto.CollaborateurResponse;
import com.hr.referentiel.entity.Collaborateur;
import com.hr.referentiel.entity.UniteOrganisation;
import com.hr.referentiel.kafka.CollaborateurCompteDemandePublisher;
import com.hr.referentiel.repository.CollaborateurRepository;
import com.hr.referentiel.repository.UniteOrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentielRhServiceSuperieurSyncTest {

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

	private ReferentielRhService service;

	private UniteOrganisation unite;
	private Collaborateur manager;

	@BeforeEach
	void setUp() {
		service = new ReferentielRhService(
				uniteRepository, collaborateurRepository, collaborateurConnecteService,
				familleMetierService, publisherProvider);

		unite = new UniteOrganisation();
		unite.setId(UUID.randomUUID());
		unite.setCode("IT");
		unite.setLibelle("Informatique");

		manager = new Collaborateur();
		manager.setId(UUID.randomUUID());
		manager.setMatricule("M-MGR");
		manager.setPrenom("Ada");
		manager.setNom("Lovelace");
		manager.setStatut("ACTIF");
		unite.setManager(manager);

		when(collaborateurRepository.save(any(Collaborateur.class))).thenAnswer(inv -> {
			Collaborateur c = inv.getArgument(0);
			if (c.getId() == null) {
				c.setId(UUID.randomUUID());
			}
			return c;
		});
	}

	@Test
	@DisplayName("H3-T1 : création ignore superieur_identifiant divergent → manager du nœud")
	void creer_deriveSuperieurDuManager() {
		when(uniteRepository.findById(unite.getId())).thenReturn(Optional.of(unite));
		when(collaborateurRepository.existsByMatriculeIgnoreCase("M-NEW")).thenReturn(false);

		UUID fauxSuperieur = UUID.randomUUID();
		CollaborateurCreationRequest req = new CollaborateurCreationRequest();
		req.setMatricule("M-NEW");
		req.setPrenom("Bob");
		req.setNom("Martin");
		req.setCourrielProfessionnel("bob@agua.test");
		req.setStatut("ACTIF");
		req.setProfilAcces("COLLABORATEUR");
		req.setUniteIdentifiant(unite.getId());
		req.setSuperieurIdentifiant(fauxSuperieur);
		req.setCompteUtilisateurId(UUID.randomUUID());

		CollaborateurResponse response = service.creerCollaborateur(req);

		assertThat(response.getSuperieurIdentifiant()).isEqualTo(manager.getId());
	}

	@Test
	@DisplayName("H3-T2 : changement d'unité recalcule superieur")
	void maj_changementUnite_resyncSuperieur() {
		UniteOrganisation autre = new UniteOrganisation();
		autre.setId(UUID.randomUUID());
		autre.setCode("RH");
		Collaborateur managerRh = new Collaborateur();
		managerRh.setId(UUID.randomUUID());
		managerRh.setStatut("ACTIF");
		autre.setManager(managerRh);

		UUID collabId = UUID.randomUUID();
		Collaborateur collab = new Collaborateur();
		collab.setId(collabId);
		collab.setMatricule("M-EMP");
		collab.setPrenom("Bob");
		collab.setNom("Martin");
		collab.setStatut("ACTIF");
		collab.setProfilAcces("COLLABORATEUR");
		collab.setUnite(unite);
		collab.setSuperieur(manager);

		when(collaborateurRepository.findById(collabId)).thenReturn(Optional.of(collab));
		when(uniteRepository.findById(autre.getId())).thenReturn(Optional.of(autre));

		CollaborateurMiseAJourRequest req = new CollaborateurMiseAJourRequest();
		req.setUniteIdentifiant(autre.getId());
		req.setSuperieurIdentifiant(manager.getId()); // divergent, ignoré

		Optional<CollaborateurResponse> response = service.mettreAJourCollaborateur(collabId, req);

		assertThat(response).isPresent();
		assertThat(response.get().getSuperieurIdentifiant()).isEqualTo(managerRh.getId());
		assertThat(collab.getSuperieur()).isEqualTo(managerRh);
	}

	@Test
	@DisplayName("H3-R02 : si collab est manager du nœud, superieur = manager parent")
	void creer_managerDuNoeud_superieurParent() {
		UniteOrganisation parent = new UniteOrganisation();
		parent.setId(UUID.randomUUID());
		Collaborateur managerParent = new Collaborateur();
		managerParent.setId(UUID.randomUUID());
		managerParent.setStatut("ACTIF");
		parent.setManager(managerParent);

		UniteOrganisation noeud = new UniteOrganisation();
		noeud.setId(UUID.randomUUID());
		noeud.setCode("SVC");
		noeud.setParent(parent);
		noeud.setManager(manager); // manager déjà sur le nœud ; on crée… non : on maj un collab qui EST le manager

		manager.setUnite(noeud);
		when(collaborateurRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

		CollaborateurMiseAJourRequest req = new CollaborateurMiseAJourRequest();
		req.setPrenom("Ada");

		Optional<CollaborateurResponse> response = service.mettreAJourCollaborateur(manager.getId(), req);

		assertThat(response).isPresent();
		assertThat(response.get().getSuperieurIdentifiant()).isEqualTo(managerParent.getId());
	}
}
