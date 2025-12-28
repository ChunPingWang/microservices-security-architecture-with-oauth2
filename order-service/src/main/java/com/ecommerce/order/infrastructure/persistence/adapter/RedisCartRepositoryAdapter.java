package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.order.infrastructure.persistence.model.CartData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisCartRepositoryAdapter implements CartRepository {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCartRepositoryAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        String key = buildKey(customerId);
        CartData cartData = (CartData) redisTemplate.opsForValue().get(key);

        if (cartData == null) {
            return Optional.empty();
        }

        return Optional.of(cartData.toDomain());
    }

    @Override
    public void save(Cart cart) {
        String key = buildKey(cart.getCustomerId());
        CartData cartData = CartData.from(cart);
        redisTemplate.opsForValue().set(key, cartData, CART_TTL);
    }

    @Override
    public void delete(String customerId) {
        String key = buildKey(customerId);
        redisTemplate.delete(key);
    }

    @Override
    public boolean exists(String customerId) {
        String key = buildKey(customerId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String buildKey(String customerId) {
        return CART_KEY_PREFIX + customerId;
    }
}
