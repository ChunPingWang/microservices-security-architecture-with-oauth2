package com.ecommerce.shared.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for Domain Events in DDD.
 * Domain Events represent something that happened in the domain.
 */
public abstract class DomainEvent implements Serializable {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String aggregateType;
    private final int version;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this(aggregateId, aggregateType, 1);
    }

    protected DomainEvent(String aggregateId, String aggregateType, int version) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.aggregateId = Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "Aggregate type cannot be null");
        this.version = version;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public int getVersion() {
        return version;
    }

    /**
     * Returns the event type name, typically the simple class name.
     */
    @JsonIgnore
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventId=" + eventId +
                ", occurredOn=" + occurredOn +
                ", aggregateId='" + aggregateId + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", version=" + version +
                '}';
    }
}
