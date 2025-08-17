package io.example.github.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<?> rateLimit(HttpClientErrorException.TooManyRequests ex) {
        String retryAfter = ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null;
        return ResponseEntity.status(429).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 429,
                "error", "Rate limit exceeded",
                "message", ex.getMessage(),
                "retryAfter", retryAfter
        ));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> clientErr(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", ex.getStatusCode().value(),
                "error", "Client Error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<?> serverErr(HttpServerErrorException ex) {
        return ResponseEntity.status(502).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 502,
                "error", "Upstream Error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> badReq(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 400,
                "error", "Validation Error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> fallback(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 500,
                "error", "Internal Error",
                "message", ex.getMessage()
        ));
    }
}
