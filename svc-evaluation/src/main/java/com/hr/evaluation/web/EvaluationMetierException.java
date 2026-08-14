package com.hr.evaluation.web;

/**
 * Exception métier avec code API stable (E1–E5).
 */
public class EvaluationMetierException extends RuntimeException {

	private final String code;
	private final int httpStatus;

	public EvaluationMetierException(String code, String message, int httpStatus) {
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

	public static EvaluationMetierException unprocessable(String code, String message) {
		return new EvaluationMetierException(code, message, 422);
	}

	public static EvaluationMetierException conflict(String code, String message) {
		return new EvaluationMetierException(code, message, 409);
	}

	public static EvaluationMetierException badRequest(String code, String message) {
		return new EvaluationMetierException(code, message, 400);
	}
}
