package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.ProductInfo;
import com.ecommerce.order.application.dto.UpdateCartItemCommand;
import com.ecommerce.order.application.exception.CartItemNotFoundException;
import com.ecommerce.order.application.exception.ProductNotAvailableException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.port.ProductServicePort;
import com.ecommerce.order.domain.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateCartItemUseCase {

    private final CartRepository cartRepository;
    private final ProductServicePort productServicePort;

    public UpdateCartItemUseCase(CartRepository cartRepository, ProductServicePort productServicePort) {
        this.cartRepository = cartRepository;
        this.productServicePort = productServicePort;
    }

    public CartDto execute(String customerId, String productId, UpdateCartItemCommand command) {
        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new CartItemNotFoundException(productId));

        // Check if item exists in cart
        if (!cart.containsProduct(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        // Validate stock availability for new quantity
        ProductInfo productInfo = productServicePort.getProduct(productId)
            .orElseThrow(() -> new ProductNotAvailableException(productId, "Product not found"));

        if (command.quantity() > productInfo.availableStock()) {
            throw new ProductNotAvailableException(productId,
                "Requested quantity exceeds available stock. Available: " + productInfo.availableStock());
        }

        // Update quantity
        cart.updateItemQuantity(productId, command.quantity());

        // Save cart
        cartRepository.save(cart);

        return CartDto.from(cart);
    }
}
