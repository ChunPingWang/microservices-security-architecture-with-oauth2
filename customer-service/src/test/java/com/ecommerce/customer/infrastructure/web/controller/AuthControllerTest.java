package com.ecommerce.customer.infrastructure.web.controller;

import com.ecommerce.customer.application.dto.*;
import com.ecommerce.customer.application.usecase.AuthenticateCustomerUseCase;
import com.ecommerce.customer.application.usecase.RefreshTokenUseCase;
import com.ecommerce.customer.application.usecase.RegisterCustomerUseCase;
import com.ecommerce.customer.domain.model.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 測試")
class AuthControllerTest {

    @Mock
    private RegisterCustomerUseCase registerCustomerUseCase;

    @Mock
    private AuthenticateCustomerUseCase authenticateCustomerUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                registerCustomerUseCase,
                authenticateCustomerUseCase,
                refreshTokenUseCase
        );
    }

    private CustomerDto createCustomerDto() {
        return new CustomerDto(
                UUID.randomUUID().toString(),
                "test@example.com",
                "Test User",
                CustomerStatus.ACTIVE,
                Instant.now()
        );
    }

    @Test
    @DisplayName("註冊成功應該回傳 201 CREATED")
    void registerShouldReturn201Created() {
        // Given
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "test@example.com",
                "Password123!",
                "Test User"
        );

        AuthResponse authResponse = new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900L,
                createCustomerDto()
        );

        when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                .thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.register(command);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access-token");

        verify(registerCustomerUseCase).execute(command);
    }

    @Test
    @DisplayName("登入成功應該回傳 200 OK")
    void loginShouldReturn200Ok() {
        // Given
        LoginCommand command = new LoginCommand("test@example.com", "Password123!");

        AuthResponse authResponse = new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900L,
                createCustomerDto()
        );

        when(authenticateCustomerUseCase.execute(any(LoginCommand.class)))
                .thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.login(command);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access-token");

        verify(authenticateCustomerUseCase).execute(command);
    }

    @Test
    @DisplayName("Token 刷新成功應該回傳 200 OK")
    void refreshShouldReturn200Ok() {
        // Given
        RefreshTokenCommand command = new RefreshTokenCommand("old-refresh-token");

        AuthResponse authResponse = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                "Bearer",
                900L,
                createCustomerDto()
        );

        when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class)))
                .thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.refresh(command);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("new-access-token");

        verify(refreshTokenUseCase).execute(command);
    }
}
