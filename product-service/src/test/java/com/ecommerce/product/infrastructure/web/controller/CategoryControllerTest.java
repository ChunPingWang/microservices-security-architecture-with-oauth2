package com.ecommerce.product.infrastructure.web.controller;

import com.ecommerce.product.application.dto.CategoryDto;
import com.ecommerce.product.application.usecase.GetCategoriesUseCase;
import com.ecommerce.product.domain.model.CategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetCategoriesUseCase getCategoriesUseCase;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(getCategoriesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCategories_shouldReturnAllCategories() throws Exception {
        // Given
        CategoryDto category1 = createCategory("Electronics");
        CategoryDto category2 = createCategory("Clothing");

        when(getCategoriesUseCase.execute()).thenReturn(List.of(category1, category2));

        // When & Then
        mockMvc.perform(get("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[1].name").value("Clothing"))
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCategories_shouldReturnEmptyListWhenNoCategories() throws Exception {
        // Given
        when(getCategoriesUseCase.execute()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCategoryTree_shouldReturnHierarchicalCategories() throws Exception {
        // Given
        CategoryDto child = createCategoryWithChildren("Smartphones", List.of());
        CategoryDto parent = createCategoryWithChildren("Electronics", List.of(child));

        when(getCategoriesUseCase.executeAsTree()).thenReturn(List.of(parent));

        // When & Then
        mockMvc.perform(get("/v1/categories/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[0].children[0].name").value("Smartphones"));
    }

    @Test
    void getCategoryTree_shouldReturnMultipleRootCategories() throws Exception {
        // Given
        CategoryDto root1 = createCategoryWithChildren("Electronics", List.of());
        CategoryDto root2 = createCategoryWithChildren("Clothing", List.of());

        when(getCategoriesUseCase.executeAsTree()).thenReturn(List.of(root1, root2));

        // When & Then
        mockMvc.perform(get("/v1/categories/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[1].name").value("Clothing"))
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCategoryTree_shouldHandleDeepNesting() throws Exception {
        // Given
        CategoryDto level3 = createCategoryWithChildren("Laptops", List.of());
        CategoryDto level2 = createCategoryWithChildren("Computers", List.of(level3));
        CategoryDto level1 = createCategoryWithChildren("Electronics", List.of(level2));

        when(getCategoriesUseCase.executeAsTree()).thenReturn(List.of(level1));

        // When & Then
        mockMvc.perform(get("/v1/categories/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[0].children[0].name").value("Computers"))
            .andExpect(jsonPath("$[0].children[0].children[0].name").value("Laptops"));
    }

    private CategoryDto createCategory(String name) {
        return new CategoryDto(
            CategoryId.generate().asString(),
            name,
            "Description for " + name,
            null,
            0,
            true,
            null
        );
    }

    private CategoryDto createCategoryWithChildren(String name, List<CategoryDto> children) {
        return new CategoryDto(
            CategoryId.generate().asString(),
            name,
            "Description for " + name,
            null,
            0,
            true,
            children
        );
    }
}
