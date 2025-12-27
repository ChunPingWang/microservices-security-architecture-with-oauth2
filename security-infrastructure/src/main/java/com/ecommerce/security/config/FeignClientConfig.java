package com.ecommerce.security.config;

import com.ecommerce.security.interceptor.ServiceAuthInterceptor;
import com.ecommerce.security.interceptor.TracingFeignInterceptor;
import com.ecommerce.security.jwt.JwtTokenProvider;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Feign Client configuration for service-to-service authentication.
 * Add this configuration to your FeignClient annotations.
 *
 * Usage:
 * <pre>
 * @FeignClient(name = "payment-service", configuration = FeignClientConfig.class)
 * public interface PaymentServiceClient {
 *     // ...
 * }
 * </pre>
 */
public class FeignClientConfig {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Bean
    public RequestInterceptor serviceAuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        return new ServiceAuthInterceptor(jwtTokenProvider, serviceName);
    }

    @Bean
    public RequestInterceptor tracingInterceptor() {
        return new TracingFeignInterceptor();
    }
}
