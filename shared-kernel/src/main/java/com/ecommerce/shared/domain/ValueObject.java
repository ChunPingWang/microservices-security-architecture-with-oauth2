package com.ecommerce.shared.domain;

import java.io.Serializable;

/**
 * Base class for Value Objects in DDD.
 * Value Objects are immutable and compared by their attribute values.
 *
 * @param <T> The concrete value object type
 */
public abstract class ValueObject<T extends ValueObject<T>> implements Serializable {

    /**
     * Value objects must implement equals based on their attributes.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Value objects must implement hashCode consistent with equals.
     */
    @Override
    public abstract int hashCode();

    /**
     * Value objects should provide a meaningful string representation.
     */
    @Override
    public abstract String toString();

    /**
     * Check if this value object is the same as another.
     *
     * @param other The other value object to compare
     * @return true if they are equal
     */
    public boolean sameAs(T other) {
        return this.equals(other);
    }
}
