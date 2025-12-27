package com.ecommerce.customer.domain.model;

import com.ecommerce.shared.domain.EntityId;

import java.util.UUID;

/**
 * Customer Entity ID.
 */
public class CustomerId extends EntityId<CustomerId> {

    public CustomerId() {
        super();
    }

    public CustomerId(UUID value) {
        super(value);
    }

    public CustomerId(String value) {
        super(value);
    }

    public static CustomerId generate() {
        return new CustomerId();
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}
