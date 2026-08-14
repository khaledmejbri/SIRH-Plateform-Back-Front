package com.hr.referentiel.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NiveauSenioriteTest {

	@Test
	void confirmedAliasVersConfirme() {
		assertThat(NiveauSeniorite.parseOptional("CONFIRMED")).contains(NiveauSeniorite.CONFIRME);
		assertThat(NiveauSeniorite.parseOptional("CONFIRME")).contains(NiveauSeniorite.CONFIRME);
	}

	@Test
	void midEtExpertRejetes() {
		assertThat(NiveauSeniorite.parseOptional("MID")).isEmpty();
		assertThat(NiveauSeniorite.parseOptional("EXPERT")).isEmpty();
	}

	@Test
	void catalogueFermeQuatreValeurs() {
		assertThat(NiveauSeniorite.catalogue()).containsExactly(
				NiveauSeniorite.JUNIOR,
				NiveauSeniorite.CONFIRME,
				NiveauSeniorite.SENIOR,
				NiveauSeniorite.TEAM_LEAD);
	}
}
