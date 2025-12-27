package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository Port.
 */
public interface ProductRepository {

    /**
     * Save a product.
     */
    Product save(Product product);

    /**
     * Find product by ID.
     */
    Optional<Product> findById(ProductId id);

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all products with pagination.
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * Find products by status.
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Find products by category.
     */
    Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable);

    /**
     * Find products by category and status.
     */
    Page<Product> findByCategoryIdAndStatus(CategoryId categoryId, ProductStatus status, Pageable pageable);

    /**
     * Search products by name or description.
     */
    Page<Product> search(String keyword, Pageable pageable);

    /**
     * Search active products by keyword.
     */
    Page<Product> searchActive(String keyword, Pageable pageable);

    /**
     * Find products by IDs.
     */
    List<Product> findByIds(List<ProductId> ids);

    /**
     * Check if SKU exists.
     */
    boolean existsBySku(String sku);

    /**
     * Delete a product.
     */
    void delete(Product product);

    /**
     * Count products by category.
     */
    long countByCategoryId(CategoryId categoryId);
}
