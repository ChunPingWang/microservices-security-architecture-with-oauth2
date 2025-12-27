package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.AuthResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.ecommerce.customer.application.exception.EmailAlreadyExistsException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.security.jwt.JwtProperties;
import com.ecommerce.security.jwt.JwtTokenProvider;
import com.ecommerce.shared.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegisterCustomerUseCase 測試")
class RegisterCustomerUseCaseTest {

    private static final String VALID_PASSWORD = "SecureP@ssw0rd";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    private RegisterCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterCustomerUseCase(customerRepository, jwtTokenProvider, jwtProperties);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("應該成功註冊新客戶")
    void shouldRegisterNewCustomer() {
        // Given
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "newuser@example.com",
                VALID_PASSWORD,
                "New User"
        );

        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        // When
        AuthResponse response = useCase.execute(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(customerRepository).existsByEmail(Email.of("newuser@example.com"));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("應該儲存正確的客戶資料")
    void shouldSaveCorrectCustomerData() {
        // Given
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "test@example.com",
                VALID_PASSWORD,
                "Test User"
        );

        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh");

        // When
        useCase.execute(command);

        // Then
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(savedCustomer.getName()).isEqualTo("Test User");
        assertThat(savedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(savedCustomer.getPassword().matches(VALID_PASSWORD)).isTrue();
    }

    @Test
    @DisplayName("email 已存在時應該拋出例外")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "existing@example.com",
                VALID_PASSWORD,
                "Existing User"
        );

        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("應該產生正確的 JWT tokens")
    void shouldGenerateCorrectTokens() {
        // Given
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "user@example.com",
                VALID_PASSWORD,
                "User Name"
        );

        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(), eq("user@example.com"), eq("CUSTOMER")))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(), eq("user@example.com"), eq("CUSTOMER")))
                .thenReturn("refresh-token");

        // When
        AuthResponse response = useCase.execute(command);

        // Then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(900L); // 15 minutes = 900 seconds

        verify(jwtTokenProvider).generateAccessToken(any(), eq("user@example.com"), eq("CUSTOMER"));
        verify(jwtTokenProvider).generateRefreshToken(any(), eq("user@example.com"), eq("CUSTOMER"));
    }
}
