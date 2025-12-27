package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.AuthResponse;
import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.ecommerce.customer.application.exception.EmailAlreadyExistsException;
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
 * Use case for customer registration.
 */
@Service
public class RegisterCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterCustomerUseCase.class);

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public RegisterCustomerUseCase(CustomerRepository customerRepository,
                                    JwtTokenProvider jwtTokenProvider,
                                    JwtProperties jwtProperties) {
        this.customerRepository = customerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse execute(RegisterCustomerCommand command) {
        log.info("Registering new customer with email: {}", command.email());

        Email email = Email.of(command.email());

        // Check if email already exists
        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // Create customer
        Customer customer = Customer.register(email, command.password(), command.name());

        // Save customer
        customer = customerRepository.save(customer);

        log.info("Customer registered successfully: {}", customer.getId().asString());

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
