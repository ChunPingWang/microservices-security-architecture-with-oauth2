package com.ecommerce.product.infrastructure.persistence.repository;

import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ProductEntity.
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<ProductEntity> findByStatus(ProductStatus status, Pageable pageable);

    Page<ProductEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<ProductEntity> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProductEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.status = :status AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProductEntity> searchByStatus(@Param("keyword") String keyword,
                                        @Param("status") ProductStatus status,
                                        Pageable pageable);

    List<ProductEntity> findByIdIn(List<UUID> ids);

    long countByCategoryId(UUID categoryId);
}
