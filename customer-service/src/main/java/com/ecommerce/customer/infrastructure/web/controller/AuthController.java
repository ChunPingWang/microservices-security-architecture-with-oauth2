package com.ecommerce.customer.infrastructure.web.controller;

import com.ecommerce.customer.application.dto.*;
import com.ecommerce.customer.application.usecase.AuthenticateCustomerUseCase;
import com.ecommerce.customer.application.usecase.RefreshTokenUseCase;
import com.ecommerce.customer.application.usecase.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final AuthenticateCustomerUseCase authenticateCustomerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(RegisterCustomerUseCase registerCustomerUseCase,
                          AuthenticateCustomerUseCase authenticateCustomerUseCase,
                          RefreshTokenUseCase refreshTokenUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.authenticateCustomerUseCase = authenticateCustomerUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    /**
     * Register a new customer.
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCustomerCommand command) {
        log.info("Registration request for email: {}", command.email());
        AuthResponse response = registerCustomerUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Customer login.
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        log.info("Login request for email: {}", command.email());
        AuthResponse response = authenticateCustomerUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token.
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenCommand command) {
        log.debug("Token refresh request");
        AuthResponse response = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(response);
    }
}
