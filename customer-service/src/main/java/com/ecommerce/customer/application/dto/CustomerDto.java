package com.ecommerce.customer.application.dto;

import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;

import java.time.Instant;

/**
 * Customer DTO for API responses.
 */
public record CustomerDto(
        String id,
        String email,
        String name,
        CustomerStatus status,
        Instant createdAt
) {
    public static CustomerDto from(Customer customer) {
        return new CustomerDto(
                customer.getId().asString(),
                customer.getEmail().getValue(),
                customer.getName(),
                customer.getStatus(),
                customer.getCreatedAt()
        );
    }
}
