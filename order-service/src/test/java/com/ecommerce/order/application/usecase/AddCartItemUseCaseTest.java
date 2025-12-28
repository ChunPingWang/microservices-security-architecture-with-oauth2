package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.AddCartItemCommand;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.ProductInfo;
import com.ecommerce.order.application.exception.ProductNotAvailableException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.port.ProductServicePort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCartItemUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServicePort productServicePort;

    private AddCartItemUseCase useCase;

    private static final String CUSTOMER_ID = "customer-123";
    private static final String PRODUCT_ID = "product-001";

    @BeforeEach
    void setUp() {
        useCase = new AddCartItemUseCase(cartRepository, productServicePort);
    }

    @Test
    void execute_shouldAddItemToNewCart() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 2);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 100, "image.jpg", true);

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));
        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        // When
        CartDto result = useCase.execute(CUSTOMER_ID, command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.distinctItemCount());
        assertEquals(2, result.totalItemCount());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void execute_shouldAddItemToExistingCart() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 2);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 100, "image.jpg", true);

        Cart existingCart = Cart.create(CUSTOMER_ID);
        existingCart.addItem("other-product", "Other", Money.of(new BigDecimal("50.00")), 1, null);

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));
        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(existingCart));

        // When
        CartDto result = useCase.execute(CUSTOMER_ID, command);

        // Then
        assertEquals(2, result.distinctItemCount());
        assertEquals(3, result.totalItemCount());
    }

    @Test
    void execute_shouldIncreaseQuantityWhenProductAlreadyInCart() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 2);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 100, "image.jpg", true);

        Cart existingCart = Cart.create(CUSTOMER_ID);
        existingCart.addItem(PRODUCT_ID, "iPhone 15", Money.of(new BigDecimal("999.99")), 3, "image.jpg");

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));
        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(existingCart));

        // When
        CartDto result = useCase.execute(CUSTOMER_ID, command);

        // Then
        assertEquals(1, result.distinctItemCount());
        assertEquals(5, result.totalItemCount());
    }

    @Test
    void execute_shouldThrowWhenProductNotFound() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 2);
        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotAvailableException.class,
            () -> useCase.execute(CUSTOMER_ID, command));
    }

    @Test
    void execute_shouldThrowWhenProductOutOfStock() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 2);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 0, "image.jpg", false);

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));

        // When & Then
        assertThrows(ProductNotAvailableException.class,
            () -> useCase.execute(CUSTOMER_ID, command));
    }

    @Test
    void execute_shouldThrowWhenRequestedQuantityExceedsStock() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 10);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 5, "image.jpg", true);

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));

        // When & Then - Exception is thrown before cart repository is called
        assertThrows(ProductNotAvailableException.class,
            () -> useCase.execute(CUSTOMER_ID, command));
    }

    @Test
    void execute_shouldThrowWhenTotalQuantityExceedsStock() {
        // Given
        AddCartItemCommand command = new AddCartItemCommand(PRODUCT_ID, 3);
        ProductInfo productInfo = new ProductInfo(PRODUCT_ID, "iPhone 15",
            new BigDecimal("999.99"), "TWD", 5, "image.jpg", true);

        Cart existingCart = Cart.create(CUSTOMER_ID);
        existingCart.addItem(PRODUCT_ID, "iPhone 15", Money.of(new BigDecimal("999.99")), 4, "image.jpg");

        when(productServicePort.getProduct(PRODUCT_ID)).thenReturn(Optional.of(productInfo));
        when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(existingCart));

        // When & Then
        assertThrows(ProductNotAvailableException.class,
            () -> useCase.execute(CUSTOMER_ID, command));
    }
}
