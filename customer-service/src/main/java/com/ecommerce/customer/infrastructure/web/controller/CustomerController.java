package com.ecommerce.customer.infrastructure.web.controller;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.usecase.GetCustomerProfileUseCase;
import com.ecommerce.security.context.CurrentUser;
import com.ecommerce.security.context.CurrentUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for customer profile endpoints.
 */
@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;

    public CustomerController(GetCustomerProfileUseCase getCustomerProfileUseCase) {
        this.getCustomerProfileUseCase = getCustomerProfileUseCase;
    }

    /**
     * Get current customer profile.
     * GET /v1/customers/me
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerDto> getMyProfile(@CurrentUser CurrentUserContext currentUser) {
        log.debug("Getting profile for current user: {}", currentUser.getUserId().orElse("unknown"));

        String customerId = currentUser.requireUserId();
        CustomerDto customer = getCustomerProfileUseCase.execute(customerId);

        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer by ID (admin only).
     * GET /v1/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable String id) {
        log.debug("Getting customer by ID: {}", id);

        CustomerDto customer = getCustomerProfileUseCase.execute(id);
        return ResponseEntity.ok(customer);
    }
}
