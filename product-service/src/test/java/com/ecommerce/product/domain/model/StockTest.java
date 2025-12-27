package com.ecommerce.product.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Test
    void of_shouldCreateStockWithQuantity() {
        Stock stock = Stock.of(100);

        assertEquals(100, stock.getQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
    }

    @Test
    void of_shouldThrowExceptionForNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> Stock.of(-1));
    }

    @Test
    void ofWithReserved_shouldCreateStockWithReservedQuantity() {
        Stock stock = Stock.of(100, 20);

        assertEquals(100, stock.getQuantity());
        assertEquals(20, stock.getReservedQuantity());
        assertEquals(80, stock.getAvailableQuantity());
    }

    @Test
    void ofWithReserved_shouldThrowExceptionWhenReservedExceedsQuantity() {
        assertThrows(IllegalArgumentException.class, () -> Stock.of(100, 150));
    }

    @Test
    void reserve_shouldReduceAvailableQuantity() {
        Stock stock = Stock.of(100);
        Stock reserved = stock.reserve(30);

        assertEquals(100, reserved.getQuantity());
        assertEquals(30, reserved.getReservedQuantity());
        assertEquals(70, reserved.getAvailableQuantity());
    }

    @Test
    void reserve_shouldThrowExceptionWhenInsufficientStock() {
        Stock stock = Stock.of(100);
        Stock reserved = stock.reserve(80);

        assertThrows(IllegalStateException.class, () -> reserved.reserve(30));
    }

    @Test
    void release_shouldIncreaseAvailableQuantity() {
        Stock stock = Stock.of(100, 30);
        Stock released = stock.release(10);

        assertEquals(100, released.getQuantity());
        assertEquals(20, released.getReservedQuantity());
        assertEquals(80, released.getAvailableQuantity());
    }

    @Test
    void release_shouldThrowExceptionWhenReleasingMoreThanReserved() {
        Stock stock = Stock.of(100, 30);

        assertThrows(IllegalStateException.class, () -> stock.release(50));
    }

    @Test
    void deduct_shouldReduceTotalAndReservedQuantity() {
        Stock stock = Stock.of(100, 30);
        Stock deducted = stock.deduct(20);

        assertEquals(80, deducted.getQuantity());
        assertEquals(10, deducted.getReservedQuantity());
        assertEquals(70, deducted.getAvailableQuantity());
    }

    @Test
    void deduct_shouldThrowExceptionWhenDeductingMoreThanReserved() {
        Stock stock = Stock.of(100, 30);

        assertThrows(IllegalStateException.class, () -> stock.deduct(50));
    }

    @Test
    void add_shouldIncreaseQuantity() {
        Stock stock = Stock.of(100);
        Stock added = stock.add(50);

        assertEquals(150, added.getQuantity());
        assertEquals(0, added.getReservedQuantity());
        assertEquals(150, added.getAvailableQuantity());
    }

    @Test
    void add_shouldThrowExceptionForNegativeAmount() {
        Stock stock = Stock.of(100);

        assertThrows(IllegalArgumentException.class, () -> stock.add(-10));
    }

    @Test
    void add_shouldThrowExceptionForZeroAmount() {
        Stock stock = Stock.of(100);

        assertThrows(IllegalArgumentException.class, () -> stock.add(0));
    }

    @Test
    void hasAvailable_shouldReturnTrueWhenSufficientStock() {
        Stock stock = Stock.of(100);

        assertTrue(stock.hasAvailable(100));
        assertTrue(stock.hasAvailable(50));
    }

    @Test
    void hasAvailable_shouldReturnFalseWhenInsufficientStock() {
        Stock stock = Stock.of(100);

        assertFalse(stock.hasAvailable(150));
    }

    @Test
    void hasAvailable_shouldConsiderReservedQuantity() {
        Stock stock = Stock.of(100, 60);

        assertTrue(stock.hasAvailable(40));
        assertFalse(stock.hasAvailable(50));
    }

    @Test
    void isAvailable_shouldReturnTrueWhenAvailableQuantityPositive() {
        Stock stock = Stock.of(100);
        assertTrue(stock.isAvailable());
    }

    @Test
    void isAvailable_shouldReturnFalseWhenAllReserved() {
        Stock stock = Stock.of(100, 100);
        assertFalse(stock.isAvailable());
    }

    @Test
    void zero_shouldCreateEmptyStock() {
        Stock stock = Stock.zero();

        assertEquals(0, stock.getQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertFalse(stock.isAvailable());
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        Stock stock1 = Stock.of(100, 20);
        Stock stock2 = Stock.of(100, 20);

        assertEquals(stock1, stock2);
        assertEquals(stock1.hashCode(), stock2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues() {
        Stock stock1 = Stock.of(100);
        Stock stock2 = Stock.of(200);

        assertNotEquals(stock1, stock2);
    }
}
