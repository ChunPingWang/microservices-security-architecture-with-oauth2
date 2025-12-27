package com.ecommerce.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FallbackController Tests")
class FallbackControllerTest {

    private FallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new FallbackController();
    }

    @Test
    @DisplayName("should return 503 for service unavailable")
    void shouldReturn503ForServiceUnavailable() {
        StepVerifier.create(controller.serviceUnavailable())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("error")).isEqualTo("SERVICE_UNAVAILABLE");
                    assertThat(body.get("message")).isNotNull();
                    assertThat(body.get("timestamp")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 429 for rate limit exceeded")
    void shouldReturn429ForRateLimitExceeded() {
        StepVerifier.create(controller.rateLimitExceeded())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("error")).isEqualTo("RATE_LIMIT_EXCEEDED");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 401 for unauthorized")
    void shouldReturn401ForUnauthorized() {
        StepVerifier.create(controller.unauthorized())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("error")).isEqualTo("UNAUTHORIZED");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 403 for forbidden")
    void shouldReturn403ForForbidden() {
        StepVerifier.create(controller.forbidden())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("error")).isEqualTo("FORBIDDEN");
                })
                .verifyComplete();
    }
}
