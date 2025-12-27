package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.Category;
import com.ecommerce.product.domain.model.CategoryId;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository Port.
 */
public interface CategoryRepository {

    /**
     * Save a category.
     */
    Category save(Category category);

    /**
     * Find category by ID.
     */
    Optional<Category> findById(CategoryId id);

    /**
     * Find all categories.
     */
    List<Category> findAll();

    /**
     * Find all active categories.
     */
    List<Category> findAllActive();

    /**
     * Find root categories (no parent).
     */
    List<Category> findRootCategories();

    /**
     * Find active root categories.
     */
    List<Category> findActiveRootCategories();

    /**
     * Find child categories by parent ID.
     */
    List<Category> findByParentId(CategoryId parentId);

    /**
     * Find active child categories by parent ID.
     */
    List<Category> findActiveByParentId(CategoryId parentId);

    /**
     * Check if category exists.
     */
    boolean existsById(CategoryId id);

    /**
     * Check if category name exists under the same parent.
     */
    boolean existsByNameAndParentId(String name, CategoryId parentId);

    /**
     * Delete a category.
     */
    void delete(Category category);

    /**
     * Count child categories.
     */
    long countByParentId(CategoryId parentId);
}
