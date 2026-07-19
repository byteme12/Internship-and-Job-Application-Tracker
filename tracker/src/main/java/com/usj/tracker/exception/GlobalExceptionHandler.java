package com.usj.tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catches exceptions thrown anywhere in the controller layer and converts
 * them into consistent JSON error responses, instead of leaking stack
 * traces or generic 500s to the frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidTransitionException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ApplicationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
