package com.ecommerce.customer.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

/**
 * Event: Customer login succeeded.
 */
public class LoginSuccessEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Customer";

    private final String email;

    public LoginSuccessEvent(String customerId, String email) {
        super(customerId, AGGREGATE_TYPE);
        this.email = email;
    }

    public String getCustomerId() {
        return getAggregateId();
    }

    public String getEmail() {
        return email;
    }
}
