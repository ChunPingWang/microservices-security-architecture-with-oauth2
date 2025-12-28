package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.model.Cart;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByCustomerId(String customerId);

    void save(Cart cart);

    void delete(String customerId);

    boolean exists(String customerId);
}
