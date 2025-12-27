package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Route configuration for API Gateway.
 * Defines routing rules to downstream microservices.
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Customer Service Routes
                .route("customer-service-auth", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("customer-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://customer-service"))

                .route("customer-service", r -> r
                        .path("/api/v1/customers/**", "/api/customers/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("customer-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://customer-service"))

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/v1/products/**", "/api/v1/categories/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("product-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://product-service"))

                // Order Service Routes
                .route("order-service-cart", r -> r
                        .path("/api/v1/cart/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://order-service"))

                .route("order-service-orders", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://order-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://payment-service"))

                // Logistics Service Routes
                .route("logistics-service", r -> r
                        .path("/api/v1/shipments/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("logistics-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://logistics-service"))

                // Sales Service Routes
                .route("sales-service", r -> r
                        .path("/api/v1/promotions/**", "/api/v1/coupons/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("sales-service-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://sales-service"))

                // Admin Portal Routes
                .route("admin-portal", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("admin-portal-cb")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                        )
                        .uri("lb://admin-portal"))

                .build();
    }
}
