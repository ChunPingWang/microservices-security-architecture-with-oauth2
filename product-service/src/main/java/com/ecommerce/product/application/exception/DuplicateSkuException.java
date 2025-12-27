package com.ecommerce.product.application.exception;

/**
 * Exception thrown when a duplicate SKU is detected.
 */
public class DuplicateSkuException extends RuntimeException {

    private final String sku;

    public DuplicateSkuException(String sku) {
        super("SKU already exists: " + sku);
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}
