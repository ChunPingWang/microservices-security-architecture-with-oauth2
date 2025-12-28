package com.ecommerce.order.domain.model;

import com.ecommerce.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private static final String PRODUCT_ID = "product-001";
    private static final String PRODUCT_NAME = "iPhone 15";
    private static final Money UNIT_PRICE = Money.of(new BigDecimal("999.99"));
    private static final String IMAGE_URL = "https://example.com/image.jpg";

    @Test
    void create_shouldCreateCartItem() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertNotNull(item);
        assertEquals(PRODUCT_ID, item.getProductId());
        assertEquals(PRODUCT_NAME, item.getProductName());
        assertEquals(UNIT_PRICE, item.getUnitPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(IMAGE_URL, item.getImageUrl());
    }

    @Test
    void create_shouldThrowWhenQuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
            CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 0, IMAGE_URL));
    }

    @Test
    void create_shouldThrowWhenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
            CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, -1, IMAGE_URL));
    }

    @Test
    void create_shouldThrowWhenQuantityExceeds99() {
        assertThrows(IllegalArgumentException.class, () ->
            CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 100, IMAGE_URL));
    }

    @Test
    void create_shouldAllowMaxQuantity99() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 99, IMAGE_URL);
        assertEquals(99, item.getQuantity());
    }

    @Test
    void updateQuantity_shouldUpdateQuantity() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        item.updateQuantity(5);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void updateQuantity_shouldThrowWhenExceeds99() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertThrows(IllegalArgumentException.class, () -> item.updateQuantity(100));
    }

    @Test
    void increaseQuantity_shouldIncreaseQuantity() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        item.increaseQuantity(3);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void increaseQuantity_shouldThrowWhenAmountIsZeroOrNegative() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(-1));
    }

    @Test
    void decreaseQuantity_shouldDecreaseQuantity() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 5, IMAGE_URL);

        item.decreaseQuantity(2);

        assertEquals(3, item.getQuantity());
    }

    @Test
    void decreaseQuantity_shouldThrowWhenResultIsZero() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertThrows(IllegalArgumentException.class, () -> item.decreaseQuantity(2));
    }

    @Test
    void getSubtotal_shouldCalculateCorrectly() {
        CartItem item = CartItem.create(PRODUCT_ID, PRODUCT_NAME, Money.of(new BigDecimal("100.00")), 3, IMAGE_URL);

        Money subtotal = item.getSubtotal();

        assertEquals(new BigDecimal("300.00"), subtotal.getAmount());
    }

    @Test
    void equals_shouldBeEqualByProductId() {
        CartItem item1 = CartItem.create(PRODUCT_ID, "Name 1", UNIT_PRICE, 1, IMAGE_URL);
        CartItem item2 = CartItem.create(PRODUCT_ID, "Name 2", Money.of(new BigDecimal("50.00")), 5, null);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldNotBeEqualForDifferentProductId() {
        CartItem item1 = CartItem.create("product-001", PRODUCT_NAME, UNIT_PRICE, 1, IMAGE_URL);
        CartItem item2 = CartItem.create("product-002", PRODUCT_NAME, UNIT_PRICE, 1, IMAGE_URL);

        assertNotEquals(item1, item2);
    }
}
