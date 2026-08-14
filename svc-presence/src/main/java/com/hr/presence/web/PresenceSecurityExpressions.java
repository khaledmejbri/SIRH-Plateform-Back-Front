package com.hr.presence.web;

public final class PresenceSecurityExpressions {

	public static final String BACKOFFICE_LECTURE = "hasAnyRole('RH','DIRECTION','ADMIN')";

	public static final String BACKOFFICE_ECRITURE = "hasAnyRole('RH','ADMIN')";

	public static final String MOBILE_USER = "hasRole('USER')";

	private PresenceSecurityExpressions() {
	}
}
