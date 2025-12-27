package com.ecommerce.product.domain.model;

import com.ecommerce.shared.domain.ValueObject;

import java.util.Objects;

/**
 * Stock Value Object.
 * Represents the quantity of a product in stock.
 */
public class Stock extends ValueObject<Stock> {

    private final int quantity;
    private final int reservedQuantity;

    private Stock(int quantity, int reservedQuantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reservedQuantity > quantity) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed total quantity");
        }
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    public static Stock of(int quantity) {
        return new Stock(quantity, 0);
    }

    public static Stock of(int quantity, int reservedQuantity) {
        return new Stock(quantity, reservedQuantity);
    }

    public static Stock zero() {
        return new Stock(0, 0);
    }

    /**
     * Get the total quantity in stock.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Get the reserved quantity.
     */
    public int getReservedQuantity() {
        return reservedQuantity;
    }

    /**
     * Get the available quantity (total - reserved).
     */
    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * Check if stock is available.
     */
    public boolean isAvailable() {
        return getAvailableQuantity() > 0;
    }

    /**
     * Check if the requested quantity is available.
     */
    public boolean hasAvailable(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

    /**
     * Reserve a quantity of stock.
     */
    public Stock reserve(int quantityToReserve) {
        if (quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be positive");
        }
        if (!hasAvailable(quantityToReserve)) {
            throw new IllegalStateException("Insufficient stock available");
        }
        return new Stock(quantity, reservedQuantity + quantityToReserve);
    }

    /**
     * Release reserved stock.
     */
    public Stock release(int quantityToRelease) {
        if (quantityToRelease <= 0) {
            throw new IllegalArgumentException("Quantity to release must be positive");
        }
        if (quantityToRelease > reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved");
        }
        return new Stock(quantity, reservedQuantity - quantityToRelease);
    }

    /**
     * Deduct sold quantity from stock.
     */
    public Stock deduct(int quantityToDeduct) {
        if (quantityToDeduct <= 0) {
            throw new IllegalArgumentException("Quantity to deduct must be positive");
        }
        if (quantityToDeduct > reservedQuantity) {
            throw new IllegalStateException("Cannot deduct more than reserved");
        }
        return new Stock(quantity - quantityToDeduct, reservedQuantity - quantityToDeduct);
    }

    /**
     * Add stock quantity.
     */
    public Stock add(int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        return new Stock(quantity + quantityToAdd, reservedQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return quantity == stock.quantity && reservedQuantity == stock.reservedQuantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, reservedQuantity);
    }

    @Override
    public String toString() {
        return String.format("Stock{quantity=%d, reserved=%d, available=%d}",
                quantity, reservedQuantity, getAvailableQuantity());
    }
}
