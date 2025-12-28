package com.ecommerce.order.infrastructure.persistence.model;

import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.model.CartId;
import com.ecommerce.order.domain.model.CartItem;
import com.ecommerce.shared.vo.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

public class CartData implements Serializable {

    private String id;
    private String customerId;
    private List<CartItemData> items;
    private Instant lastModifiedAt;

    public CartData() {}

    public CartData(String id, String customerId, List<CartItemData> items, Instant lastModifiedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static CartData from(Cart cart) {
        List<CartItemData> itemDataList = cart.getItems().stream()
            .map(CartItemData::from)
            .toList();

        return new CartData(
            cart.getId().asString(),
            cart.getCustomerId(),
            itemDataList,
            cart.getLastModifiedAt()
        );
    }

    public Cart toDomain() {
        List<CartItem> domainItems = items.stream()
            .map(CartItemData::toDomain)
            .toList();

        return Cart.restore(
            CartId.of(id),
            customerId,
            domainItems,
            lastModifiedAt
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CartItemData> getItems() {
        return items;
    }

    public void setItems(List<CartItemData> items) {
        this.items = items;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public static class CartItemData implements Serializable {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private String currency;
        private int quantity;
        private String imageUrl;

        public CartItemData() {}

        public CartItemData(String productId, String productName, BigDecimal unitPrice,
                            String currency, int quantity, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.currency = currency;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }

        public static CartItemData from(CartItem item) {
            return new CartItemData(
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency().getCurrencyCode(),
                item.getQuantity(),
                item.getImageUrl()
            );
        }

        public CartItem toDomain() {
            return CartItem.create(
                productId,
                productName,
                Money.of(unitPrice, Currency.getInstance(currency)),
                quantity,
                imageUrl
            );
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
