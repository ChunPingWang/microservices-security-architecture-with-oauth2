package com.ecommerce.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Command for updating an existing product.
 */
public record UpdateProductCommand(
        @Size(max = 200, message = "商品名稱最多200字")
        String name,

        @Size(max = 2000, message = "商品描述最多2000字")
        String description,

        @Size(max = 50, message = "SKU最多50字")
        String sku,

        @DecimalMin(value = "0", message = "價格不能為負數")
        BigDecimal price,

        String categoryId,

        String imageUrl
) {}
