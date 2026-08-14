package com.hr.presence.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aligné sur {@code svc-notification} (recipient {@code RH} = broadcast rôle RH).
 */
public record NotificationMessage(
		@JsonProperty("type") String type,
		@JsonProperty("recipient") String recipient,
		@JsonProperty("subject") String subject,
		@JsonProperty("content") String content
) {
}
