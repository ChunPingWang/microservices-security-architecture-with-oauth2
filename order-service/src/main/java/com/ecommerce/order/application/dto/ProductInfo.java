package com.ecommerce.order.application.dto;

import java.math.BigDecimal;

public record ProductInfo(
    String id,
    String name,
    BigDecimal price,
    String currency,
    int availableStock,
    String imageUrl,
    boolean inStock
) {}
