package com.ecommerce.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT Token provider for generating and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Generate an access token for a user.
     */
    public String generateAccessToken(String userId, String email, String role) {
        return generateToken(userId, email, role, TokenType.ACCESS,
                jwtProperties.getAccessTokenExpiration().toMillis());
    }

    /**
     * Generate a refresh token for a user.
     */
    public String generateRefreshToken(String userId, String email, String role) {
        return generateToken(userId, email, role, TokenType.REFRESH,
                jwtProperties.getRefreshTokenExpiration().toMillis());
    }

    /**
     * Generate a service token for internal service-to-service communication.
     */
    public String generateServiceToken(String serviceName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, serviceName);
        claims.put(CLAIM_EMAIL, serviceName + "@internal");
        claims.put(CLAIM_ROLE, "SERVICE");
        claims.put(CLAIM_TOKEN_TYPE, TokenType.SERVICE.name());

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getServiceTokenExpiration().toMillis());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(serviceName)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    private String generateToken(String userId, String email, String role,
                                  TokenType tokenType, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_EMAIL, email);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_TOKEN_TYPE, tokenType.name());

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userId)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate a JWT token and return claims if valid.
     */
    public Optional<TokenClaims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new TokenClaims(
                    claims.get(CLAIM_USER_ID, String.class),
                    claims.get(CLAIM_EMAIL, String.class),
                    claims.get(CLAIM_ROLE, String.class),
                    TokenType.valueOf(claims.get(CLAIM_TOKEN_TYPE, String.class)),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()
            ));
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Check if a token is valid (not expired and properly signed).
     */
    public boolean isTokenValid(String token) {
        return validateToken(token).isPresent();
    }

    /**
     * Extract user ID from token without full validation.
     * Use with caution - does not verify signature.
     */
    public Optional<String> extractUserId(String token) {
        return validateToken(token).map(TokenClaims::userId);
    }

    /**
     * Token types.
     */
    public enum TokenType {
        ACCESS,
        REFRESH,
        SERVICE
    }

    /**
     * Token claims record.
     */
    public record TokenClaims(
            String userId,
            String email,
            String role,
            TokenType tokenType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        public boolean isAccessToken() {
            return tokenType == TokenType.ACCESS;
        }

        public boolean isRefreshToken() {
            return tokenType == TokenType.REFRESH;
        }

        public boolean isServiceToken() {
            return tokenType == TokenType.SERVICE;
        }
    }
}
