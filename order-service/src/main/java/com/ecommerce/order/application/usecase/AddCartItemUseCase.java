package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.AddCartItemCommand;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.ProductInfo;
import com.ecommerce.order.application.exception.ProductNotAvailableException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.port.ProductServicePort;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.shared.vo.Money;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
public class AddCartItemUseCase {

    private final CartRepository cartRepository;
    private final ProductServicePort productServicePort;

    public AddCartItemUseCase(CartRepository cartRepository, ProductServicePort productServicePort) {
        this.cartRepository = cartRepository;
        this.productServicePort = productServicePort;
    }

    public CartDto execute(String customerId, AddCartItemCommand command) {
        // Get product info from product-service
        ProductInfo productInfo = productServicePort.getProduct(command.productId())
            .orElseThrow(() -> new ProductNotAvailableException(command.productId(), "Product not found"));

        // Validate product availability
        if (!productInfo.inStock()) {
            throw new ProductNotAvailableException(command.productId(), "Product is out of stock");
        }

        if (productInfo.availableStock() < command.quantity()) {
            throw new ProductNotAvailableException(command.productId(),
                "Requested quantity exceeds available stock. Available: " + productInfo.availableStock());
        }

        // Get or create cart
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> Cart.create(customerId));

        // Check if adding would exceed available stock
        int currentQuantityInCart = cart.getItem(command.productId())
            .map(item -> item.getQuantity())
            .orElse(0);
        int totalRequested = currentQuantityInCart + command.quantity();

        if (totalRequested > productInfo.availableStock()) {
            throw new ProductNotAvailableException(command.productId(),
                "Total quantity would exceed available stock. In cart: " + currentQuantityInCart +
                ", Requesting: " + command.quantity() +
                ", Available: " + productInfo.availableStock());
        }

        // Add item to cart
        Money unitPrice = Money.of(productInfo.price(), Currency.getInstance(productInfo.currency()));
        cart.addItem(
            productInfo.id(),
            productInfo.name(),
            unitPrice,
            command.quantity(),
            productInfo.imageUrl()
        );

        // Save cart
        cartRepository.save(cart);

        return CartDto.from(cart);
    }
}
