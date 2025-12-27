package com.ecommerce.product.infrastructure.persistence.adapter;

import com.ecommerce.product.domain.model.Category;
import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.repository.CategoryRepository;
import com.ecommerce.product.infrastructure.persistence.entity.CategoryEntity;
import com.ecommerce.product.infrastructure.persistence.repository.JpaCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements CategoryRepository using JPA.
 */
@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaRepository;

    public CategoryRepositoryAdapter(JpaCategoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = toEntity(category);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaRepository.findByParentIdIsNull().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findActiveRootCategories() {
        return jpaRepository.findByParentIdIsNullAndActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentId(CategoryId parentId) {
        return jpaRepository.findByParentId(parentId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findActiveByParentId(CategoryId parentId) {
        return jpaRepository.findByParentIdAndActiveTrue(parentId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByNameAndParentId(String name, CategoryId parentId) {
        return jpaRepository.existsByNameAndParentId(name,
                parentId != null ? parentId.getValue() : null);
    }

    @Override
    public void delete(Category category) {
        jpaRepository.deleteById(category.getId().getValue());
    }

    @Override
    public long countByParentId(CategoryId parentId) {
        return jpaRepository.countByParentId(parentId.getValue());
    }

    private CategoryEntity toEntity(Category category) {
        return new CategoryEntity(
                category.getId().getValue(),
                category.getName(),
                category.getDescription(),
                category.getParentId() != null ? category.getParentId().getValue() : null,
                category.getDisplayOrder(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private Category toDomain(CategoryEntity entity) {
        return new Category(
                CategoryId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getParentId() != null ? CategoryId.of(entity.getParentId()) : null,
                entity.getDisplayOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
