package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record FamilleMetierResponse(
		@JsonProperty("code") String code,
		@JsonProperty("libelle") String libelle,
		@JsonProperty("actif") boolean actif,
		@JsonProperty("systeme") boolean systeme,
		@JsonProperty("cree_le") Instant creeLe,
		@JsonProperty("modifie_le") Instant modifieLe
) {
}
