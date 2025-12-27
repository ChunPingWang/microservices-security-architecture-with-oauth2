package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.AuthResponse;
import com.ecommerce.customer.application.dto.LoginCommand;
import com.ecommerce.customer.application.exception.AccountLockedException;
import com.ecommerce.customer.application.exception.InvalidCredentialsException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.domain.model.Password;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import com.ecommerce.shared.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthenticateCustomerUseCase 測試")
class AuthenticateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    private AuthenticateCustomerUseCase useCase;

    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_PASSWORD = "SecureP@ssw0rd";

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateCustomerUseCase(customerRepository, jwtTokenProvider, jwtProperties);
    }

    private Customer createActiveCustomer() {
        return new Customer(
                CustomerId.generate(),
                Email.of(TEST_EMAIL),
                Password.fromPlainText(TEST_PASSWORD),
                "Test User",
                CustomerStatus.ACTIVE,
                0,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    private Customer createLockedCustomer() {
        return new Customer(
                CustomerId.generate(),
                Email.of(TEST_EMAIL),
                Password.fromPlainText(TEST_PASSWORD),
                "Test User",
                CustomerStatus.LOCKED,
                5,
                Instant.now().plusSeconds(1800), // 鎖定30分鐘
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("應該成功認證有效的憑證")
    void shouldAuthenticateWithValidCredentials() {
        // Given
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);
        Customer customer = createActiveCustomer();

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(15));

        // When
        AuthResponse response = useCase.execute(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("找不到用戶時應該拋出 InvalidCredentialsException")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginCommand command = new LoginCommand("notfound@example.com", "Password1234");

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("密碼錯誤時應該拋出 InvalidCredentialsException")
    void shouldThrowExceptionForWrongPassword() {
        // Given
        LoginCommand command = new LoginCommand(TEST_EMAIL, "WrongP@ss1");
        Customer customer = createActiveCustomer();

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(customerRepository).save(any(Customer.class)); // 失敗也要儲存失敗次數
    }

    @Test
    @DisplayName("帳號被鎖定時應該拋出 AccountLockedException")
    void shouldThrowExceptionWhenAccountLocked() {
        // Given
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);
        Customer lockedCustomer = createLockedCustomer();

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(lockedCustomer));

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AccountLockedException.class)
                .extracting("lockedUntil")
                .isNotNull();
    }

    @Test
    @DisplayName("應該產生正確的 JWT tokens")
    void shouldGenerateCorrectTokens() {
        // Given
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);
        Customer customer = createActiveCustomer();

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(any(), eq(TEST_EMAIL), eq("CUSTOMER")))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(), eq(TEST_EMAIL), eq("CUSTOMER")))
                .thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(15));

        // When
        AuthResponse response = useCase.execute(command);

        // Then
        verify(jwtTokenProvider).generateAccessToken(any(), eq(TEST_EMAIL), eq("CUSTOMER"));
        verify(jwtTokenProvider).generateRefreshToken(any(), eq(TEST_EMAIL), eq("CUSTOMER"));
    }
}
