package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.exception.ProductNotFoundException;
import com.ecommerce.product.domain.model.*;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProductDetailUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductDetailUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductDetailUseCase(productRepository);
    }

    @Test
    void execute_shouldReturnProductDetail() {
        // Given
        ProductId productId = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Product product = createTestProduct(productId, categoryId, "iPhone 15", "SKU-001");

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

        // When
        ProductDto result = useCase.execute(productId.asString());

        // Then
        assertNotNull(result);
        assertEquals(productId.asString(), result.id());
        assertEquals("iPhone 15", result.name());
        assertEquals("SKU-001", result.sku());
    }

    @Test
    void execute_shouldThrowExceptionWhenProductNotFound() {
        // Given
        String productId = ProductId.generate().asString();
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> useCase.execute(productId));
    }

    @Test
    void execute_shouldMapAllProductFields() {
        // Given
        ProductId productId = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Product product = createTestProduct(productId, categoryId, "MacBook Pro", "SKU-MAC");

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

        // When
        ProductDto result = useCase.execute(productId.asString());

        // Then
        assertEquals(productId.asString(), result.id());
        assertEquals("MacBook Pro", result.name());
        assertEquals("Product description", result.description());
        assertEquals("SKU-MAC", result.sku());
        assertEquals(100, result.availableStock());
        assertEquals(categoryId.asString(), result.categoryId());
        assertEquals(ProductStatus.ACTIVE, result.status());
    }

    @Test
    void execute_shouldCalculateAvailableQuantityCorrectly() {
        // Given
        ProductId productId = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(100, 30);
        Instant now = Instant.now();

        Product product = new Product(
            productId, "Test Product", "Description", "SKU-001",
            price, stock, categoryId, ProductStatus.ACTIVE, null,
            now, now
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

        // When
        ProductDto result = useCase.execute(productId.asString());

        // Then
        assertEquals(70, result.availableStock());
    }

    private Product createTestProduct(ProductId id, CategoryId categoryId, String name, String sku) {
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(100);
        Instant now = Instant.now();

        return new Product(id, name, "Product description", sku, price, stock, categoryId,
            ProductStatus.ACTIVE, "https://example.com/image.jpg", now, now);
    }
}
