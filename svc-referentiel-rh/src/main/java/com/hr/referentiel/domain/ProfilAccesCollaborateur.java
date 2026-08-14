package com.hr.referentiel.domain;

import java.util.Locale;

/**
 * Catalogue unique des profils d'accès collaborateur ({@code profil_acces}).
 * Chaque valeur correspond à un ensemble de rôles JWT (remplacement, pas union) :
 * <ul>
 *   <li>COLLABORATEUR → USER</li>
 *   <li>RO → USER, RO</li>
 *   <li>RESPONSABLE → USER, RESPONSABLE</li>
 *   <li>RH → USER, RH</li>
 *   <li>DIRECTION → USER, DIRECTION</li>
 *   <li>ADMIN → USER, ADMIN</li>
 * </ul>
 */
public enum ProfilAccesCollaborateur {

	COLLABORATEUR,
	RO,
	RESPONSABLE,
	RH,
	DIRECTION,
	ADMIN;

	public static final String VALEURS_ACCEPTEES = "COLLABORATEUR, RO, RESPONSABLE, RH, DIRECTION, ADMIN";

	public static final String MESSAGE_INVALIDE = "profil_acces invalide : " + VALEURS_ACCEPTEES + ".";

	/**
	 * {@code null} / blank → {@link #COLLABORATEUR}. Valeur hors catalogue → {@link IllegalArgumentException}.
	 */
	public static ProfilAccesCollaborateur parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return COLLABORATEUR;
		}
		try {
			return valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(MESSAGE_INVALIDE);
		}
	}
}
