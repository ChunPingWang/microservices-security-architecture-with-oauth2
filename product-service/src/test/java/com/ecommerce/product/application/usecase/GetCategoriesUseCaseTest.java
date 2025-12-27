package com.ecommerce.product.application.usecase;

import com.ecommerce.product.application.dto.CategoryDto;
import com.ecommerce.product.domain.model.Category;
import com.ecommerce.product.domain.model.CategoryId;
import com.ecommerce.product.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetCategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    private GetCategoriesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCategoriesUseCase(categoryRepository);
    }

    @Test
    void execute_shouldReturnAllActiveCategories() {
        // Given
        Category cat1 = Category.create("Electronics", "Electronic products", null);
        Category cat2 = Category.create("Clothing", "Clothing items", null);

        when(categoryRepository.findAllActive()).thenReturn(List.of(cat1, cat2));

        // When
        List<CategoryDto> result = useCase.execute();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals("Clothing", result.get(1).name());
    }

    @Test
    void execute_shouldReturnEmptyListWhenNoCategories() {
        // Given
        when(categoryRepository.findAllActive()).thenReturn(List.of());

        // When
        List<CategoryDto> result = useCase.execute();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_shouldMapCategoryFieldsCorrectly() {
        // Given
        CategoryId parentId = CategoryId.generate();
        CategoryId id = CategoryId.generate();
        Instant now = Instant.now();
        Category category = new Category(
            id, "Smartphones", "Mobile phones and accessories",
            parentId, 5, true, now, now
        );

        when(categoryRepository.findAllActive()).thenReturn(List.of(category));

        // When
        List<CategoryDto> result = useCase.execute();

        // Then
        assertEquals(1, result.size());
        CategoryDto dto = result.get(0);
        assertEquals(id.asString(), dto.id());
        assertEquals("Smartphones", dto.name());
        assertEquals("Mobile phones and accessories", dto.description());
        assertEquals(parentId.asString(), dto.parentId());
        assertEquals(5, dto.displayOrder());
        assertTrue(dto.active());
    }

    @Test
    void executeAsTree_shouldReturnHierarchicalStructure() {
        // Given
        Category rootCategory = Category.create("Electronics", "Electronic products", null);
        CategoryId rootId = rootCategory.getId();
        Category childCategory = Category.create("Smartphones", "Mobile phones", rootId);

        when(categoryRepository.findActiveRootCategories()).thenReturn(List.of(rootCategory));
        when(categoryRepository.findActiveByParentId(rootId)).thenReturn(List.of(childCategory));
        when(categoryRepository.findActiveByParentId(childCategory.getId())).thenReturn(List.of());

        // When
        List<CategoryDto> result = useCase.executeAsTree();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertNotNull(result.get(0).children());
        assertEquals(1, result.get(0).children().size());
        assertEquals("Smartphones", result.get(0).children().get(0).name());
    }

    @Test
    void executeAsTree_shouldHandleMultipleRootCategories() {
        // Given
        Category category1 = Category.create("Electronics", "Description", null);
        Category category2 = Category.create("Clothing", "Description", null);

        when(categoryRepository.findActiveRootCategories()).thenReturn(List.of(category1, category2));
        when(categoryRepository.findActiveByParentId(category1.getId())).thenReturn(List.of());
        when(categoryRepository.findActiveByParentId(category2.getId())).thenReturn(List.of());

        // When
        List<CategoryDto> result = useCase.executeAsTree();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void executeAsTree_shouldHandleDeepNesting() {
        // Given
        Category cat1 = Category.create("Electronics", "Description", null);
        CategoryId level1Id = cat1.getId();
        Category cat2 = Category.create("Computers", "Description", level1Id);
        CategoryId level2Id = cat2.getId();
        Category cat3 = Category.create("Laptops", "Description", level2Id);

        when(categoryRepository.findActiveRootCategories()).thenReturn(List.of(cat1));
        when(categoryRepository.findActiveByParentId(level1Id)).thenReturn(List.of(cat2));
        when(categoryRepository.findActiveByParentId(level2Id)).thenReturn(List.of(cat3));
        when(categoryRepository.findActiveByParentId(cat3.getId())).thenReturn(List.of());

        // When
        List<CategoryDto> result = useCase.executeAsTree();

        // Then
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals(1, result.get(0).children().size());
        assertEquals("Computers", result.get(0).children().get(0).name());
        assertEquals(1, result.get(0).children().get(0).children().size());
        assertEquals("Laptops", result.get(0).children().get(0).children().get(0).name());
    }

    @Test
    void executeAsTree_shouldReturnEmptyListWhenNoRootCategories() {
        // Given
        when(categoryRepository.findActiveRootCategories()).thenReturn(List.of());

        // When
        List<CategoryDto> result = useCase.executeAsTree();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
