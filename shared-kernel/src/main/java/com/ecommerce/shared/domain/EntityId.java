package com.ecommerce.shared.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for Entity IDs in DDD.
 * Uses UUID as the underlying identifier type.
 *
 * @param <T> The concrete entity ID type
 */
public abstract class EntityId<T extends EntityId<T>> implements Serializable, Comparable<T> {

    private final UUID value;

    protected EntityId() {
        this.value = UUID.randomUUID();
    }

    protected EntityId(UUID value) {
        this.value = Objects.requireNonNull(value, "ID value cannot be null");
    }

    protected EntityId(String value) {
        Objects.requireNonNull(value, "ID value cannot be null");
        this.value = UUID.fromString(value);
    }

    public UUID getValue() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId<?> entityId = (EntityId<?>) o;
        return Objects.equals(value, entityId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value + "]";
    }

    @Override
    public int compareTo(T other) {
        return this.value.compareTo(other.getValue());
    }
}
