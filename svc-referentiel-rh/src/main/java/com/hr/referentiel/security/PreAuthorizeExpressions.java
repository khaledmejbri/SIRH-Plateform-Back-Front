package com.hr.referentiel.security;

/**
 * Droits de la SPA RH / back-office.
 * <ul>
 *   <li>{@link #BACKOFFICE_LECTURE} : RH, DIRECTION, ADMIN — GET / consultation</li>
 *   <li>{@link #BACKOFFICE_ECRITURE} : RH, ADMIN — mutations (DIRECTION exclu)</li>
 * </ul>
 * {@link #BACKOFFICE_RH} est conservé comme alias de lecture pour rétrocompatibilité.
 */
public final class PreAuthorizeExpressions {

	public static final String BACKOFFICE_LECTURE = "hasAnyRole('RH','DIRECTION','ADMIN')";

	public static final String BACKOFFICE_ECRITURE = "hasAnyRole('RH','ADMIN')";

	/** @deprecated préférer {@link #BACKOFFICE_LECTURE} ou {@link #BACKOFFICE_ECRITURE} */
	@Deprecated
	public static final String BACKOFFICE_RH = BACKOFFICE_LECTURE;

	private PreAuthorizeExpressions() {
	}
}
