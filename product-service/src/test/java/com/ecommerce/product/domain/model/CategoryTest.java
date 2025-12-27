package com.ecommerce.product.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void create_shouldCreateCategory() {
        Category category = Category.create("Electronics", "Electronic products", null);

        assertNotNull(category.getId());
        assertEquals("Electronics", category.getName());
        assertEquals("Electronic products", category.getDescription());
        assertNull(category.getParentId());
        assertEquals(0, category.getDisplayOrder());
        assertTrue(category.isActive());
    }

    @Test
    void create_shouldCreateSubCategory() {
        CategoryId parentId = CategoryId.generate();
        Category category = Category.create("Smartphones", "Mobile phones", parentId);

        assertNotNull(category.getId());
        assertEquals("Smartphones", category.getName());
        assertEquals(parentId, category.getParentId());
        assertTrue(category.isActive());
    }

    @Test
    void create_shouldThrowExceptionForNullName() {
        assertThrows(NullPointerException.class, () ->
            Category.create(null, "Description", null));
    }

    @Test
    void create_shouldThrowExceptionForEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
            Category.create("  ", "Description", null));
    }

    @Test
    void createRoot_shouldCreateRootCategory() {
        Category category = Category.createRoot("Electronics", "Electronic products");

        assertNotNull(category.getId());
        assertEquals("Electronics", category.getName());
        assertNull(category.getParentId());
        assertTrue(category.isRoot());
    }

    @Test
    void constructor_shouldReconstituteCategory() {
        CategoryId id = CategoryId.generate();
        CategoryId parentId = CategoryId.generate();
        Instant now = Instant.now();

        Category category = new Category(
            id, "Electronics", "Electronic products",
            parentId, 5, false, now, now
        );

        assertEquals(id, category.getId());
        assertEquals("Electronics", category.getName());
        assertEquals(parentId, category.getParentId());
        assertEquals(5, category.getDisplayOrder());
        assertFalse(category.isActive());
    }

    @Test
    void update_shouldUpdateNameAndDescription() {
        Category category = Category.create("Electronics", "Description", null);

        category.update("Consumer Electronics", "New description");

        assertEquals("Consumer Electronics", category.getName());
        assertEquals("New description", category.getDescription());
    }

    @Test
    void update_shouldNotUpdateNameIfNullOrEmpty() {
        Category category = Category.create("Electronics", "Description", null);

        category.update(null, "New description");
        assertEquals("Electronics", category.getName());

        category.update("  ", "Another description");
        assertEquals("Electronics", category.getName());
    }

    @Test
    void moveTo_shouldChangeParent() {
        Category category = Category.create("Electronics", "Description", null);
        CategoryId newParentId = CategoryId.generate();

        category.moveTo(newParentId);

        assertEquals(newParentId, category.getParentId());
        assertFalse(category.isRoot());
    }

    @Test
    void moveTo_shouldThrowExceptionWhenMovingToSelf() {
        CategoryId id = CategoryId.generate();
        Instant now = Instant.now();
        Category category = new Category(id, "Electronics", "Description", null, 0, true, now, now);

        assertThrows(IllegalArgumentException.class, () -> category.moveTo(id));
    }

    @Test
    void setDisplayOrder_shouldChangeOrder() {
        Category category = Category.create("Electronics", "Description", null);

        category.setDisplayOrder(10);

        assertEquals(10, category.getDisplayOrder());
    }

    @Test
    void activate_shouldSetActiveTrue() {
        CategoryId id = CategoryId.generate();
        Instant now = Instant.now();
        Category category = new Category(id, "Electronics", "Description", null, 0, false, now, now);

        category.activate();

        assertTrue(category.isActive());
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        Category category = Category.create("Electronics", "Description", null);

        category.deactivate();

        assertFalse(category.isActive());
    }

    @Test
    void isRoot_shouldReturnTrueForRootCategory() {
        Category category = Category.create("Electronics", "Description", null);

        assertTrue(category.isRoot());
    }

    @Test
    void isRoot_shouldReturnFalseForSubCategory() {
        CategoryId parentId = CategoryId.generate();
        Category category = Category.create("Smartphones", "Description", parentId);

        assertFalse(category.isRoot());
    }
}
