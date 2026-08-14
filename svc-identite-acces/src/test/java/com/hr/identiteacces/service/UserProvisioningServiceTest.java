package com.hr.identiteacces.service;

import com.hr.identiteacces.entity.User;
import com.hr.identiteacces.kafka.CollaborateurCompteCreeEvent;
import com.hr.identiteacces.kafka.CollaborateurCompteDemandeEvent;
import com.hr.identiteacces.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private KafkaTemplate<String, CollaborateurCompteCreeEvent> collaborateurCompteCreeKafkaTemplate;

	@Mock
	private CollaborateurWelcomeMailService collaborateurWelcomeMailService;

	@InjectMocks
	private UserProvisioningService service;

	@Test
	@DisplayName("MAJ rôles RO → COLLABORATEUR remplace les rôles (retire RO, garde USER)")
	void majRoles_roVersCollaborateur_remplaceSansUnion() {
		UUID userId = UUID.randomUUID();
		UUID collaborateurId = UUID.randomUUID();
		User user = User.builder()
				.id(userId)
				.username("M001")
				.email("ada@agua.test")
				.password("deja-encode")
				.roles(new HashSet<>(Set.of("USER", "RO")))
				.build();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		CollaborateurCompteDemandeEvent event = new CollaborateurCompteDemandeEvent(
				collaborateurId,
				"M001",
				"ada@agua.test",
				"Ada",
				"Lovelace",
				"COLLABORATEUR",
				null,
				CollaborateurCompteDemandeEvent.OPERATION_MAJ_ROLES,
				userId);

		assertThatCode(() -> service.provisionCollaborateurCompte(event)).doesNotThrowAnyException();

		assertThat(user.getRoles()).containsExactly("USER");
		verify(userRepository).save(user);
		verify(passwordEncoder, never()).encode(any());
		verify(collaborateurWelcomeMailService, never())
				.scheduleWelcomeEmail(any(), any(), any(), any(), any());
		verify(collaborateurCompteCreeKafkaTemplate, never()).send(any(), any(), any());
	}

	@Test
	@DisplayName("compte existant trouvé par matricule : remplacement idempotent des rôles")
	void majRoles_parMatricule_remplace() {
		UUID userId = UUID.randomUUID();
		User user = User.builder()
				.id(userId)
				.username("M002")
				.email("alan@agua.test")
				.password("deja-encode")
				.roles(new HashSet<>(Set.of("USER", "RH")))
				.build();
		when(userRepository.findByUsername("M002")).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		CollaborateurCompteDemandeEvent event = new CollaborateurCompteDemandeEvent(
				UUID.randomUUID(),
				"M002",
				"alan@agua.test",
				"Alan",
				"Turing",
				"RO",
				null,
				CollaborateurCompteDemandeEvent.OPERATION_MAJ_ROLES,
				null);

		service.provisionCollaborateurCompte(event);

		assertThat(user.getRoles()).containsExactlyInAnyOrder("USER", "RO");
		assertThat(user.getRoles()).doesNotContain("RH");
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("mot_de_passe_initial null sur une MAJ : pas de NPE, pas de réencodage")
	void majRoles_motDePasseNull_pasDeNpe() {
		UUID userId = UUID.randomUUID();
		User user = User.builder()
				.id(userId)
				.username("M003")
				.email("grace@agua.test")
				.password("deja-encode")
				.roles(new HashSet<>(Set.of("USER")))
				.build();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		CollaborateurCompteDemandeEvent event = new CollaborateurCompteDemandeEvent(
				UUID.randomUUID(),
				"M003",
				"grace@agua.test",
				"Grace",
				"Hopper",
				"ADMIN",
				null,
				CollaborateurCompteDemandeEvent.OPERATION_MAJ_ROLES,
				userId);

		assertThatCode(() -> service.provisionCollaborateurCompte(event)).doesNotThrowAnyException();
		assertThat(user.getPassword()).isEqualTo("deja-encode");
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	@DisplayName("compte introuvable alors que l'id est renseigné : WARN, pas d'exception")
	void majRoles_compteIntrouvable_neThrowPas() {
		UUID compteId = UUID.randomUUID();
		when(userRepository.findById(compteId)).thenReturn(Optional.empty());
		when(userRepository.findByUsername("M404")).thenReturn(Optional.empty());

		CollaborateurCompteDemandeEvent event = new CollaborateurCompteDemandeEvent(
				UUID.randomUUID(),
				"M404",
				"absent@agua.test",
				"Inconnu",
				"Agent",
				"RH",
				null,
				CollaborateurCompteDemandeEvent.OPERATION_MAJ_ROLES,
				compteId);

		assertThatCode(() -> service.provisionCollaborateurCompte(event)).doesNotThrowAnyException();
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
		verify(collaborateurWelcomeMailService, never())
				.scheduleWelcomeEmail(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("création : user déjà existant → remplace les rôles sans recréer le mot de passe")
	void creation_userExistant_remplaceRoles() {
		UUID userId = UUID.randomUUID();
		UUID collaborateurId = UUID.randomUUID();
		User user = User.builder()
				.id(userId)
				.username("M010")
				.email("exist@agua.test")
				.password("deja-encode")
				.roles(new HashSet<>(Set.of("USER")))
				.build();
		when(userRepository.findByUsername("M010")).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		CollaborateurCompteDemandeEvent event = new CollaborateurCompteDemandeEvent(
				collaborateurId,
				"M010",
				"exist@agua.test",
				"Exist",
				"Ant",
				"DIRECTION",
				"Secret123");

		service.provisionCollaborateurCompte(event);

		assertThat(user.getRoles()).containsExactlyInAnyOrder("USER", "DIRECTION");
		verify(passwordEncoder, never()).encode(any());
		verify(collaborateurWelcomeMailService, never())
				.scheduleWelcomeEmail(any(), any(), any(), any(), any());
		ArgumentCaptor<CollaborateurCompteCreeEvent> captor =
				ArgumentCaptor.forClass(CollaborateurCompteCreeEvent.class);
		verify(collaborateurCompteCreeKafkaTemplate).send(any(), any(), captor.capture());
		assertThat(captor.getValue().compteUtilisateurIdentifiant()).isEqualTo(userId);
	}
}
