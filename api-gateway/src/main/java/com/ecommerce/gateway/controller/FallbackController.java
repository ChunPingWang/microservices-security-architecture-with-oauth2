package com.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/service-unavailable")
    public Mono<ResponseEntity<Map<String, Object>>> serviceUnavailable() {
        Map<String, Object> response = Map.of(
                "success", false,
                "error", "SERVICE_UNAVAILABLE",
                "message", "服務暫時無法使用，請稍後再試",
                "timestamp", Instant.now().toString()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/rate-limit-exceeded")
    public Mono<ResponseEntity<Map<String, Object>>> rateLimitExceeded() {
        Map<String, Object> response = Map.of(
                "success", false,
                "error", "RATE_LIMIT_EXCEEDED",
                "message", "請求過於頻繁，請稍後再試",
                "timestamp", Instant.now().toString()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response));
    }

    @GetMapping("/unauthorized")
    public Mono<ResponseEntity<Map<String, Object>>> unauthorized() {
        Map<String, Object> response = Map.of(
                "success", false,
                "error", "UNAUTHORIZED",
                "message", "請先登入",
                "timestamp", Instant.now().toString()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response));
    }

    @GetMapping("/forbidden")
    public Mono<ResponseEntity<Map<String, Object>>> forbidden() {
        Map<String, Object> response = Map.of(
                "success", false,
                "error", "FORBIDDEN",
                "message", "權限不足",
                "timestamp", Instant.now().toString()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response));
    }
}
