package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.exception.CustomerNotFoundException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.domain.model.Password;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.shared.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerProfileUseCase 測試")
class GetCustomerProfileUseCaseTest {

    private static final String VALID_PASSWORD = "SecureP@ssw0rd1";

    @Mock
    private CustomerRepository customerRepository;

    private GetCustomerProfileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerProfileUseCase(customerRepository);
    }

    private Customer createCustomer(UUID id) {
        return new Customer(
                CustomerId.of(id),
                Email.of("user@example.com"),
                Password.fromPlainText(VALID_PASSWORD),
                "Test User",
                CustomerStatus.ACTIVE,
                0,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-02T00:00:00Z")
        );
    }

    @Test
    @DisplayName("應該成功取得客戶資料")
    void shouldGetCustomerProfile() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = createCustomer(customerId);

        when(customerRepository.findById(any(CustomerId.class)))
                .thenReturn(Optional.of(customer));

        // When
        CustomerDto result = useCase.execute(customerId.toString());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(customerId.toString());
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("找不到客戶時應該拋出例外")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        String customerId = UUID.randomUUID().toString();

        when(customerRepository.findById(any(CustomerId.class)))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(customerId);
    }

    @Test
    @DisplayName("應該正確轉換客戶資料為 DTO")
    void shouldCorrectlyMapToDto() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = createCustomer(customerId);

        when(customerRepository.findById(any(CustomerId.class)))
                .thenReturn(Optional.of(customer));

        // When
        CustomerDto result = useCase.execute(customerId.toString());

        // Then
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("無效的 UUID 格式應該拋出例外")
    void shouldThrowExceptionForInvalidUuid() {
        // When/Then
        assertThatThrownBy(() -> useCase.execute("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
