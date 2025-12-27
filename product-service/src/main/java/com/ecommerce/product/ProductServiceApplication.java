package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Product Service Application.
 * Provides product catalog, search, and category management.
 */
@SpringBootApplication(scanBasePackages = {
        "com.ecommerce.product",
        "com.ecommerce.security"
})
@EnableCaching
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
