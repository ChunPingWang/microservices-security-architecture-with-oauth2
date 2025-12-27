package com.ecommerce.customer.infrastructure.persistence.adapter;

import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.customer.domain.model.Password;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import com.ecommerce.customer.infrastructure.persistence.entity.CustomerEntity;
import com.ecommerce.customer.infrastructure.persistence.repository.JpaCustomerRepository;
import com.ecommerce.shared.vo.Email;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that implements CustomerRepository using JPA.
 */
@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository jpaRepository;

    public CustomerRepositoryAdapter(JpaCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = toEntity(customer);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public void delete(Customer customer) {
        jpaRepository.deleteById(customer.getId().getValue());
    }

    private CustomerEntity toEntity(Customer customer) {
        return new CustomerEntity(
                customer.getId().getValue(),
                customer.getEmail().getValue(),
                customer.getPassword().getHashedValue(),
                customer.getName(),
                customer.getStatus(),
                customer.getFailedLoginAttempts(),
                customer.getLockedUntil(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private Customer toDomain(CustomerEntity entity) {
        return new Customer(
                CustomerId.of(entity.getId()),
                Email.of(entity.getEmail()),
                Password.fromHash(entity.getPasswordHash()),
                entity.getName(),
                entity.getStatus(),
                entity.getFailedLoginAttempts(),
                entity.getLockedUntil(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
