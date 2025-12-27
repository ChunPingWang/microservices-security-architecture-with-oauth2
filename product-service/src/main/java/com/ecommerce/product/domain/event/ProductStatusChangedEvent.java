package com.ecommerce.product.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

/**
 * Event raised when product status changes.
 */
public class ProductStatusChangedEvent extends DomainEvent {

    private final String productId;
    private final String previousStatus;
    private final String newStatus;

    public ProductStatusChangedEvent(String productId, String previousStatus, String newStatus) {
        super(productId, "Product");
        this.productId = productId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public String getProductId() {
        return productId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
