package com.hr.referentiel.domain;

import java.util.Locale;

/**
 * Catalogue fermé des types de congé ({@code contenu.type_conge}) — M01 § 6.2.
 * PJ / certificat obligatoire uniquement pour {@link #MALADIE} et {@link #MATERNITE}.
 */
public enum TypeConge {

	ANNUEL,
	MALADIE,
	MATERNITE,
	SANS_SOLDE,
	AUTRE;

	public static final String VALEURS_ACCEPTEES =
			"ANNUEL, MALADIE, MATERNITE, SANS_SOLDE, AUTRE";

	public static final String MESSAGE_INVALIDE =
			"type_conge invalide : " + VALEURS_ACCEPTEES + ".";

	public static final String MESSAGE_CERTIFICAT_OBLIGATOIRE =
			"Un certificat / pièce jointe est obligatoire pour un congé MALADIE ou MATERNITE.";

	/** {@code true} si une pièce jointe (certificat médical / maternité) est exigée. */
	public boolean exigePieceJointe() {
		return this == MALADIE || this == MATERNITE;
	}

	/**
	 * Parse une valeur catalogue. {@code null} / blank → {@link IllegalArgumentException}
	 * (le champ {@code type_conge} est déjà obligatoire en amont).
	 */
	public static TypeConge parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException(MESSAGE_INVALIDE);
		}
		try {
			return valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(MESSAGE_INVALIDE);
		}
	}
}
