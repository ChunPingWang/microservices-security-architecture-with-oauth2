package com.ecommerce.security.config;

import com.ecommerce.security.context.CurrentUserArgumentResolver;
import com.ecommerce.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Auto-configuration for security infrastructure.
 * This configuration is automatically loaded when the security-infrastructure module is included.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(basePackages = "com.ecommerce.security")
public class SecurityAutoConfiguration implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public SecurityAutoConfiguration(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
