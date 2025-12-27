package com.ecommerce.product.domain.model;

import com.ecommerce.shared.domain.EntityId;

import java.util.UUID;

/**
 * Category Entity ID.
 */
public class CategoryId extends EntityId<CategoryId> {

    public CategoryId() {
        super();
    }

    public CategoryId(UUID value) {
        super(value);
    }

    public CategoryId(String value) {
        super(value);
    }

    public static CategoryId generate() {
        return new CategoryId();
    }

    public static CategoryId of(String value) {
        return new CategoryId(value);
    }

    public static CategoryId of(UUID value) {
        return new CategoryId(value);
    }
}
