package com.ecommerce.product.domain.model;

import com.ecommerce.product.domain.event.ProductCreatedEvent;
import com.ecommerce.product.domain.event.ProductStockUpdatedEvent;
import com.ecommerce.product.domain.event.ProductStatusChangedEvent;
import com.ecommerce.shared.domain.AggregateRoot;
import com.ecommerce.shared.vo.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * Product Aggregate Root.
 * Manages product information, pricing, and stock.
 */
public class Product extends AggregateRoot<ProductId> {

    private final ProductId id;
    private String name;
    private String description;
    private String sku;
    private Money price;
    private Stock stock;
    private CategoryId categoryId;
    private ProductStatus status;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Create a new product.
     */
    public static Product create(String name, String description, String sku,
                                  Money price, int initialStock, CategoryId categoryId) {
        Objects.requireNonNull(name, "Product name cannot be null");
        Objects.requireNonNull(price, "Product price cannot be null");
        Objects.requireNonNull(categoryId, "Category ID cannot be null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (price.isNegative()) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }

        ProductId id = ProductId.generate();
        Product product = new Product(
                id,
                name.trim(),
                description,
                sku,
                price,
                Stock.of(initialStock),
                categoryId,
                ProductStatus.ACTIVE,
                null,
                Instant.now(),
                Instant.now()
        );

        product.registerEvent(new ProductCreatedEvent(
                id.asString(),
                name,
                price.getAmount(),
                price.getCurrency().getCurrencyCode(),
                initialStock
        ));

        return product;
    }

    /**
     * Constructor for reconstitution from persistence.
     */
    public Product(ProductId id, String name, String description, String sku,
                   Money price, Stock stock, CategoryId categoryId,
                   ProductStatus status, String imageUrl,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Update product details.
     */
    public void updateDetails(String name, String description, String sku, String imageUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.sku = sku;
        this.imageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * Update product price.
     */
    public void updatePrice(Money newPrice) {
        Objects.requireNonNull(newPrice, "Price cannot be null");
        if (newPrice.isNegative()) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
        this.updatedAt = Instant.now();
    }

    /**
     * Update category.
     */
    public void updateCategory(CategoryId newCategoryId) {
        Objects.requireNonNull(newCategoryId, "Category ID cannot be null");
        this.categoryId = newCategoryId;
        this.updatedAt = Instant.now();
    }

    /**
     * Add stock quantity.
     */
    public void addStock(int quantity) {
        Stock oldStock = this.stock;
        this.stock = this.stock.add(quantity);
        this.updatedAt = Instant.now();

        // Update status if was out of stock
        if (this.status == ProductStatus.OUT_OF_STOCK && this.stock.isAvailable()) {
            this.status = ProductStatus.ACTIVE;
        }

        registerEvent(new ProductStockUpdatedEvent(
                id.asString(),
                oldStock.getQuantity(),
                this.stock.getQuantity()
        ));
    }

    /**
     * Reserve stock for an order.
     */
    public void reserveStock(int quantity) {
        this.stock = this.stock.reserve(quantity);
        this.updatedAt = Instant.now();
    }

    /**
     * Release reserved stock.
     */
    public void releaseStock(int quantity) {
        this.stock = this.stock.release(quantity);
        this.updatedAt = Instant.now();
    }

    /**
     * Deduct sold stock.
     */
    public void deductStock(int quantity) {
        Stock oldStock = this.stock;
        this.stock = this.stock.deduct(quantity);
        this.updatedAt = Instant.now();

        // Update status if out of stock
        if (!this.stock.isAvailable()) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }

        registerEvent(new ProductStockUpdatedEvent(
                id.asString(),
                oldStock.getQuantity(),
                this.stock.getQuantity()
        ));
    }

    /**
     * Activate the product.
     */
    public void activate() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot activate discontinued product");
        }
        ProductStatus oldStatus = this.status;
        this.status = this.stock.isAvailable() ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
        this.updatedAt = Instant.now();

        if (oldStatus != this.status) {
            registerEvent(new ProductStatusChangedEvent(
                    id.asString(), oldStatus.name(), this.status.name()));
        }
    }

    /**
     * Deactivate the product.
     */
    public void deactivate() {
        ProductStatus oldStatus = this.status;
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();

        if (oldStatus != this.status) {
            registerEvent(new ProductStatusChangedEvent(
                    id.asString(), oldStatus.name(), this.status.name()));
        }
    }

    /**
     * Discontinue the product.
     */
    public void discontinue() {
        ProductStatus oldStatus = this.status;
        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = Instant.now();

        if (oldStatus != this.status) {
            registerEvent(new ProductStatusChangedEvent(
                    id.asString(), oldStatus.name(), this.status.name()));
        }
    }

    /**
     * Check if the product is available for purchase.
     */
    public boolean isAvailableForPurchase() {
        return status == ProductStatus.ACTIVE && stock.isAvailable();
    }

    /**
     * Check if the requested quantity is available.
     */
    public boolean hasAvailableStock(int quantity) {
        return status == ProductStatus.ACTIVE && stock.hasAvailable(quantity);
    }

    // Getters
    @Override
    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public Money getPrice() {
        return price;
    }

    public Stock getStock() {
        return stock;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getAvailableStock() {
        return stock.getAvailableQuantity();
    }
}
