package com.ecommerce.security.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials");
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is locked");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Token has expired");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid token");
    }
}
