package com.neversion.api.exception;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standardized error response DTO (RFC 7807-inspired).
 * The {@code fieldErrors} list is only included in the JSON output
 * when it is non-null (i.e., for @Valid / MethodArgumentNotValidException).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors) {

    /**
     * Convenience constructor for responses without field-level errors.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }

    /**
     * Represents a single field validation error.
     */
    public record FieldError(
            String field,
            String message,
            Object rejectedValue) {
    }
}
