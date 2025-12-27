package com.ecommerce.product.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Category Entity.
 * Represents a product category with hierarchical support.
 */
public class Category {

    private final CategoryId id;
    private String name;
    private String description;
    private CategoryId parentId;
    private int displayOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Create a new category.
     */
    public static Category create(String name, String description, CategoryId parentId) {
        Objects.requireNonNull(name, "Category name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        Category category = new Category(
                CategoryId.generate(),
                name.trim(),
                description,
                parentId,
                0,
                true,
                Instant.now(),
                Instant.now()
        );
        return category;
    }

    /**
     * Create a root category (no parent).
     */
    public static Category createRoot(String name, String description) {
        return create(name, description, null);
    }

    /**
     * Constructor for reconstitution from persistence.
     */
    public Category(CategoryId id, String name, String description, CategoryId parentId,
                    int displayOrder, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Update category details.
     */
    public void update(String name, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * Move category to a new parent.
     */
    public void moveTo(CategoryId newParentId) {
        if (newParentId != null && newParentId.equals(this.id)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        this.parentId = newParentId;
        this.updatedAt = Instant.now();
    }

    /**
     * Set display order.
     */
    public void setDisplayOrder(int order) {
        this.displayOrder = order;
        this.updatedAt = Instant.now();
    }

    /**
     * Activate category.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivate category.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Check if this is a root category.
     */
    public boolean isRoot() {
        return parentId == null;
    }

    // Getters
    public CategoryId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CategoryId getParentId() {
        return parentId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
