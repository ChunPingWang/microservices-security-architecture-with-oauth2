package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.dto.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClientFallback.class);

    @Override
    public ProductInfo getProduct(String productId) {
        log.warn("Fallback: Unable to fetch product info for productId: {}", productId);
        return null;
    }
}
