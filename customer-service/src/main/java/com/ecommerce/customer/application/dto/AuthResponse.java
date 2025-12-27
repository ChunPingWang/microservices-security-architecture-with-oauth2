package com.ecommerce.customer.application.dto;

/**
 * Authentication response with tokens.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        CustomerDto customer
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInSeconds, CustomerDto customer) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, customer);
    }
}
