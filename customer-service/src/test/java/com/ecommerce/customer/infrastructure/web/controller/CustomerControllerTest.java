package com.ecommerce.customer.infrastructure.web.controller;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.usecase.GetCustomerProfileUseCase;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.security.context.CurrentUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController 測試")
class CustomerControllerTest {

    @Mock
    private GetCustomerProfileUseCase getCustomerProfileUseCase;

    @Mock
    private CurrentUserContext currentUserContext;

    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        customerController = new CustomerController(getCustomerProfileUseCase);
    }

    private CustomerDto createCustomerDto(String id) {
        return new CustomerDto(
                id,
                "user@example.com",
                "Test User",
                CustomerStatus.ACTIVE,
                Instant.now()
        );
    }

    @Test
    @DisplayName("getMyProfile 應該回傳當前用戶資料")
    void getMyProfileShouldReturnCurrentUserProfile() {
        // Given
        String userId = UUID.randomUUID().toString();
        CustomerDto customerDto = createCustomerDto(userId);

        when(currentUserContext.getUserId()).thenReturn(Optional.of(userId));
        when(currentUserContext.requireUserId()).thenReturn(userId);
        when(getCustomerProfileUseCase.execute(userId)).thenReturn(customerDto);

        // When
        ResponseEntity<CustomerDto> response = customerController.getMyProfile(currentUserContext);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userId);
        assertThat(response.getBody().email()).isEqualTo("user@example.com");

        verify(getCustomerProfileUseCase).execute(userId);
    }

    @Test
    @DisplayName("getCustomerById 應該回傳指定客戶資料")
    void getCustomerByIdShouldReturnCustomerProfile() {
        // Given
        String customerId = UUID.randomUUID().toString();
        CustomerDto customerDto = createCustomerDto(customerId);

        when(getCustomerProfileUseCase.execute(customerId)).thenReturn(customerDto);

        // When
        ResponseEntity<CustomerDto> response = customerController.getCustomerById(customerId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(customerId);

        verify(getCustomerProfileUseCase).execute(customerId);
    }
}
