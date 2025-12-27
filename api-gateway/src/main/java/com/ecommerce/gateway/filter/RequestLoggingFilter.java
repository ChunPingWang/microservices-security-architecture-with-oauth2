package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter for request logging and tracing.
 * Adds request ID and logs all incoming requests.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        // Generate or use existing request ID
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        String method = mutatedRequest.getMethod().name();
        String path = mutatedRequest.getPath().value();
        String clientIp = getClientIp(mutatedRequest);

        log.info("Incoming request: {} {} from {} [requestId={}]",
                method, path, clientIp, requestId);

        final String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    Long start = exchange.getAttribute(START_TIME_ATTR);
                    long duration = start != null ? System.currentTimeMillis() - start : 0;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info("Request completed: {} {} - {} in {}ms [requestId={}]",
                            method, path, statusCode, duration, finalRequestId);
                })
                .doOnError(throwable -> {
                    Long start = exchange.getAttribute(START_TIME_ATTR);
                    long duration = start != null ? System.currentTimeMillis() - start : 0;
                    log.error("Request failed: {} {} - {} in {}ms [requestId={}]",
                            method, path, throwable.getMessage(), duration, finalRequestId);
                });
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check for forwarded headers (when behind load balancer)
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
