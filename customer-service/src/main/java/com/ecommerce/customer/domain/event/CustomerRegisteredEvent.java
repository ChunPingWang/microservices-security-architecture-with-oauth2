package com.ecommerce.customer.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

/**
 * Event: Customer has registered.
 */
public class CustomerRegisteredEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Customer";

    private final String email;
    private final String name;

    public CustomerRegisteredEvent(String customerId, String email, String name) {
        super(customerId, AGGREGATE_TYPE);
        this.email = email;
        this.name = name;
    }

    public String getCustomerId() {
        return getAggregateId();
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
