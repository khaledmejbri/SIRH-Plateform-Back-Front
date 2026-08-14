package com.hr.referentiel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeCongeTest {

	@Test
	@DisplayName("les 5 valeurs du catalogue sont acceptées (casse ignorée)")
	void parse_catalogueComplet() {
		for (TypeConge type : TypeConge.values()) {
			assertThat(TypeConge.parse(type.name())).isEqualTo(type);
			assertThat(TypeConge.parse(type.name().toLowerCase())).isEqualTo(type);
		}
	}

	@Test
	@DisplayName("valeur hors catalogue → exception avec la liste des 5 valeurs")
	void parse_inconnu_exception() {
		assertThatThrownBy(() -> TypeConge.parse("RTT"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(TypeConge.MESSAGE_INVALIDE)
				.hasMessageContaining("ANNUEL")
				.hasMessageContaining("MALADIE")
				.hasMessageContaining("MATERNITE")
				.hasMessageContaining("SANS_SOLDE")
				.hasMessageContaining("AUTRE");
	}

	@Test
	@DisplayName("MALADIE et MATERNITE exigent une PJ ; AUTRE non")
	void exigePieceJointe() {
		assertThat(TypeConge.MALADIE.exigePieceJointe()).isTrue();
		assertThat(TypeConge.MATERNITE.exigePieceJointe()).isTrue();
		assertThat(TypeConge.ANNUEL.exigePieceJointe()).isFalse();
		assertThat(TypeConge.SANS_SOLDE.exigePieceJointe()).isFalse();
		assertThat(TypeConge.AUTRE.exigePieceJointe()).isFalse();
	}
}
