package com.ecommerce.customer.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

import java.time.Instant;

/**
 * Event: Customer account has been locked.
 */
public class CustomerLockedEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Customer";

    private final String email;
    private final Instant lockedUntil;

    public CustomerLockedEvent(String customerId, String email, Instant lockedUntil) {
        super(customerId, AGGREGATE_TYPE);
        this.email = email;
        this.lockedUntil = lockedUntil;
    }

    public String getCustomerId() {
        return getAggregateId();
    }

    public String getEmail() {
        return email;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
