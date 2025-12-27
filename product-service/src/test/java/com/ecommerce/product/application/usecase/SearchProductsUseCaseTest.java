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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private SearchProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchProductsUseCase(productRepository);
    }

    @Test
    void execute_shouldSearchActiveProducts() {
        // Given
        String keyword = "iPhone";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = createTestProduct("iPhone 15", "SKU-IP15");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.searchActive(eq(keyword), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(keyword, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).name());
        verify(productRepository).searchActive(eq(keyword), eq(pageable));
    }

    @Test
    void execute_shouldReturnEmptyPageWhenNoMatch() {
        // Given
        String keyword = "NonExistentProduct";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.searchActive(eq(keyword), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(keyword, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void execute_shouldHandleMultipleResults() {
        // Given
        String keyword = "Phone";
        Pageable pageable = PageRequest.of(0, 10);
        Product product1 = createTestProduct("iPhone 15", "SKU-IP15");
        Product product2 = createTestProduct("Galaxy Phone", "SKU-GAL");
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productRepository.searchActive(eq(keyword), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(keyword, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void execute_shouldMapProductSummaryCorrectly() {
        // Given
        String keyword = "MacBook";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = createTestProduct("MacBook Pro", "SKU-MBP");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.searchActive(eq(keyword), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(keyword, pageable);

        // Then
        ProductSummaryDto dto = result.getContent().get(0);
        assertEquals("MacBook Pro", dto.name());
        assertEquals(100, dto.availableStock());
        assertTrue(dto.inStock());
    }

    @Test
    void execute_shouldHandlePagination() {
        // Given
        String keyword = "Test";
        Pageable pageable = PageRequest.of(1, 5); // Second page
        Product product = createTestProduct("Test Product", "SKU-TEST");
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 10);

        when(productRepository.searchActive(eq(keyword), any(Pageable.class)))
            .thenReturn(productPage);

        // When
        Page<ProductSummaryDto> result = useCase.execute(keyword, pageable);

        // Then
        assertEquals(10, result.getTotalElements());
        assertEquals(1, result.getNumber()); // Page 1 (0-indexed)
        assertEquals(5, result.getSize());
    }

    private Product createTestProduct(String name, String sku) {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        return Product.create(name, "Description", sku, price, 100, categoryId);
    }
}
