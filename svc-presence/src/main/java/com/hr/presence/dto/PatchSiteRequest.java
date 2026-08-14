package com.hr.presence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record PatchSiteRequest(
		@Size(max = 255)
		@JsonProperty("libelle") String libelle,

		@JsonProperty("actif") Boolean actif
) {
}
