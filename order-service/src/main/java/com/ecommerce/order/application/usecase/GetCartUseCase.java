package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class GetCartUseCase {

    private final CartRepository cartRepository;

    public GetCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartDto execute(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> Cart.create(customerId));

        return CartDto.from(cart);
    }
}
