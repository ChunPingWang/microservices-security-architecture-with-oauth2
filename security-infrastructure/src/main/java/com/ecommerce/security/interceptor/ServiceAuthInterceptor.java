package com.ecommerce.security.interceptor;

import com.ecommerce.security.jwt.JwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign RequestInterceptor that adds service authentication token
 * for internal service-to-service communication.
 */
public class ServiceAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuthInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final String serviceName;

    public ServiceAuthInterceptor(JwtTokenProvider jwtTokenProvider, String serviceName) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.serviceName = serviceName;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Generate a service token for internal calls
        String serviceToken = jwtTokenProvider.generateServiceToken(serviceName);
        template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + serviceToken);

        log.debug("Added service authentication token for: {}", serviceName);
    }
}
