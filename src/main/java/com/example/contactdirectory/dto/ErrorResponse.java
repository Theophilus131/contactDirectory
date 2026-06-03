package com.example.contactdirectory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Structured error response returned for all API errors.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Structured error response")
public class ErrorResponse {

    @Schema(description = "ISO-8601 timestamp of the error", example = "2026-06-03T10:15:30Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error category", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error description", example = "Validation failed for one or more fields")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/contacts")
    private String path;

    @Schema(description = "Field-level validation errors (present only for validation failures)")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<FieldError> fieldErrors;

    /**
     * Individual field-level validation error.
     */
    @Data
    @Builder
    @Schema(description = "Details of a single field validation failure")
    public static class FieldError {

        @Schema(description = "Name of the field that failed validation", example = "email")
        private String field;

        @Schema(description = "Validation failure message", example = "Email must be a valid email address")
        private String message;
    }
}
