package com.ecommerce.product.infrastructure.web.controller;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSummaryDto;
import com.ecommerce.product.application.usecase.GetProductDetailUseCase;
import com.ecommerce.product.application.usecase.GetProductListUseCase;
import com.ecommerce.product.application.usecase.SearchProductsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for product endpoints.
 * Public endpoints - no authentication required for browsing.
 */
@RestController
@RequestMapping("/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final GetProductListUseCase getProductListUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    public ProductController(GetProductListUseCase getProductListUseCase,
                             GetProductDetailUseCase getProductDetailUseCase,
                             SearchProductsUseCase searchProductsUseCase) {
        this.getProductListUseCase = getProductListUseCase;
        this.getProductDetailUseCase = getProductDetailUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
    }

    /**
     * Get all products with pagination.
     * GET /v1/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductSummaryDto>> getProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.debug("Getting products, page: {}", pageable.getPageNumber());

        Page<ProductSummaryDto> products = getProductListUseCase.execute(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID.
     * GET /v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        log.debug("Getting product by ID: {}", id);

        ProductDto product = getProductDetailUseCase.execute(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Search products by keyword.
     * GET /v1/products/search?q={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductSummaryDto>> searchProducts(
            @RequestParam("q") String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.debug("Searching products with keyword: {}", keyword);

        Page<ProductSummaryDto> products = searchProductsUseCase.execute(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category.
     * GET /v1/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductSummaryDto>> getProductsByCategory(
            @PathVariable String categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.debug("Getting products for category: {}", categoryId);

        Page<ProductSummaryDto> products = getProductListUseCase.executeByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
}
