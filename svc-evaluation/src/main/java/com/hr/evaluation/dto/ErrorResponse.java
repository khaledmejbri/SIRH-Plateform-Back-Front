package com.hr.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response for all API endpoints.
 * Provides clear, user-friendly error messages with actionable guidance.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @JsonProperty("code")
    String code,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("details")
    String details,
    
    @JsonProperty("timestamp")
    Instant timestamp,
    
    @JsonProperty("path")
    String path,
    
    @JsonProperty("suggestions")
    List<String> suggestions,
    
    @JsonProperty("httpStatus")
    int httpStatus
) {
    public static ErrorResponse of(String code, String message, int httpStatus) {
        return new ErrorResponse(code, message, null, Instant.now(), null, null, httpStatus);
    }
    
    public static ErrorResponse of(String code, String message, String details, int httpStatus) {
        return new ErrorResponse(code, message, details, Instant.now(), null, null, httpStatus);
    }
    
    public static ErrorResponse of(String code, String message, String details, List<String> suggestions, int httpStatus) {
        return new ErrorResponse(code, message, details, Instant.now(), null, suggestions, httpStatus);
    }
}
