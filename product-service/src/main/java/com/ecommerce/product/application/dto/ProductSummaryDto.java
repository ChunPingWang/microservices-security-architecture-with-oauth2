package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.model.Product;

import java.math.BigDecimal;

/**
 * Product summary DTO for list views.
 */
public record ProductSummaryDto(
        String id,
        String name,
        BigDecimal price,
        String currency,
        int availableStock,
        String imageUrl,
        boolean inStock
) {
    public static ProductSummaryDto from(Product product) {
        return new ProductSummaryDto(
                product.getId().asString(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getAvailableStock(),
                product.getImageUrl(),
                product.isAvailableForPurchase()
        );
    }
}
