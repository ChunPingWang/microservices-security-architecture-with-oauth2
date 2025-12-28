package com.ecommerce.order.application.exception;

public class ProductNotAvailableException extends RuntimeException {

    private final String productId;

    public ProductNotAvailableException(String productId) {
        super("Product is not available: " + productId);
        this.productId = productId;
    }

    public ProductNotAvailableException(String productId, String reason) {
        super("Product is not available: " + productId + ". Reason: " + reason);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
