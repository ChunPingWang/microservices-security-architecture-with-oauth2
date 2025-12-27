package com.ecommerce.shared.event;

import com.ecommerce.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event: Shipment status has changed.
 */
public class ShipmentStatusChangedEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Shipment";

    private final String orderId;
    private final ShipmentStatus previousStatus;
    private final ShipmentStatus newStatus;
    private final String location;
    private final String description;
    private final Instant statusChangedAt;

    public ShipmentStatusChangedEvent(String shipmentId, String orderId,
                                       ShipmentStatus previousStatus,
                                       ShipmentStatus newStatus,
                                       String location, String description) {
        super(shipmentId, AGGREGATE_TYPE);
        this.orderId = Objects.requireNonNull(orderId);
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus);
        this.location = location;
        this.description = description;
        this.statusChangedAt = Instant.now();
    }

    public String getShipmentId() {
        return getAggregateId();
    }

    public String getOrderId() {
        return orderId;
    }

    public ShipmentStatus getPreviousStatus() {
        return previousStatus;
    }

    public ShipmentStatus getNewStatus() {
        return newStatus;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStatusChangedAt() {
        return statusChangedAt;
    }

    /**
     * Shipment status values.
     */
    public enum ShipmentStatus {
        PREPARING,          // 准备中
        SHIPPED,            // 已出货
        IN_TRANSIT,         // 运送中
        OUT_FOR_DELIVERY,   // 配送中
        DELIVERED,          // 已送达
        FAILED,             // 配送失败
        RETURNED            // 已退回
    }
}
