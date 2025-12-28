package com.ecommerce.order.application.exception;

public class CartItemNotFoundException extends RuntimeException {

    private final String productId;

    public CartItemNotFoundException(String productId) {
        super("Cart item not found for product: " + productId);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
