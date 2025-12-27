package com.ecommerce.customer.application.exception;

/**
 * Exception thrown when token is invalid.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
