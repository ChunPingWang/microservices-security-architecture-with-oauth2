package com.ecommerce.order.application.usecase;

import com.ecommerce.order.domain.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class ClearCartUseCase {

    private final CartRepository cartRepository;

    public ClearCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void execute(String customerId) {
        cartRepository.delete(customerId);
    }
}
