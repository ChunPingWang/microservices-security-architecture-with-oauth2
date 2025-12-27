package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductSummaryDto;
import com.ecommerce.product.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for searching products.
 */
@Service
@Transactional(readOnly = true)
public class SearchProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(SearchProductsUseCase.class);

    private final ProductRepository productRepository;

    public SearchProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Search products by keyword.
     */
    public Page<ProductSummaryDto> execute(String keyword, Pageable pageable) {
        log.debug("Searching products with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        return productRepository.searchActive(keyword.trim(), pageable)
                .map(ProductSummaryDto::from);
    }
}
