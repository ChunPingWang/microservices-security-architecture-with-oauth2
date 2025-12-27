package com.ecommerce.shared.event;

import com.ecommerce.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event: Payment has been completed successfully.
 */
public class PaymentCompletedEvent extends DomainEvent {

    private static final String AGGREGATE_TYPE = "Payment";

    private final String orderId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentMethod paymentMethod;
    private final String transactionId;
    private final Instant completedAt;

    public PaymentCompletedEvent(String paymentId, String orderId, BigDecimal amount,
                                  String currency, PaymentMethod paymentMethod,
                                  String transactionId) {
        super(paymentId, AGGREGATE_TYPE);
        this.orderId = Objects.requireNonNull(orderId);
        this.amount = Objects.requireNonNull(amount);
        this.currency = Objects.requireNonNull(currency);
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.transactionId = transactionId;
        this.completedAt = Instant.now();
    }

    public String getPaymentId() {
        return getAggregateId();
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Supported payment methods.
     */
    public enum PaymentMethod {
        CREDIT_CARD,
        BANK_TRANSFER,
        LINE_PAY,
        APPLE_PAY
    }
}
