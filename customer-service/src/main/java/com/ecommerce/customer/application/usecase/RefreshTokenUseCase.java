package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.AuthResponse;
import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.RefreshTokenCommand;
import com.ecommerce.customer.application.exception.InvalidTokenException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import com.ecommerce.security.jwt.JwtTokenProvider.TokenClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for refreshing access token.
 */
@Service
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public RefreshTokenUseCase(CustomerRepository customerRepository,
                                JwtTokenProvider jwtTokenProvider,
                                JwtProperties jwtProperties) {
        this.customerRepository = customerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(RefreshTokenCommand command) {
        log.debug("Processing token refresh");

        // Validate refresh token
        TokenClaims claims = jwtTokenProvider.validateToken(command.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        // Verify it's a refresh token
        if (!claims.isRefreshToken()) {
            throw new InvalidTokenException("Invalid token type");
        }

        // Find customer
        CustomerId customerId = CustomerId.of(claims.userId());
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InvalidTokenException("Customer not found"));

        // Check if customer can login
        if (!customer.canLogin()) {
            throw new InvalidTokenException("Account is not active");
        }

        log.info("Token refreshed for customer: {}", customer.getId().asString());

        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getId().asString(),
                customer.getEmail().getValue(),
                "CUSTOMER"
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                customer.getId().asString(),
                customer.getEmail().getValue(),
                "CUSTOMER"
        );

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenExpiration().toSeconds(),
                CustomerDto.from(customer)
        );
    }
}
