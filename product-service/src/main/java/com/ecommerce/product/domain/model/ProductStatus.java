package com.ecommerce.product.domain.model;

/**
 * Product status enumeration.
 */
public enum ProductStatus {
    /**
     * Product is active and available for sale.
     */
    ACTIVE,

    /**
     * Product is inactive (not shown to customers).
     */
    INACTIVE,

    /**
     * Product is out of stock.
     */
    OUT_OF_STOCK,

    /**
     * Product has been discontinued.
     */
    DISCONTINUED
}
