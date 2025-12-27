package com.ecommerce.product.infrastructure.persistence.repository;

import com.ecommerce.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for CategoryEntity.
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByActiveTrue();

    List<CategoryEntity> findByParentIdIsNull();

    List<CategoryEntity> findByParentIdIsNullAndActiveTrue();

    List<CategoryEntity> findByParentId(UUID parentId);

    List<CategoryEntity> findByParentIdAndActiveTrue(UUID parentId);

    boolean existsByNameAndParentId(String name, UUID parentId);

    long countByParentId(UUID parentId);
}
