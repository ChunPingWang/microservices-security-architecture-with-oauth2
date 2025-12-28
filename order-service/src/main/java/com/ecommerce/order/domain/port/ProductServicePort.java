package com.ecommerce.order.domain.port;

import com.ecommerce.order.application.dto.ProductInfo;

import java.util.Optional;

public interface ProductServicePort {

    Optional<ProductInfo> getProduct(String productId);

    boolean isProductAvailable(String productId, int requestedQuantity);
}
