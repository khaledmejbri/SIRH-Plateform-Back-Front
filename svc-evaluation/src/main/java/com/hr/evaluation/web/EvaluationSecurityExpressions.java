package com.hr.evaluation.web;

/**
 * Droits évaluations (alignés matrice § 4.2 / vague rôles).
 * <ul>
 *   <li>{@link #BACKOFFICE_LECTURE} : RH, DIRECTION, ADMIN — consultation campagnes / scores</li>
 *   <li>{@link #BACKOFFICE_ECRITURE} : RH, ADMIN — mutations (DIRECTION exclu)</li>
 *   <li>{@link #MOBILE_USER} : parcours mobile collaborateur / manager (tout profil inclut USER)</li>
 * </ul>
 * Pas de rôle fantôme {@code COLLABORATOR}.
 */
public final class EvaluationSecurityExpressions {

	public static final String BACKOFFICE_LECTURE = "hasAnyRole('RH','DIRECTION','ADMIN')";

	public static final String BACKOFFICE_ECRITURE = "hasAnyRole('RH','ADMIN')";

	public static final String MOBILE_USER = "hasRole('USER')";

	/** @deprecated préférer {@link #BACKOFFICE_LECTURE} ou {@link #BACKOFFICE_ECRITURE} */
	@Deprecated
	public static final String BACKOFFICE_RH = BACKOFFICE_LECTURE;

	private EvaluationSecurityExpressions() {
	}
}
