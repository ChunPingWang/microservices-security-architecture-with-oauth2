package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "product-service",
    url = "${services.product-service.url:http://localhost:8082}",
    fallback = ProductServiceClientFallback.class
)
public interface ProductServiceClient {

    @GetMapping("/v1/products/{productId}")
    ProductInfo getProduct(@PathVariable("productId") String productId);
}
