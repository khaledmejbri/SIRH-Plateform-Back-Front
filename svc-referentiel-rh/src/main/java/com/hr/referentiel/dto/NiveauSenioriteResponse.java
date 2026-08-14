package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NiveauSenioriteResponse(
		@JsonProperty("code") String code,
		@JsonProperty("libelle") String libelle
) {
}
