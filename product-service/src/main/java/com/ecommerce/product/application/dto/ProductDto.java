package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product DTO for API responses.
 */
public record ProductDto(
        String id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        String currency,
        int availableStock,
        String categoryId,
        ProductStatus status,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId().asString(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getAvailableStock(),
                product.getCategoryId().asString(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
