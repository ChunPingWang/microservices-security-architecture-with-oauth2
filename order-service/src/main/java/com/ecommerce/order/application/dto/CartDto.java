package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.model.Cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartDto(
    String id,
    String customerId,
    List<CartItemDto> items,
    BigDecimal totalAmount,
    String currency,
    int totalItemCount,
    int distinctItemCount,
    Instant lastModifiedAt
) {
    public static CartDto from(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
            .map(CartItemDto::from)
            .toList();

        return new CartDto(
            cart.getId().asString(),
            cart.getCustomerId(),
            itemDtos,
            cart.calculateTotal().getAmount(),
            cart.calculateTotal().getCurrency().getCurrencyCode(),
            cart.getTotalItemCount(),
            cart.getDistinctItemCount(),
            cart.getLastModifiedAt()
        );
    }
}
