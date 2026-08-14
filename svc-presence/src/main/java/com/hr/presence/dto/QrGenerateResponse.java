package com.hr.presence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record QrGenerateResponse(
		@JsonProperty("site_id") UUID siteId,
		@JsonProperty("qr_version") int qrVersion,
		@JsonProperty("jti") UUID jti,
		@JsonProperty("valid_from") Instant validFrom,
		@JsonProperty("valid_until") Instant validUntil,
		@JsonProperty("qr_token") String qrToken,
		@JsonProperty("download_path") String downloadPath
) {
}
