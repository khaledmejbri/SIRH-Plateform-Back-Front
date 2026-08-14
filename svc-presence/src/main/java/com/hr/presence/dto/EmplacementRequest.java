package com.hr.presence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record EmplacementRequest(
		@NotNull
		@DecimalMin(value = "-90.0")
		@DecimalMax(value = "90.0")
		@JsonProperty("latitude") Double latitude,

		@NotNull
		@DecimalMin(value = "-180.0")
		@DecimalMax(value = "180.0")
		@JsonProperty("longitude") Double longitude
) {
}
