package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.dto.ProductInfo;
import com.ecommerce.order.domain.port.ProductServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductServiceAdapter implements ProductServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceAdapter.class);

    private final ProductServiceClient productServiceClient;

    public ProductServiceAdapter(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @Override
    public Optional<ProductInfo> getProduct(String productId) {
        try {
            ProductInfo product = productServiceClient.getProduct(productId);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isProductAvailable(String productId, int requestedQuantity) {
        return getProduct(productId)
            .map(product -> product.inStock() && product.availableStock() >= requestedQuantity)
            .orElse(false);
    }
}
