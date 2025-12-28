package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.model.CartItem;

import java.math.BigDecimal;

public record CartItemDto(
    String productId,
    String productName,
    BigDecimal unitPrice,
    String currency,
    int quantity,
    BigDecimal subtotal,
    String imageUrl
) {
    public static CartItemDto from(CartItem item) {
        return new CartItemDto(
            item.getProductId(),
            item.getProductName(),
            item.getUnitPrice().getAmount(),
            item.getUnitPrice().getCurrency().getCurrencyCode(),
            item.getQuantity(),
            item.getSubtotal().getAmount(),
            item.getImageUrl()
        );
    }
}
