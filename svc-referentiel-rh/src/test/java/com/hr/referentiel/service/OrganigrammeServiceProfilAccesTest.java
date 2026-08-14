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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganigrammeServiceProfilAccesTest {

	@Mock
	private UniteOrganisationRepository uniteRepository;

	@Mock
	private CollaborateurRepository collaborateurRepository;

	@InjectMocks
	private OrganigrammeService service;

	private UUID noeudId;
	private UUID managerId;
	private UniteOrganisation noeud;
	private Collaborateur manager;

	@BeforeEach
	void setUp() {
		noeudId = UUID.randomUUID();
		managerId = UUID.randomUUID();

		noeud = new UniteOrganisation();
		noeud.setId(noeudId);
		noeud.setCode("IT");
		noeud.setLibelle("Informatique");

		manager = new Collaborateur();
		manager.setId(managerId);
		manager.setMatricule("M001");
		manager.setPrenom("Ada");
		manager.setNom("Lovelace");
		manager.setProfilAcces("COLLABORATEUR");

		when(uniteRepository.findById(noeudId)).thenReturn(Optional.of(noeud));
		when(uniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		lenient().when(collaborateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		lenient().when(collaborateurRepository.findByUniteId(noeudId)).thenReturn(List.of(manager));
		lenient().when(uniteRepository.findByParentId(noeudId)).thenReturn(List.of());
	}

	@Test
	@DisplayName("assigner un manager avec role_workflow ne change pas profil_acces")
	void assignerManager_ignoreRoleWorkflow() {
		when(collaborateurRepository.findById(managerId)).thenReturn(Optional.of(manager));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(managerId);
		req.setRoleWorkflow("RO");

		service.assignerManager(noeudId, req);

		assertThat(manager.getProfilAcces()).isEqualTo("COLLABORATEUR");
		assertThat(noeud.getManager()).isEqualTo(manager);
	}

	@Test
	@DisplayName("role_workflow hors catalogue n'est pas une erreur 422")
	void assignerManager_roleWorkflowInvalideIgnore() {
		when(collaborateurRepository.findById(managerId)).thenReturn(Optional.of(manager));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(managerId);
		req.setRoleWorkflow("PAS_UN_ROLE");

		assertThatCode(() -> service.assignerManager(noeudId, req)).doesNotThrowAnyException();
		assertThat(manager.getProfilAcces()).isEqualTo("COLLABORATEUR");
	}

	@Test
	@DisplayName("retirer un manager ne rétrograde pas profil_acces")
	void retirerManager_neChangePasProfilAcces() {
		manager.setProfilAcces("RO");
		noeud.setManager(manager);

		service.retirerManager(noeudId);

		assertThat(manager.getProfilAcces()).isEqualTo("RO");
		assertThat(noeud.getManager()).isNull();
	}

	@Test
	@DisplayName("remplacer un manager ne rétrograde pas l'ancien profil_acces")
	void assignerManager_neDemotePasAncien() {
		Collaborateur ancien = new Collaborateur();
		ancien.setId(UUID.randomUUID());
		ancien.setMatricule("M000");
		ancien.setPrenom("Alan");
		ancien.setNom("Turing");
		ancien.setProfilAcces("RESPONSABLE");
		noeud.setManager(ancien);

		when(collaborateurRepository.findById(managerId)).thenReturn(Optional.of(manager));

		OrganigrammeAssignerManagerRequest req = new OrganigrammeAssignerManagerRequest();
		req.setCollaborateurIdentifiant(managerId);
		req.setRoleWorkflow("RESPONSABLE");

		service.assignerManager(noeudId, req);

		assertThat(ancien.getProfilAcces()).isEqualTo("RESPONSABLE");
		assertThat(manager.getProfilAcces()).isEqualTo("COLLABORATEUR");
	}
}
