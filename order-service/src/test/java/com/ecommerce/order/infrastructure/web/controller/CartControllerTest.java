package com.ecommerce.order.infrastructure.web.controller;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.CartItemDto;
import com.ecommerce.order.application.exception.CartItemNotFoundException;
import com.ecommerce.order.application.exception.ProductNotAvailableException;
import com.ecommerce.order.application.usecase.*;
import com.ecommerce.order.infrastructure.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetCartUseCase getCartUseCase;

    @Mock
    private AddCartItemUseCase addCartItemUseCase;

    @Mock
    private UpdateCartItemUseCase updateCartItemUseCase;

    @Mock
    private RemoveCartItemUseCase removeCartItemUseCase;

    @Mock
    private ClearCartUseCase clearCartUseCase;

    private ObjectMapper objectMapper;
    private Principal testPrincipal;

    @BeforeEach
    void setUp() {
        CartController controller = new CartController(
            getCartUseCase, addCartItemUseCase, updateCartItemUseCase,
            removeCartItemUseCase, clearCartUseCase
        );

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        testPrincipal = () -> "customer-123";
    }

    @Test
    void getCart_shouldReturnCart() throws Exception {
        // Given
        CartDto cart = createCartDto();
        when(getCartUseCase.execute("customer-123")).thenReturn(cart);

        // When & Then
        mockMvc.perform(get("/v1/cart")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value("customer-123"))
            .andExpect(jsonPath("$.totalItemCount").value(2));
    }

    @Test
    void addItem_shouldAddItemToCart() throws Exception {
        // Given
        CartDto cart = createCartDto();
        when(addCartItemUseCase.execute(eq("customer-123"), any())).thenReturn(cart);

        String requestBody = """
            {
                "productId": "product-001",
                "quantity": 2
            }
            """;

        // When & Then
        mockMvc.perform(post("/v1/cart/items")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value("customer-123"));
    }

    @Test
    void addItem_shouldReturn400WhenProductNotAvailable() throws Exception {
        // Given
        when(addCartItemUseCase.execute(eq("customer-123"), any()))
            .thenThrow(new ProductNotAvailableException("product-001", "Out of stock"));

        String requestBody = """
            {
                "productId": "product-001",
                "quantity": 2
            }
            """;

        // When & Then
        mockMvc.perform(post("/v1/cart/items")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("PRODUCT_NOT_AVAILABLE"));
    }

    @Test
    void updateItemQuantity_shouldUpdateQuantity() throws Exception {
        // Given
        CartDto cart = createCartDto();
        when(updateCartItemUseCase.execute(eq("customer-123"), eq("product-001"), any()))
            .thenReturn(cart);

        String requestBody = """
            {
                "quantity": 5
            }
            """;

        // When & Then
        mockMvc.perform(put("/v1/cart/items/product-001")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value("customer-123"));
    }

    @Test
    void updateItemQuantity_shouldReturn404WhenItemNotFound() throws Exception {
        // Given
        when(updateCartItemUseCase.execute(eq("customer-123"), eq("product-001"), any()))
            .thenThrow(new CartItemNotFoundException("product-001"));

        String requestBody = """
            {
                "quantity": 5
            }
            """;

        // When & Then
        mockMvc.perform(put("/v1/cart/items/product-001")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
    }

    @Test
    void removeItem_shouldRemoveItem() throws Exception {
        // Given
        CartDto cart = createEmptyCartDto();
        when(removeCartItemUseCase.execute("customer-123", "product-001")).thenReturn(cart);

        // When & Then
        mockMvc.perform(delete("/v1/cart/items/product-001")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void clearCart_shouldClearCart() throws Exception {
        // Given
        doNothing().when(clearCartUseCase).execute("customer-123");

        // When & Then
        mockMvc.perform(delete("/v1/cart")
                .principal(testPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    private CartDto createCartDto() {
        CartItemDto item = new CartItemDto(
            "product-001",
            "iPhone 15",
            new BigDecimal("999.99"),
            "TWD",
            2,
            new BigDecimal("1999.98"),
            "image.jpg"
        );

        return new CartDto(
            "cart-123",
            "customer-123",
            List.of(item),
            new BigDecimal("1999.98"),
            "TWD",
            2,
            1,
            Instant.now()
        );
    }

    private CartDto createEmptyCartDto() {
        return new CartDto(
            "cart-123",
            "customer-123",
            Collections.emptyList(),
            BigDecimal.ZERO,
            "TWD",
            0,
            0,
            Instant.now()
        );
    }
}
