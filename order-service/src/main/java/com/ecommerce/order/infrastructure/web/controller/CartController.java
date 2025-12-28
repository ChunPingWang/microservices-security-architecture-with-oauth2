package com.ecommerce.order.infrastructure.web.controller;

import com.ecommerce.order.application.dto.AddCartItemCommand;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.UpdateCartItemCommand;
import com.ecommerce.order.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/cart")
public class CartController {

    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;

    public CartController(
            GetCartUseCase getCartUseCase,
            AddCartItemUseCase addCartItemUseCase,
            UpdateCartItemUseCase updateCartItemUseCase,
            RemoveCartItemUseCase removeCartItemUseCase,
            ClearCartUseCase clearCartUseCase) {
        this.getCartUseCase = getCartUseCase;
        this.addCartItemUseCase = addCartItemUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(Principal principal) {
        CartDto cart = getCartUseCase.execute(principal.getName());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            Principal principal,
            @Valid @RequestBody AddCartItemCommand command) {
        CartDto cart = addCartItemUseCase.execute(principal.getName(), command);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            Principal principal,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemCommand command) {
        CartDto cart = updateCartItemUseCase.execute(principal.getName(), productId, command);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(
            Principal principal,
            @PathVariable String productId) {
        CartDto cart = removeCartItemUseCase.execute(principal.getName(), productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Principal principal) {
        clearCartUseCase.execute(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
