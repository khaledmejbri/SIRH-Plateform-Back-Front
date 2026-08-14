package com.hr.presence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PageResponse<T>(
		@JsonProperty("content") List<T> content,
		@JsonProperty("page") int page,
		@JsonProperty("size") int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages
) {
}
