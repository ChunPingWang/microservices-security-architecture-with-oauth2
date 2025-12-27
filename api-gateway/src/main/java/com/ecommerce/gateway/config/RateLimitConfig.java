package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration for API Gateway.
 * Uses Redis for distributed rate limiting.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Default rate limiter configuration.
     * replenishRate: Number of requests per second allowed
     * burstCapacity: Maximum burst capacity
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        // 10 requests per second, burst up to 20
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Strict rate limiter for sensitive endpoints like login.
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        // 5 requests per second, burst up to 10
        return new RedisRateLimiter(5, 10, 1);
    }

    /**
     * Relaxed rate limiter for public read endpoints.
     */
    @Bean
    public RedisRateLimiter relaxedRateLimiter() {
        // 50 requests per second, burst up to 100
        return new RedisRateLimiter(50, 100, 1);
    }

    /**
     * Key resolver based on client IP address.
     * Used when user is not authenticated.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Key resolver based on authenticated user ID.
     * Falls back to IP if not authenticated.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just("user:" + userId);
            }
            // Fallback to IP
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Key resolver combining API path and user/IP.
     * Provides more granular rate limiting per endpoint.
     */
    @Bean
    public KeyResolver pathUserKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            String key;
            if (userId != null) {
                key = "user:" + userId + ":" + path;
            } else {
                String ip = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
                key = "ip:" + ip + ":" + path;
            }
            return Mono.just(key);
        };
    }
}
