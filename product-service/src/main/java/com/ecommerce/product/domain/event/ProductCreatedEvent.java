package com.ecommerce.product.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

import java.math.BigDecimal;

/**
 * Event raised when a new product is created.
 */
public class ProductCreatedEvent extends DomainEvent {

    private final String productId;
    private final String name;
    private final BigDecimal price;
    private final String currency;
    private final int initialStock;

    public ProductCreatedEvent(String productId, String name, BigDecimal price,
                                String currency, int initialStock) {
        super(productId, "Product");
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.initialStock = initialStock;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getInitialStock() {
        return initialStock;
    }
}
