package com.ecommerce.product.domain.model;

import com.ecommerce.shared.domain.EntityId;

import java.util.UUID;

/**
 * Product Entity ID.
 */
public class ProductId extends EntityId<ProductId> {

    public ProductId() {
        super();
    }

    public ProductId(UUID value) {
        super(value);
    }

    public ProductId(String value) {
        super(value);
    }

    public static ProductId generate() {
        return new ProductId();
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}
