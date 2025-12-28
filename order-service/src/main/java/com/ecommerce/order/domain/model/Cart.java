package com.ecommerce.order.domain.model;

import com.ecommerce.shared.domain.AggregateRoot;
import com.ecommerce.shared.vo.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class Cart extends AggregateRoot<CartId> implements Serializable {

    private static final int MAX_ITEMS = 50;

    private final CartId id;
    private final String customerId;
    private final Map<String, CartItem> items;
    private Instant lastModifiedAt;

    private Cart(CartId id, String customerId) {
        this.id = Objects.requireNonNull(id, "CartId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId cannot be null");
        this.items = new LinkedHashMap<>();
        this.lastModifiedAt = Instant.now();
    }

    @Override
    public CartId getId() {
        return id;
    }

    public static Cart create(String customerId) {
        CartId cartId = CartId.fromCustomerId(customerId);
        return new Cart(cartId, customerId);
    }

    public static Cart restore(CartId id, String customerId, List<CartItem> items, Instant lastModifiedAt) {
        Cart cart = new Cart(id, customerId);
        items.forEach(item -> cart.items.put(item.getProductId(), item));
        cart.lastModifiedAt = lastModifiedAt;
        return cart;
    }

    public void addItem(String productId, String productName, Money unitPrice, int quantity, String imageUrl) {
        if (items.size() >= MAX_ITEMS && !items.containsKey(productId)) {
            throw new IllegalStateException("Cart cannot have more than " + MAX_ITEMS + " different items");
        }

        CartItem existingItem = items.get(productId);
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            CartItem newItem = CartItem.create(productId, productName, unitPrice, quantity, imageUrl);
            items.put(productId, newItem);
        }
        updateLastModified();
    }

    public void updateItemQuantity(String productId, int newQuantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product not found in cart: " + productId);
        }
        item.updateQuantity(newQuantity);
        updateLastModified();
    }

    public void removeItem(String productId) {
        CartItem removed = items.remove(productId);
        if (removed == null) {
            throw new IllegalArgumentException("Product not found in cart: " + productId);
        }
        updateLastModified();
    }

    public void clear() {
        items.clear();
        updateLastModified();
    }

    public Money calculateTotal() {
        return items.values().stream()
            .map(CartItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public int getTotalItemCount() {
        return items.values().stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    public int getDistinctItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean containsProduct(String productId) {
        return items.containsKey(productId);
    }

    public Optional<CartItem> getItem(String productId) {
        return Optional.ofNullable(items.get(productId));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    private void updateLastModified() {
        this.lastModifiedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Cart{" +
            "id=" + getId().asString() +
            ", customerId='" + customerId + '\'' +
            ", itemCount=" + getDistinctItemCount() +
            ", total=" + calculateTotal() +
            '}';
    }
}
