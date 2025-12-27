package com.ecommerce.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for Aggregate Roots in DDD.
 * Aggregate Root is the entry point to an aggregate and ensures consistency.
 *
 * @param <ID> The type of the entity ID
 */
public abstract class AggregateRoot<ID extends EntityId<?>> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private int version = 0;

    /**
     * Returns the unique identifier of this aggregate.
     */
    public abstract ID getId();

    /**
     * Returns the current version of this aggregate (for optimistic locking).
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version (typically used by persistence layer).
     */
    protected void setVersion(int version) {
        this.version = version;
    }

    /**
     * Registers a domain event to be published.
     *
     * @param event The domain event to register
     */
    protected void registerEvent(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event cannot be null");
        this.domainEvents.add(event);
    }

    /**
     * Returns all registered domain events.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all registered domain events.
     * Typically called after events have been published.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Checks if there are any pending domain events.
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateRoot<?> that = (AggregateRoot<?>) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
