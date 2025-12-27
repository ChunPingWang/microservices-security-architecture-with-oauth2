package com.ecommerce.product.infrastructure.web.controller;

import com.ecommerce.product.application.dto.CategoryDto;
import com.ecommerce.product.application.usecase.GetCategoriesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for category endpoints.
 * Public endpoints - no authentication required.
 */
@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final GetCategoriesUseCase getCategoriesUseCase;

    public CategoryController(GetCategoriesUseCase getCategoriesUseCase) {
        this.getCategoriesUseCase = getCategoriesUseCase;
    }

    /**
     * Get all categories (flat list).
     * GET /v1/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        log.debug("Getting all categories");

        List<CategoryDto> categories = getCategoriesUseCase.execute();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get categories as a tree structure.
     * GET /v1/categories/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDto>> getCategoryTree() {
        log.debug("Getting category tree");

        List<CategoryDto> categoryTree = getCategoriesUseCase.executeAsTree();
        return ResponseEntity.ok(categoryTree);
    }
}
