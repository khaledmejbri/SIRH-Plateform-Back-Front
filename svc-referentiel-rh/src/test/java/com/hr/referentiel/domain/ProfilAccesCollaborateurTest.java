package com.hr.referentiel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilAccesCollaborateurTest {

	@Test
	@DisplayName("null / blank à la création → COLLABORATEUR")
	void parse_vide_collaborateur() {
		assertThat(ProfilAccesCollaborateur.parse(null)).isEqualTo(ProfilAccesCollaborateur.COLLABORATEUR);
		assertThat(ProfilAccesCollaborateur.parse("")).isEqualTo(ProfilAccesCollaborateur.COLLABORATEUR);
		assertThat(ProfilAccesCollaborateur.parse("  ")).isEqualTo(ProfilAccesCollaborateur.COLLABORATEUR);
	}

	@Test
	@DisplayName("RO n'est plus mappé vers RESPONSABLE")
	void parse_roDistinctDeResponsable() {
		assertThat(ProfilAccesCollaborateur.parse("RO")).isEqualTo(ProfilAccesCollaborateur.RO);
		assertThat(ProfilAccesCollaborateur.parse("ro")).isEqualTo(ProfilAccesCollaborateur.RO);
		assertThat(ProfilAccesCollaborateur.parse("RESPONSABLE")).isEqualTo(ProfilAccesCollaborateur.RESPONSABLE);
	}

	@Test
	@DisplayName("les 6 valeurs du catalogue sont acceptées")
	void parse_catalogueComplet() {
		for (ProfilAccesCollaborateur profil : ProfilAccesCollaborateur.values()) {
			assertThat(ProfilAccesCollaborateur.parse(profil.name())).isEqualTo(profil);
		}
	}

	@Test
	@DisplayName("valeur hors catalogue → exception avec la liste des 6 valeurs")
	void parse_inconnu_exception() {
		assertThatThrownBy(() -> ProfilAccesCollaborateur.parse("SUPERADMIN"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(ProfilAccesCollaborateur.MESSAGE_INVALIDE)
				.hasMessageContaining("COLLABORATEUR")
				.hasMessageContaining("RO")
				.hasMessageContaining("RESPONSABLE")
				.hasMessageContaining("RH")
				.hasMessageContaining("DIRECTION")
				.hasMessageContaining("ADMIN");
	}
}
