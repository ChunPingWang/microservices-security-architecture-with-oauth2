package com.ecommerce.order.domain.model;

import com.ecommerce.shared.domain.EntityId;

import java.util.Objects;
import java.util.UUID;

public class CartId extends EntityId<CartId> {

    private CartId(UUID value) {
        super(value);
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }

    public static CartId of(UUID value) {
        Objects.requireNonNull(value, "CartId value cannot be null");
        return new CartId(value);
    }

    public static CartId of(String value) {
        Objects.requireNonNull(value, "CartId value cannot be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("CartId value cannot be empty");
        }
        return new CartId(UUID.fromString(value));
    }

    public static CartId fromCustomerId(String customerId) {
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        return new CartId(UUID.nameUUIDFromBytes(customerId.getBytes()));
    }

    @Override
    public String toString() {
        return "CartId[" + getValue() + "]";
    }
}
