package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.exception.CartItemNotFoundException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;

    public RemoveCartItemUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartDto execute(String customerId, String productId) {
        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new CartItemNotFoundException(productId));

        // Check if item exists
        if (!cart.containsProduct(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        // Remove item
        cart.removeItem(productId);

        // Save or delete cart based on whether it's empty
        if (cart.isEmpty()) {
            cartRepository.delete(customerId);
        } else {
            cartRepository.save(cart);
        }

        return CartDto.from(cart);
    }
}
