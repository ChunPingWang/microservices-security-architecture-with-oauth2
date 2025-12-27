package com.ecommerce.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Command for creating a new product.
 */
public record CreateProductCommand(
        @NotBlank(message = "商品名稱不能為空")
        @Size(max = 200, message = "商品名稱最多200字")
        String name,

        @Size(max = 2000, message = "商品描述最多2000字")
        String description,

        @Size(max = 50, message = "SKU最多50字")
        String sku,

        @NotNull(message = "價格不能為空")
        @DecimalMin(value = "0", message = "價格不能為負數")
        BigDecimal price,

        @Min(value = 0, message = "庫存不能為負數")
        int initialStock,

        @NotBlank(message = "分類ID不能為空")
        String categoryId,

        String imageUrl
) {}
