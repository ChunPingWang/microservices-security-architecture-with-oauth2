package com.ecommerce.gateway.filter;

import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthenticationWebFilter Tests")
class JwtAuthenticationWebFilterTest {

    private JwtAuthenticationWebFilter filter;
    private JwtTokenProvider jwtTokenProvider;
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("this-is-a-very-long-secret-key-for-testing-purposes-only-256-bits");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        filter = new JwtAuthenticationWebFilter(jwtTokenProvider);

        filterChain = mock(WebFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Nested
    @DisplayName("With valid access token")
    class WithValidAccessTokenTests {

        @Test
        @DisplayName("should authenticate user and add headers")
        void shouldAuthenticateUserAndAddHeaders() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/v1/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            // Verify headers are added
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                    .isEqualTo("user-123");
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Email"))
                    .isEqualTo("test@example.com");
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Role"))
                    .isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("should set security context with authentication")
        void shouldSetSecurityContextWithAuthentication() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "ADMIN");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/admin/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Create a chain that captures the security context
            WebFilterChain captureChain = ex -> ReactiveSecurityContextHolder.getContext()
                    .doOnNext(ctx -> {
                        assertThat(ctx.getAuthentication()).isNotNull();
                        assertThat(ctx.getAuthentication().getPrincipal()).isEqualTo("user-123");
                        assertThat(ctx.getAuthentication().getAuthorities())
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    })
                    .then();

            StepVerifier.create(filter.filter(exchange, captureChain))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("With service token")
    class WithServiceTokenTests {

        @Test
        @DisplayName("should authenticate service and add headers")
        void shouldAuthenticateServiceAndAddHeaders() {
            String token = jwtTokenProvider.generateServiceToken("order-service");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/internal/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Role"))
                    .isEqualTo("SERVICE");
        }
    }

    @Nested
    @DisplayName("Without token")
    class WithoutTokenTests {

        @Test
        @DisplayName("should pass through without authentication")
        void shouldPassThroughWithoutAuthentication() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/v1/products")
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            // No user headers should be added
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        }
    }

    @Nested
    @DisplayName("With invalid token")
    class WithInvalidTokenTests {

        @Test
        @DisplayName("should pass through with invalid token")
        void shouldPassThroughWithInvalidToken() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/v1/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            // No user headers should be added
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        }

        @Test
        @DisplayName("should pass through with non-Bearer token")
        void shouldPassThroughWithNonBearerToken() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/v1/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        }
    }

    @Nested
    @DisplayName("With refresh token")
    class WithRefreshTokenTests {

        @Test
        @DisplayName("should not authenticate with refresh token")
        void shouldNotAuthenticateWithRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("user-123", "test@example.com", "CUSTOMER");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/v1/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();

            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(filter.filter(exchange, filterChain))
                    .verifyComplete();

            // No user headers should be added for refresh token
            assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        }
    }
}
