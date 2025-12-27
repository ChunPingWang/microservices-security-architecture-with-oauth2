package com.ecommerce.security.exception;

/**
 * Exception thrown when authorization fails (user lacks required permissions).
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AuthorizationException accessDenied() {
        return new AuthorizationException("Access denied");
    }

    public static AuthorizationException insufficientRole(String requiredRole) {
        return new AuthorizationException("Insufficient role. Required: " + requiredRole);
    }
}
