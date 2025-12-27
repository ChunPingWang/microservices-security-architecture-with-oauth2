package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.model.Category;

import java.util.List;

/**
 * Category DTO for API responses.
 */
public record CategoryDto(
        String id,
        String name,
        String description,
        String parentId,
        int displayOrder,
        boolean active,
        List<CategoryDto> children
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(
                category.getId().asString(),
                category.getName(),
                category.getDescription(),
                category.getParentId() != null ? category.getParentId().asString() : null,
                category.getDisplayOrder(),
                category.isActive(),
                null
        );
    }

    public static CategoryDto from(Category category, List<CategoryDto> children) {
        return new CategoryDto(
                category.getId().asString(),
                category.getName(),
                category.getDescription(),
                category.getParentId() != null ? category.getParentId().asString() : null,
                category.getDisplayOrder(),
                category.isActive(),
                children
        );
    }
}
