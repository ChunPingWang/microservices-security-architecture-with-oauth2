package com.ecommerce.product.application.exception;

/**
 * Exception thrown when a category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    private final String categoryId;

    public CategoryNotFoundException(String categoryId) {
        super("Category not found: " + categoryId);
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
