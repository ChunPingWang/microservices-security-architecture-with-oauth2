package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductSummaryDto;
import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting product list.
 */
@Service
@Transactional(readOnly = true)
public class GetProductListUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetProductListUseCase.class);

    private final ProductRepository productRepository;

    public GetProductListUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Get all active products with pagination.
     */
    @Cacheable(value = "products", key = "'list:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProductSummaryDto> execute(Pageable pageable) {
        log.debug("Getting product list, page: {}", pageable.getPageNumber());

        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(ProductSummaryDto::from);
    }

    /**
     * Get products by category.
     */
    @Cacheable(value = "products", key = "'category:' + #categoryId + ':' + #pageable.pageNumber")
    public Page<ProductSummaryDto> executeByCategory(String categoryId, Pageable pageable) {
        log.debug("Getting products for category: {}", categoryId);

        return productRepository.findByCategoryIdAndStatus(
                        CategoryId.of(categoryId), ProductStatus.ACTIVE, pageable)
                .map(ProductSummaryDto::from);
    }
}
