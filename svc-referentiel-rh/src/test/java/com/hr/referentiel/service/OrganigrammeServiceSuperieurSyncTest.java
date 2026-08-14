package com.hr.referentiel.service;

import com.hr.referentiel.dto.OrganigrammeAssignerManagerRequest;
import com.hr.referentiel.entity.Collaborateur;
import com.hr.referentiel.entity.UniteOrganisation;
import com.hr.referentiel.repository.CollaborateurRepository;
import com.hr.referentiel.repository.UniteOrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganigrammeServiceSuperieurSyncTest {

	@Mock
	private UniteOrganisationRepository uniteRepository;

	@Mock
	private CollaborateurRepository collaborateurRepository;

	@InjectMocks
	private OrganigrammeService service;

	private UUID noeudId;
	private UniteOrganisation noeud;
	private Collaborateur manager;
	private Collaborateur membreActif;
	private Collaborateur membreInactif;

	@BeforeEach
	void setUp() {
		noeudId = UUID.randomUUID();

		noeud = new UniteOrganisation();
		noeud.setId(noeudId);
		noeud.setCode("IT");
		noeud.setLibelle("Informatique");

		manager = collab(UUID.randomUUID(), "M-MGR", "Ada", "Lovelace", "ACTIF");
		manager.setUnite(noeud);

		membreActif = collab(UUID.randomUUID(), "M-EMP", "Bob", "Martin", "ACTIF");
		membreActif.setUnite(noeud);
		membreActif.setSuperieur(null);

		membreInactif = collab(UUID.randomUUID(), "M-OLD", "Old", "Member", "INACTIF");
		membreInactif.setUnite(noeud);
		membreInactif.setSuperieur(manager);

		when(uniteRepository.findById(noeudId)).thenReturn(Optional.of(noeud));
		when(uniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(collaborateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(uniteRepository.findByParentId(noeudId)).thenReturn(List.of());
	}

	@Test
	@DisplayName("H3-T3 : assigner manager met à jour superieur des membres ACTIFS")
	void assignerManager_syncMembresActifs() {
		when(collaborateurRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
		when(collaborateurRepository.findByUniteId(noeudId))
				.thenReturn(List.of(manager, membreActif, membreInactif));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(manager.getId());

		service.assignerManager(noeudId, req);

		assertThat(membreActif.getSuperieur()).isEqualTo(manager);
		assertThat(manager.getSuperieur()).isNull();
		// Must = ACTIFS seulement : inactif non réécrit
		assertThat(membreInactif.getSuperieur()).isEqualTo(manager);
	}

	@Test
	@DisplayName("H3-R02 : manager n'a jamais soi-même comme supérieur (parent si présent)")
	void assignerManager_managerPasSoiMeme() {
		UniteOrganisation parent = new UniteOrganisation();
		parent.setId(UUID.randomUUID());
		Collaborateur managerParent = collab(UUID.randomUUID(), "M-PAR", "Parent", "Boss", "ACTIF");
		parent.setManager(managerParent);
		noeud.setParent(parent);

		when(collaborateurRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
		when(collaborateurRepository.findByUniteId(noeudId)).thenReturn(List.of(manager, membreActif));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(manager.getId());

		service.assignerManager(noeudId, req);

		assertThat(manager.getSuperieur()).isEqualTo(managerParent);
		assertThat(membreActif.getSuperieur()).isEqualTo(manager);
	}

	@Test
	@DisplayName("H3-T4 : retirer manager nettoie superieur des membres ACTIFS")
	void retirerManager_nettoieMembresActifs() {
		noeud.setManager(manager);
		membreActif.setSuperieur(manager);
		manager.setSuperieur(null);

		when(collaborateurRepository.findByUniteId(noeudId))
				.thenReturn(new ArrayList<>(List.of(manager, membreActif, membreInactif)));

		service.retirerManager(noeudId);

		assertThat(noeud.getManager()).isNull();
		assertThat(membreActif.getSuperieur()).isNull();
		assertThat(manager.getSuperieur()).isNull();
		// inactif non touché (Must = ACTIFS)
		assertThat(membreInactif.getSuperieur()).isEqualTo(manager);
	}

	@Test
	@DisplayName("H3 idempotence : second assignation n'écrit pas si inchangé")
	void assignerManager_idempotent() {
		membreActif.setSuperieur(manager);
		manager.setSuperieur(null);
		noeud.setManager(manager);

		when(collaborateurRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
		when(collaborateurRepository.findByUniteId(noeudId)).thenReturn(List.of(manager, membreActif));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(manager.getId());

		service.assignerManager(noeudId, req);

		assertThat(membreActif.getSuperieur()).isEqualTo(manager);
		// manager + éventuels no-op : pas de save supplémentaire pour membre déjà aligné
		verify(collaborateurRepository, times(1)).save(manager);
		verify(collaborateurRepository, never()).save(membreActif);
	}

	private static Collaborateur collab(UUID id, String matricule, String prenom, String nom, String statut) {
		Collaborateur c = new Collaborateur();
		c.setId(id);
		c.setMatricule(matricule);
		c.setPrenom(prenom);
		c.setNom(nom);
		c.setStatut(statut);
		c.setProfilAcces("COLLABORATEUR");
		return c;
	}
}
