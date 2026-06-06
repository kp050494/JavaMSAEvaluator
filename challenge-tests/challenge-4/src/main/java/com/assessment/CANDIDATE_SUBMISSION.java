package com.assessment;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * ===========================================================================
 *  CHALLENGE 4 - Global Exception Handling
 * ===========================================================================
 *  Reference implementation (replaced at runtime with candidate code).
 *  Build a @RestControllerAdvice that maps every failure to a consistent
 *  ErrorResponse JSON body with the fields:
 *     timestamp, status, message, path   (+ errors[] for validation failures)
 *  mapping:
 *     ResourceNotFoundException          -> 404
 *     MethodArgumentNotValidException    -> 400 with per-field error messages
 *     any other Exception                -> 500
 * ===========================================================================
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, errors);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest req, List<String> errors) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                message,
                req.getRequestURI(),
                errors);
        return ResponseEntity.status(status).body(body);
    }
}

/**
 * Consistent error payload returned for every failure.
 */
class ErrorResponse {
    private String timestamp;
    private int status;
    private String message;
    private String path;
    private List<String> errors;

    ErrorResponse(String timestamp, int status, String message, String path, List<String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<String> getErrors() { return errors; }
}
