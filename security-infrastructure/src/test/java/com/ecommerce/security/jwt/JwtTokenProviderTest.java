package com.ecommerce.security.jwt;

import com.ecommerce.security.jwt.JwtTokenProvider.TokenClaims;
import com.ecommerce.security.jwt.JwtTokenProvider.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("this-is-a-very-long-secret-key-for-testing-purposes-only-256-bits");
        jwtProperties.setIssuer("test-issuer");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtProperties.setServiceTokenExpiration(Duration.ofMinutes(5));

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Nested
    @DisplayName("Access Token Tests")
    class AccessTokenTests {

        @Test
        @DisplayName("should generate valid access token")
        void shouldGenerateValidAccessToken() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");

            assertThat(token).isNotEmpty();
            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should extract claims from access token")
        void shouldExtractClaimsFromAccessToken() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");

            Optional<TokenClaims> claims = jwtTokenProvider.validateToken(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().userId()).isEqualTo("user-123");
            assertThat(claims.get().email()).isEqualTo("test@example.com");
            assertThat(claims.get().role()).isEqualTo("CUSTOMER");
            assertThat(claims.get().tokenType()).isEqualTo(TokenType.ACCESS);
            assertThat(claims.get().isAccessToken()).isTrue();
        }

        @Test
        @DisplayName("access token should have correct expiration")
        void accessTokenShouldHaveCorrectExpiration() {
            Instant before = Instant.now();
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");
            Instant after = Instant.now();

            Optional<TokenClaims> claims = jwtTokenProvider.validateToken(token);

            assertThat(claims).isPresent();
            Instant expectedExpiry = before.plus(Duration.ofMinutes(15));
            assertThat(claims.get().expiresAt())
                    .isAfterOrEqualTo(expectedExpiry.minusSeconds(5))
                    .isBeforeOrEqualTo(after.plus(Duration.ofMinutes(15)).plusSeconds(5));
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("user-123", "test@example.com", "CUSTOMER");

            assertThat(token).isNotEmpty();
            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should extract claims from refresh token")
        void shouldExtractClaimsFromRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("user-123", "test@example.com", "CUSTOMER");

            Optional<TokenClaims> claims = jwtTokenProvider.validateToken(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().tokenType()).isEqualTo(TokenType.REFRESH);
            assertThat(claims.get().isRefreshToken()).isTrue();
        }
    }

    @Nested
    @DisplayName("Service Token Tests")
    class ServiceTokenTests {

        @Test
        @DisplayName("should generate valid service token")
        void shouldGenerateValidServiceToken() {
            String token = jwtTokenProvider.generateServiceToken("order-service");

            assertThat(token).isNotEmpty();
            assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should extract claims from service token")
        void shouldExtractClaimsFromServiceToken() {
            String token = jwtTokenProvider.generateServiceToken("order-service");

            Optional<TokenClaims> claims = jwtTokenProvider.validateToken(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().userId()).isEqualTo("order-service");
            assertThat(claims.get().role()).isEqualTo("SERVICE");
            assertThat(claims.get().tokenType()).isEqualTo(TokenType.SERVICE);
            assertThat(claims.get().isServiceToken()).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should reject invalid token")
        void shouldRejectInvalidToken() {
            String invalidToken = "invalid.token.here";

            Optional<TokenClaims> claims = jwtTokenProvider.validateToken(invalidToken);

            assertThat(claims).isEmpty();
            assertThat(jwtTokenProvider.isTokenValid(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("should reject tampered token")
        void shouldRejectTamperedToken() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            assertThat(jwtTokenProvider.isTokenValid(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("should reject token with wrong secret")
        void shouldRejectTokenWithWrongSecret() {
            // Generate token with current provider
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");

            // Create a new provider with different secret
            JwtProperties otherProperties = new JwtProperties();
            otherProperties.setSecretKey("different-secret-key-256-bits-long-for-testing-purposes");
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProperties);

            assertThat(otherProvider.isTokenValid(token)).isFalse();
        }

        @Test
        @DisplayName("should extract user ID from token")
        void shouldExtractUserIdFromToken() {
            String token = jwtTokenProvider.generateAccessToken("user-456", "test@example.com", "CUSTOMER");

            Optional<String> userId = jwtTokenProvider.extractUserId(token);

            assertThat(userId).isPresent().contains("user-456");
        }
    }

    @Nested
    @DisplayName("Token Claims Record Tests")
    class TokenClaimsTests {

        @Test
        @DisplayName("isAccessToken should return true for access token")
        void isAccessTokenShouldReturnTrueForAccessToken() {
            String token = jwtTokenProvider.generateAccessToken("user-123", "test@example.com", "CUSTOMER");
            TokenClaims claims = jwtTokenProvider.validateToken(token).orElseThrow();

            assertThat(claims.isAccessToken()).isTrue();
            assertThat(claims.isRefreshToken()).isFalse();
            assertThat(claims.isServiceToken()).isFalse();
        }

        @Test
        @DisplayName("isRefreshToken should return true for refresh token")
        void isRefreshTokenShouldReturnTrueForRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("user-123", "test@example.com", "CUSTOMER");
            TokenClaims claims = jwtTokenProvider.validateToken(token).orElseThrow();

            assertThat(claims.isRefreshToken()).isTrue();
            assertThat(claims.isAccessToken()).isFalse();
            assertThat(claims.isServiceToken()).isFalse();
        }

        @Test
        @DisplayName("isServiceToken should return true for service token")
        void isServiceTokenShouldReturnTrueForServiceToken() {
            String token = jwtTokenProvider.generateServiceToken("order-service");
            TokenClaims claims = jwtTokenProvider.validateToken(token).orElseThrow();

            assertThat(claims.isServiceToken()).isTrue();
            assertThat(claims.isAccessToken()).isFalse();
            assertThat(claims.isRefreshToken()).isFalse();
        }
    }
}
