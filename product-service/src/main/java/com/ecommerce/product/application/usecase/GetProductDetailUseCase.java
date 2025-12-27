package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.exception.ProductNotFoundException;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting product details.
 */
@Service
@Transactional(readOnly = true)
public class GetProductDetailUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetProductDetailUseCase.class);

    private final ProductRepository productRepository;

    public GetProductDetailUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Get product by ID.
     */
    @Cacheable(value = "product", key = "#productId")
    public ProductDto execute(String productId) {
        log.debug("Getting product detail: {}", productId);

        return productRepository.findById(ProductId.of(productId))
                .map(ProductDto::from)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
