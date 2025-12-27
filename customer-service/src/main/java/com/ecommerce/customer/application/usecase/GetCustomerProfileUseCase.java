package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.exception.CustomerNotFoundException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerId;
import com.ecommerce.customer.domain.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting customer profile.
 */
@Service
public class GetCustomerProfileUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCustomerProfileUseCase.class);

    private final CustomerRepository customerRepository;

    public GetCustomerProfileUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDto execute(String customerId) {
        log.debug("Getting profile for customer: {}", customerId);

        CustomerId id = CustomerId.of(customerId);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return CustomerDto.from(customer);
    }
}
