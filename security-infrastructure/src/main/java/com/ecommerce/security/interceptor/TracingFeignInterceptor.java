package com.ecommerce.security.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Feign RequestInterceptor that propagates distributed tracing headers (B3).
 * Ensures trace context is maintained across service calls.
 */
public class TracingFeignInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TracingFeignInterceptor.class);

    private static final List<String> TRACING_HEADERS = List.of(
            "X-B3-TraceId",
            "X-B3-SpanId",
            "X-B3-ParentSpanId",
            "X-B3-Sampled",
            "X-B3-Flags",
            "X-Request-Id",
            "X-Correlation-Id"
    );

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.debug("No request context available for tracing header propagation");
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        for (String header : TRACING_HEADERS) {
            String value = request.getHeader(header);
            if (value != null) {
                template.header(header, value);
                log.trace("Propagated tracing header: {} = {}", header, value);
            }
        }
    }
}
