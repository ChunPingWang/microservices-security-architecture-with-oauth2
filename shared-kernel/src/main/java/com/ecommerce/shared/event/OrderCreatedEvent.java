package com.ecommerce.shared.event;

import com.ecommerce.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Domain Event: Order has been created.
 */
public class OrderCreatedEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Order";

    private final String customerId;
    private final BigDecimal totalAmount;
    private final String currency;
    private final List<OrderItemData> items;

    public OrderCreatedEvent(String orderId, String customerId, BigDecimal totalAmount,
                             String currency, List<OrderItemData> items) {
        super(orderId, AGGREGATE_TYPE);
        this.customerId = Objects.requireNonNull(customerId);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.currency = Objects.requireNonNull(currency);
        this.items = List.copyOf(items);
    }

    public String getOrderId() {
        return getAggregateId();
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public List<OrderItemData> getItems() {
        return items;
    }

    /**
     * Data transfer object for order items within the event.
     */
    public record OrderItemData(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
