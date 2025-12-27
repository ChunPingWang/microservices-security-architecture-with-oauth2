package com.ecommerce.product.infrastructure.persistence.adapter;

import com.ecommerce.product.domain.model.*;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.infrastructure.persistence.entity.ProductEntity;
import com.ecommerce.product.infrastructure.persistence.repository.JpaProductRepository;
import com.ecommerce.shared.vo.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements ProductRepository using JPA.
 */
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    public ProductRepositoryAdapter(JpaProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable) {
        return jpaRepository.findByCategoryId(categoryId.getValue(), pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> findByCategoryIdAndStatus(CategoryId categoryId, ProductStatus status, Pageable pageable) {
        return jpaRepository.findByCategoryIdAndStatus(categoryId.getValue(), status, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> search(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Product> searchActive(String keyword, Pageable pageable) {
        return jpaRepository.searchByStatus(keyword, ProductStatus.ACTIVE, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<Product> findByIds(List<ProductId> ids) {
        List<java.util.UUID> uuids = ids.stream()
                .map(ProductId::getValue)
                .collect(Collectors.toList());
        return jpaRepository.findByIdIn(uuids).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public void delete(Product product) {
        jpaRepository.deleteById(product.getId().getValue());
    }

    @Override
    public long countByCategoryId(CategoryId categoryId) {
        return jpaRepository.countByCategoryId(categoryId.getValue());
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.getId().getValue(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getStock().getQuantity(),
                product.getStock().getReservedQuantity(),
                product.getCategoryId().getValue(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(
                ProductId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                Money.of(entity.getPrice(), Currency.getInstance(entity.getCurrency())),
                Stock.of(entity.getStockQuantity(), entity.getReservedQuantity()),
                CategoryId.of(entity.getCategoryId()),
                entity.getStatus(),
                entity.getImageUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
