package com.jala.backend.common.exception;

import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.storage.exception.FileStorageException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String GENERIC_ERROR =
            "An unexpected error occurred. Please try again later.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex) {

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Bean Validation failures on {@code @Valid @RequestBody} DTOs.
     * Field errors are returned in {@code data} keyed by field name.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message(VALIDATION_FAILED)
                        .data(fieldErrors)
                        .build());
    }

    /**
     * Bean Validation failures on method parameters
     * ({@code @RequestParam} / {@code @PathVariable}).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        return error(HttpStatus.BAD_REQUEST, VALIDATION_FAILED + ": " + ex.getMessage());
    }

    /** Malformed or unreadable JSON request body. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException ex) {

        log.debug("Unreadable request body", ex);

        return error(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    /** Type mismatch on a path variable or query param (e.g. non-UUID id). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        return error(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'");
    }

    /** Unique/foreign-key constraint violations surfacing from the database. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.warn("Data integrity violation", ex);

        return error(HttpStatus.CONFLICT,
                "The request conflicts with existing data");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUpload(
            MaxUploadSizeExceededException ex) {

        return error(HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded file exceeds the maximum allowed size");
    }

    /**
     * Rejected uploads (bad extension, path-traversal, unsupported or
     * mislabeled content) are client errors — return 400 with the
     * envelope instead of letting them fall through to a generic 500.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorage(
            FileStorageException ex) {

        log.warn("File storage rejected an upload: {}", ex.getMessage());

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(
            TooManyRequestsException ex) {

        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    /**
     * Login failures (bad credentials, disabled account, ...).
     * A single generic message avoids user enumeration.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException ex) {

        log.debug("Authentication failed: {}", ex.getMessage());

        return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    /**
     * Method-security denials ({@code @PreAuthorize}) thrown inside
     * controllers/services, past the filter-chain handler.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {

        return error(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action");
    }

    /** Unknown paths (Boot 3.2+ throws instead of 404-ing silently). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(
            NoResourceFoundException ex) {

        return error(HttpStatus.NOT_FOUND, "Resource not found");
    }

    /**
     * Last-resort handler: log the full exception server-side, return a
     * generic message so internals are never leaked to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {

        log.error("Unhandled exception", ex);

        return error(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .build());
    }
}
