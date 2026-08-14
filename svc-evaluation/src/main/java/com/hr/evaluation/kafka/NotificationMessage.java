package com.hr.evaluation.kafka;

public record NotificationMessage(
		String type,
		String recipient,
		String subject,
		String content
) {
}
