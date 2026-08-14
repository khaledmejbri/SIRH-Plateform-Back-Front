package com.hr.presence.web;

/**
 * Exception métier présence — codes HTTP 409 / 422 / 404.
 */
public class PresenceMetierException extends RuntimeException {

	private final String code;
	private final int httpStatus;

	public PresenceMetierException(String code, String message, int httpStatus) {
		super(message);
		this.code = code;
		this.httpStatus = httpStatus;
	}

	public String getCode() {
		return code;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public static PresenceMetierException unprocessable(String code, String message) {
		return new PresenceMetierException(code, message, 422);
	}

	public static PresenceMetierException conflict(String code, String message) {
		return new PresenceMetierException(code, message, 409);
	}

	public static PresenceMetierException notFound(String code, String message) {
		return new PresenceMetierException(code, message, 404);
	}
}
