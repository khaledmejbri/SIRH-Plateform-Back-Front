package com.hr.identiteacces.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationRoleMatrixTest {

	@Test
	@DisplayName("null, blank et COLLABORATEUR → {USER}")
	void rolesPourProfil_collaborateurOuVide() {
		assertThat(ApplicationRoleMatrix.rolesPourProfil(null)).containsExactly("USER");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("")).containsExactly("USER");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("  ")).containsExactly("USER");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("COLLABORATEUR")).containsExactly("USER");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("collaborateur")).containsExactly("USER");
	}

	@Test
	@DisplayName("RO pose RO, pas RESPONSABLE")
	void rolesPourProfil_roDistinctDeResponsable() {
		Set<String> rolesRo = ApplicationRoleMatrix.rolesPourProfil("RO");
		assertThat(rolesRo).containsExactlyInAnyOrder("USER", "RO");
		assertThat(rolesRo).doesNotContain("RESPONSABLE");

		Set<String> rolesResp = ApplicationRoleMatrix.rolesPourProfil("RESPONSABLE");
		assertThat(rolesResp).containsExactlyInAnyOrder("USER", "RESPONSABLE");
		assertThat(rolesResp).doesNotContain("RO");
	}

	@Test
	@DisplayName("RH, DIRECTION, ADMIN → {USER, rôle} sans union")
	void rolesPourProfil_backOffice() {
		assertThat(ApplicationRoleMatrix.rolesPourProfil("RH"))
				.containsExactlyInAnyOrder("USER", "RH");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("DIRECTION"))
				.containsExactlyInAnyOrder("USER", "DIRECTION");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("ADMIN"))
				.containsExactlyInAnyOrder("USER", "ADMIN");
	}

	@Test
	@DisplayName("profil inconnu → {USER} (équivalent COLLABORATEUR)")
	void rolesPourProfil_inconnu() {
		assertThat(ApplicationRoleMatrix.rolesPourProfil("INCONNU")).containsExactly("USER");
		assertThat(ApplicationRoleMatrix.rolesPourProfil("SUPERADMIN")).containsExactly("USER");
	}

	@Test
	@DisplayName("USER reste implicite pour RO, RESPONSABLE, RH, DIRECTION, ADMIN")
	void expandWithImplicitUser_conserveUserImplicite() {
		assertThat(ApplicationRoleMatrix.expandWithImplicitUser(Set.of("RO"))).contains("USER", "RO");
		assertThat(ApplicationRoleMatrix.expandWithImplicitUser(Set.of("RESPONSABLE"))).contains("USER", "RESPONSABLE");
		assertThat(ApplicationRoleMatrix.expandWithImplicitUser(Set.of("RH"))).contains("USER", "RH");
		assertThat(ApplicationRoleMatrix.expandWithImplicitUser(Set.of("DIRECTION"))).contains("USER", "DIRECTION");
		assertThat(ApplicationRoleMatrix.expandWithImplicitUser(Set.of("ADMIN"))).contains("USER", "ADMIN");
	}
}
