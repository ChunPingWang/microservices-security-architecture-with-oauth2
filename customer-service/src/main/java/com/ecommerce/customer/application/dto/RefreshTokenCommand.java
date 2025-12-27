package com.ecommerce.customer.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for token refresh.
 */
public record RefreshTokenCommand(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
