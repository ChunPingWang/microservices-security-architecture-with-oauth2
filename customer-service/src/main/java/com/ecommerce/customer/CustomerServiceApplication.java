package com.ecommerce.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer Service Application.
 * Handles customer registration, authentication, and profile management.
 */
@SpringBootApplication(scanBasePackages = {
        "com.ecommerce.customer",
        "com.ecommerce.security"
})
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
