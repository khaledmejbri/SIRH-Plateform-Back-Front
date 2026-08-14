package com.hr.evaluation.domain;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Catalogue fermé niveaux séniorité (aligné référentiel E2).
 */
public final class NiveauSenioriteCodes {

	public static final Set<String> CATALOGUE = Set.of("JUNIOR", "CONFIRME", "SENIOR", "TEAM_LEAD");

	private NiveauSenioriteCodes() {
	}

	public static Optional<String> normalizeOptional(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String n = raw.trim().toUpperCase(Locale.ROOT);
		return switch (n) {
			case "JUNIOR" -> Optional.of("JUNIOR");
			case "CONFIRME", "CONFIRMED" -> Optional.of("CONFIRME");
			case "SENIOR" -> Optional.of("SENIOR");
			case "TEAM_LEAD", "TEAMLEAD", "LEAD" -> Optional.of("TEAM_LEAD");
			default -> Optional.empty();
		};
	}

	public static String normalizeOrNull(String raw) {
		return normalizeOptional(raw).orElse(null);
	}
}
