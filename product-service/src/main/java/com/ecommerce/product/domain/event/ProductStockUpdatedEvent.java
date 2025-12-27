package com.ecommerce.product.domain.event;

import com.ecommerce.shared.domain.DomainEvent;

/**
 * Event raised when product stock is updated.
 */
public class ProductStockUpdatedEvent extends DomainEvent {

    private final String productId;
    private final int previousQuantity;
    private final int newQuantity;

    public ProductStockUpdatedEvent(String productId, int previousQuantity, int newQuantity) {
        super(productId, "Product");
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public int getChange() {
        return newQuantity - previousQuantity;
    }
}
