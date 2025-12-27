package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.ProductSummaryDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProductListUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductListUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductListUseCase(productRepository);
    }

    @Test
    void execute_shouldReturnActiveProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Product product = createTestProduct("Test Product", "SKU-001");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).name());
    }

    @Test
    void execute_shouldReturnEmptyPageWhenNoProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void executeByCategory_shouldReturnProductsInCategory() {
        // Given
        CategoryId categoryId = CategoryId.generate();
        Pageable pageable = PageRequest.of(0, 10);
        Product product = createTestProduct("Category Product", "SKU-002");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByCategoryIdAndStatus(any(CategoryId.class), eq(ProductStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.executeByCategory(categoryId.asString(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Category Product", result.getContent().get(0).name());
    }

    @Test
    void execute_shouldMapProductFieldsCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Product product = createTestProduct("iPhone 15", "SKU-IPHONE");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(pageable);

        // Then
        ProductSummaryDto dto = result.getContent().get(0);
        assertEquals(product.getId().asString(), dto.id());
        assertEquals("iPhone 15", dto.name());
        assertEquals(100, dto.availableStock());
        assertTrue(dto.inStock());
    }

    private Product createTestProduct(String name, String sku) {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        return Product.create(name, "Description", sku, price, 100, categoryId);
    }
}
