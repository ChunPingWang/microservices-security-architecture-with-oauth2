package com.ecommerce.product.infrastructure.web.controller;

import com.ecommerce.product.application.dto.ProductDto;
import com.ecommerce.product.application.dto.ProductSummaryDto;
import com.ecommerce.product.application.exception.ProductNotFoundException;
import com.ecommerce.product.application.usecase.GetProductDetailUseCase;
import com.ecommerce.product.application.usecase.GetProductListUseCase;
import com.ecommerce.product.application.usecase.SearchProductsUseCase;
import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.infrastructure.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetProductListUseCase getProductListUseCase;

    @Mock
    private GetProductDetailUseCase getProductDetailUseCase;

    @Mock
    private SearchProductsUseCase searchProductsUseCase;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(
            getProductListUseCase, getProductDetailUseCase, searchProductsUseCase
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(converter)
            .build();
    }

    @Test
    void getProducts_shouldReturnProductList() throws Exception {
        // Given
        ProductSummaryDto product = createProductSummary("Test Product");
        Page<ProductSummaryDto> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(getProductListUseCase.execute(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getProducts_withPagination_shouldWork() throws Exception {
        // Given
        Page<ProductSummaryDto> page = new PageImpl<>(List.of(), PageRequest.of(2, 20), 0);
        when(getProductListUseCase.execute(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/products")
                .param("page", "2")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getProductById_shouldReturnProductDetail() throws Exception {
        // Given
        String productId = ProductId.generate().asString();
        ProductDto product = createProductDto(productId, "iPhone 15");

        when(getProductDetailUseCase.execute(eq(productId))).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    void getProductById_shouldReturn404WhenNotFound() throws Exception {
        // Given
        String productId = ProductId.generate().asString();
        when(getProductDetailUseCase.execute(eq(productId)))
            .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(get("/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void searchProducts_shouldReturnMatchingProducts() throws Exception {
        // Given
        ProductSummaryDto product = createProductSummary("iPhone 15");
        Page<ProductSummaryDto> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(searchProductsUseCase.execute(eq("iPhone"), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/products/search")
                .param("q", "iPhone")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("iPhone 15"));
    }

    @Test
    void searchProducts_shouldReturnEmptyWhenNoMatch() throws Exception {
        // Given
        Page<ProductSummaryDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(searchProductsUseCase.execute(eq("NonExistent"), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/v1/products/search")
                .param("q", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getProductsByCategory_shouldReturnCategoryProducts() throws Exception {
        // Given
        String categoryId = CategoryId.generate().asString();
        ProductSummaryDto product = createProductSummary("Category Product");
        Page<ProductSummaryDto> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(getProductListUseCase.executeByCategory(eq(categoryId), any(Pageable.class)))
            .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/products/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Category Product"));
    }

    private ProductSummaryDto createProductSummary(String name) {
        return new ProductSummaryDto(
            ProductId.generate().asString(),
            name,
            new BigDecimal("999.99"),
            "TWD",
            100,
            "https://example.com/image.jpg",
            true
        );
    }

    private ProductDto createProductDto(String id, String name) {
        return new ProductDto(
            id,
            name,
            "Product description",
            "SKU-001",
            new BigDecimal("999.99"),
            "TWD",
            100,
            CategoryId.generate().asString(),
            ProductStatus.ACTIVE,
            "https://example.com/image.jpg",
            Instant.now(),
            Instant.now()
        );
    }
}
