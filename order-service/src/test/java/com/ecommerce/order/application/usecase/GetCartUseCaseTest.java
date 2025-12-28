package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    private GetCartUseCase useCase;

    private static final String CUSTOMER_ID = "customer-123";

    @BeforeEach
    void setUp() {
        useCase = new GetCartUseCase(cartRepository);
    }

    @Test
    void execute_shouldReturnExistingCart() {
        // Given
        Cart cart = Cart.create(CUSTOMER_ID);
        cart.addItem("product-001", "iPhone 15", Money.of(new BigDecimal("999.99")), 2, "image.jpg");

        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));

        // When
        CartDto result = useCase.execute(CUSTOMER_ID);

        // Then
        assertNotNull(result);
        assertEquals(CUSTOMER_ID, result.customerId());
        assertEquals(1, result.distinctItemCount());
        assertEquals(2, result.totalItemCount());
    }

    @Test
    void execute_shouldReturnNewCartWhenNotExists() {
        // Given
        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        // When
        CartDto result = useCase.execute(CUSTOMER_ID);

        // Then
        assertNotNull(result);
        assertEquals(CUSTOMER_ID, result.customerId());
        assertEquals(0, result.distinctItemCount());
        assertEquals(0, result.totalItemCount());
        assertTrue(result.items().isEmpty());
    }

    @Test
    void execute_shouldMapCartItemsCorrectly() {
        // Given
        Cart cart = Cart.create(CUSTOMER_ID);
        cart.addItem("product-001", "iPhone 15", Money.of(new BigDecimal("999.99")), 2, "image.jpg");
        cart.addItem("product-002", "MacBook Pro", Money.of(new BigDecimal("2499.99")), 1, "macbook.jpg");

        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));

        // When
        CartDto result = useCase.execute(CUSTOMER_ID);

        // Then
        assertEquals(2, result.items().size());
        assertEquals("iPhone 15", result.items().get(0).productName());
        assertEquals("MacBook Pro", result.items().get(1).productName());
    }
}
