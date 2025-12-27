package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.CategoryDto;
import com.ecommerce.product.domain.model.Category;
import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for getting categories.
 */
@Service
@Transactional(readOnly = true)
public class GetCategoriesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCategoriesUseCase.class);

    private final CategoryRepository categoryRepository;

    public GetCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all categories as a flat list.
     */
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryDto> execute() {
        log.debug("Getting all categories");

        return categoryRepository.findAllActive().stream()
                .sorted(Comparator.comparing(Category::getDisplayOrder))
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Get categories as a tree structure.
     */
    @Cacheable(value = "categories", key = "'tree'")
    public List<CategoryDto> executeAsTree() {
        log.debug("Getting category tree");

        List<Category> rootCategories = categoryRepository.findActiveRootCategories();

        return rootCategories.stream()
                .sorted(Comparator.comparing(Category::getDisplayOrder))
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    private CategoryDto buildCategoryTree(Category category) {
        List<Category> children = categoryRepository.findActiveByParentId(category.getId());

        if (children.isEmpty()) {
            return CategoryDto.from(category);
        }

        List<CategoryDto> childDtos = children.stream()
                .sorted(Comparator.comparing(Category::getDisplayOrder))
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());

        return CategoryDto.from(category, childDtos);
    }
}
