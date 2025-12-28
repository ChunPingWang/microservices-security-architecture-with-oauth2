package com.ecommerce.order.domain.model;

import com.ecommerce.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private static final String CUSTOMER_ID = "customer-123";
    private static final String PRODUCT_ID = "product-001";
    private static final String PRODUCT_NAME = "iPhone 15";
    private static final Money UNIT_PRICE = Money.of(new BigDecimal("999.99"));
    private static final String IMAGE_URL = "https://example.com/image.jpg";

    @BeforeEach
    void setUp() {
        cart = Cart.create(CUSTOMER_ID);
    }

    @Test
    void create_shouldCreateEmptyCart() {
        assertNotNull(cart);
        assertNotNull(cart.getId());
        assertEquals(CUSTOMER_ID, cart.getCustomerId());
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalItemCount());
        assertEquals(0, cart.getDistinctItemCount());
    }

    @Test
    void addItem_shouldAddNewItem() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertFalse(cart.isEmpty());
        assertEquals(1, cart.getDistinctItemCount());
        assertEquals(2, cart.getTotalItemCount());
        assertTrue(cart.containsProduct(PRODUCT_ID));
    }

    @Test
    void addItem_shouldIncreaseQuantityForExistingProduct() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 3, IMAGE_URL);

        assertEquals(1, cart.getDistinctItemCount());
        assertEquals(5, cart.getTotalItemCount());
    }

    @Test
    void addItem_shouldThrowWhenExceedingMaxItems() {
        for (int i = 0; i < 50; i++) {
            cart.addItem("product-" + i, "Product " + i, UNIT_PRICE, 1, IMAGE_URL);
        }

        assertThrows(IllegalStateException.class, () ->
            cart.addItem("product-51", "Product 51", UNIT_PRICE, 1, IMAGE_URL));
    }

    @Test
    void updateItemQuantity_shouldUpdateQuantity() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        cart.updateItemQuantity(PRODUCT_ID, 5);

        assertEquals(5, cart.getTotalItemCount());
    }

    @Test
    void updateItemQuantity_shouldThrowWhenProductNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            cart.updateItemQuantity("non-existent", 5));
    }

    @Test
    void removeItem_shouldRemoveItem() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);
        cart.addItem("product-002", "Product 2", UNIT_PRICE, 1, IMAGE_URL);

        cart.removeItem(PRODUCT_ID);

        assertEquals(1, cart.getDistinctItemCount());
        assertFalse(cart.containsProduct(PRODUCT_ID));
        assertTrue(cart.containsProduct("product-002"));
    }

    @Test
    void removeItem_shouldThrowWhenProductNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            cart.removeItem("non-existent"));
    }

    @Test
    void clear_shouldRemoveAllItems() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);
        cart.addItem("product-002", "Product 2", UNIT_PRICE, 1, IMAGE_URL);

        cart.clear();

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getDistinctItemCount());
        assertEquals(0, cart.getTotalItemCount());
    }

    @Test
    void calculateTotal_shouldReturnCorrectTotal() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, Money.of(new BigDecimal("100.00")), 2, IMAGE_URL);
        cart.addItem("product-002", "Product 2", Money.of(new BigDecimal("50.00")), 3, IMAGE_URL);

        Money total = cart.calculateTotal();

        assertEquals(new BigDecimal("350.00"), total.getAmount());
    }

    @Test
    void calculateTotal_shouldReturnZeroForEmptyCart() {
        Money total = cart.calculateTotal();

        assertTrue(total.isZero());
    }

    @Test
    void getItem_shouldReturnItemWhenExists() {
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 2, IMAGE_URL);

        assertTrue(cart.getItem(PRODUCT_ID).isPresent());
        assertEquals(PRODUCT_NAME, cart.getItem(PRODUCT_ID).get().getProductName());
    }

    @Test
    void getItem_shouldReturnEmptyWhenNotExists() {
        assertFalse(cart.getItem("non-existent").isPresent());
    }

    @Test
    void lastModifiedAt_shouldUpdateOnCartModification() throws InterruptedException {
        var initialTime = cart.getLastModifiedAt();

        Thread.sleep(10);
        cart.addItem(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 1, IMAGE_URL);

        assertTrue(cart.getLastModifiedAt().isAfter(initialTime));
    }
}
