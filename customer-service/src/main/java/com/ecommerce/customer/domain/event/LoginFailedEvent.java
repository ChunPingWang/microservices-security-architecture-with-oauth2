package com.ecommerce.customer.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

/**
 * Event: Customer login failed.
 */
public class LoginFailedEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Customer";

    private final String email;
    private final int failedAttempts;

    public LoginFailedEvent(String customerId, String email, int failedAttempts) {
        super(customerId, AGGREGATE_TYPE);
        this.email = email;
        this.failedAttempts = failedAttempts;
    }

    public String getCustomerId() {
        return getAggregateId();
    }

    public String getEmail() {
        return email;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }
}
