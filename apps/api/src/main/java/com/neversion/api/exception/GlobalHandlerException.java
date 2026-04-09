package com.neversion.api.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalHandlerException {

        private static final Logger log = LoggerFactory.getLogger(GlobalHandlerException.class);

        // ── 404: Resource Not Found ─────────────────────────────────────────

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // ── 400: @Valid Field Validation Errors ─────────────────────────────

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        WebRequest request) {

                List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> new ErrorResponse.FieldError(
                                                error.getField(),
                                                error.getDefaultMessage(),
                                                error.getRejectedValue()))
                                .toList();

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                "One or more fields failed validation.",
                                extractPath(request),
                                fieldErrors);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // ── 400: Malformed JSON Body ────────────────────────────────────────

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "Malformed JSON request. Please check the request body.",
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // ── 400: Type Mismatch (e.g., invalid UUID in path) ────────────────

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                        WebRequest request) {
                String detail = String.format("Parameter '%s' must be of type '%s'. Rejected value: '%s'",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                                ex.getValue());
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                detail,
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // ── 400: Domain IllegalArgumentException (e.g., value objects) ──────

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // ── 409: Account Overbooking (BR-06) ───────────────────────────────
        // Must be declared BEFORE BusinessRuleException (its parent class)

        @ExceptionHandler(AccountOverbookingException.class)
        public ResponseEntity<ErrorResponse> handleAccountOverbookingException(AccountOverbookingException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Account Overbooking",
                                ex.getMessage(),
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // ── 409: Business Rule Violation ────────────────────────────────────

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Business Rule Violation",
                                ex.getMessage(),
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // ── 409: Database Constraint Violation ──────────────────────────────

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                        WebRequest request) {
                log.warn("Data integrity violation at {}: {}", extractPath(request), ex.getMostSpecificCause().getMessage());
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                "A data integrity constraint was violated. The operation could not be completed.",
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // ── 403: Insufficient Permissions ───────────────────────────────────

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "Insufficient permissions",
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // ── 500: Catch-All Fallback ─────────────────────────────────────────

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                        WebRequest request) {
                log.error("Unhandled exception at {}: {}", extractPath(request), ex.getMessage(), ex);
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred. Please contact support.",
                                extractPath(request));
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // ── Helper ─────────────────────────────────────────────────────────

        private String extractPath(WebRequest request) {
                return request.getDescription(false).replace("uri=", "");
        }
}
