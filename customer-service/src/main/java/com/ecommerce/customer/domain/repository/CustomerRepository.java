package com.ecommerce.customer.domain.repository;

import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.shared.vo.Email;

import java.util.Optional;

/**
 * Customer Repository Port (Domain Interface).
 * Defines the contract for customer persistence operations.
 */
public interface CustomerRepository {

    /**
     * Save a customer (create or update).
     */
    Customer save(Customer customer);

    /**
     * Find customer by ID.
     */
    Optional<Customer> findById(CustomerId id);

    /**
     * Find customer by email.
     */
    Optional<Customer> findByEmail(Email email);

    /**
     * Check if email already exists.
     */
    boolean existsByEmail(Email email);

    /**
     * Delete a customer.
     */
    void delete(Customer customer);
}
