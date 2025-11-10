package com.cobanoglu.airlinemanagement.exception;

import com.cobanoglu.airlinemanagement.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildResponse(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .path(path)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage()));

        String message = fieldErrors.isEmpty()
                ? "Validation error"
                : fieldErrors.values().iterator().next();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, message, req.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String lowerMsg = msg.toLowerCase();

        if (lowerMsg.contains("email")
                || lowerMsg.contains("unique")
                || lowerMsg.contains("duplicate")
                || lowerMsg.contains("tekrar eden")
                || lowerMsg.contains("uk_")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.CONFLICT.value())
                            .message("Email is already registered.")
                            .path(req.getRequestURI())
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Data integrity violation: " + msg)
                        .path(req.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", req.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(OverbookingException.class)
    public ResponseEntity<ErrorResponse> handleOverbooking(OverbookingException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();

        String message = "Overbooking limit reached — This flight has reached 110% of its capacity and is now closed for new reservations.";
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            message += " (" + ex.getMessage() + ")";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, message, req.getRequestURI()));
    }

    @ExceptionHandler(FlightConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(FlightConflictException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildResponse(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildResponse(HttpStatus.FORBIDDEN, "Access denied: You do not have permission to perform this action.", req.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed: Invalid or missing credentials.", req.getRequestURI()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        String message = ex.getMessage() != null ? ex.getMessage() : "Unexpected error occurred";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, message, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        if (isSwaggerRequest(req)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req.getRequestURI()));
    }

    private boolean isSwaggerRequest(HttpServletRequest req) {
        String path = req.getRequestURI();
        return path.contains("/swagger") || path.contains("/api-docs");
    }
}