package com.smartwater.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidReadingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReading(InvalidReadingException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("INVALID_READING")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request parameters failed validation checks")
                .path(request.getRequestURI())
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        if (rootMsg != null) {
            String lower = rootMsg.toLowerCase();
            if (lower.contains("users_email_key") || lower.contains("idx_users_email") || lower.contains("users.email")) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("DUPLICATE_RESOURCE")
                        .message("Email already registered: A user with this email address already exists")
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            if (lower.contains("uq_meter_reading_date") || (lower.contains("water_usage_logs") && lower.contains("duplicate key"))) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("DUPLICATE_RESOURCE")
                        .message("A water reading already exists for this meter on the selected date")
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            if (lower.contains("uq_apartment_flat") || (lower.contains("households") && lower.contains("flat_number"))) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("DUPLICATE_RESOURCE")
                        .message("Flat number already exists in this apartment community")
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            if (lower.contains("meters_meter_number_key") || (lower.contains("meters") && lower.contains("meter_number"))) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("DUPLICATE_RESOURCE")
                        .message("Meter serial number is already registered")
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("DATA_INTEGRITY_VIOLATION")
                .message("Operation failed due to database constraint violation: " + (rootMsg != null ? rootMsg : "Constraint violation"))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("Access Denied: You do not have permission to access this resource")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
