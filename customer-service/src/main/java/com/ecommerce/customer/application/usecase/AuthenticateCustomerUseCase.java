package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.AuthResponse;
import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.LoginCommand;
import com.ecommerce.customer.application.exception.AccountLockedException;
import com.ecommerce.customer.application.exception.InvalidCredentialsException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import com.ecommerce.shared.vo.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for customer authentication (login).
 */
@Service
public class AuthenticateCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateCustomerUseCase.class);

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthenticateCustomerUseCase(CustomerRepository customerRepository,
                                        JwtTokenProvider jwtTokenProvider,
                                        JwtProperties jwtProperties) {
        this.customerRepository = customerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse execute(LoginCommand command) {
        log.info("Authenticating customer: {}", command.email());

        Email email = Email.of(command.email());

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        // Check if account is locked
        if (customer.isLocked()) {
            log.warn("Login attempt for locked account: {}", command.email());
            throw new AccountLockedException(customer.getLockedUntil());
        }

        // Authenticate
        boolean authenticated = customer.authenticate(command.password());

        // Save to persist failed login attempts or lock status
        customer = customerRepository.save(customer);

        if (!authenticated) {
            log.warn("Failed login attempt for: {}", command.email());

            if (customer.isLocked()) {
                throw new AccountLockedException(customer.getLockedUntil());
            }

            throw new InvalidCredentialsException();
        }

        log.info("Customer authenticated successfully: {}", customer.getId().asString());

        // Generate tokens
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
