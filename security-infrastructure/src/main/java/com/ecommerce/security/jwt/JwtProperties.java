package com.ecommerce.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT configuration properties.
 * Can be configured via application.yml with prefix 'jwt'.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * Should be at least 256 bits (32 characters) for HS256.
     */
    private String secretKey = "your-256-bit-secret-key-here-please-change-in-production";

    /**
     * JWT issuer claim.
     */
    private String issuer = "ecommerce-platform";

    /**
     * Access token expiration time.
     * Default: 15 minutes
     */
    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    /**
     * Refresh token expiration time.
     * Default: 7 days
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    /**
     * Service token expiration time for internal service-to-service calls.
     * Default: 5 minutes
     */
    private Duration serviceTokenExpiration = Duration.ofMinutes(5);

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public Duration getServiceTokenExpiration() {
        return serviceTokenExpiration;
    }

    public void setServiceTokenExpiration(Duration serviceTokenExpiration) {
        this.serviceTokenExpiration = serviceTokenExpiration;
    }
}
