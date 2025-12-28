package com.ecommerce.order.domain.model;

import com.ecommerce.shared.vo.Money;

import java.io.Serializable;
import java.util.Objects;

public class CartItem implements Serializable {

    private static final int MAX_QUANTITY = 99;
    private static final int MIN_QUANTITY = 1;

    private final String productId;
    private final String productName;
    private final Money unitPrice;
    private int quantity;
    private final String imageUrl;

    private CartItem(String productId, String productName, Money unitPrice, int quantity, String imageUrl) {
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        this.productName = Objects.requireNonNull(productName, "ProductName cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice cannot be null");
        this.imageUrl = imageUrl;
        validateAndSetQuantity(quantity);
    }

    public static CartItem create(String productId, String productName, Money unitPrice, int quantity, String imageUrl) {
        return new CartItem(productId, productName, unitPrice, quantity, imageUrl);
    }

    public void updateQuantity(int newQuantity) {
        validateAndSetQuantity(newQuantity);
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        validateAndSetQuantity(this.quantity + amount);
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        validateAndSetQuantity(this.quantity - amount);
    }

    private void validateAndSetQuantity(int quantity) {
        if (quantity < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }
        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }
        this.quantity = quantity;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(productId, cartItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "CartItem{" +
            "productId='" + productId + '\'' +
            ", productName='" + productName + '\'' +
            ", quantity=" + quantity +
            ", subtotal=" + getSubtotal() +
            '}';
    }
}
