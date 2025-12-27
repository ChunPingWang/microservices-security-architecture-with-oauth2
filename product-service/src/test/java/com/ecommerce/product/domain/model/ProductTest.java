package com.ecommerce.product.domain.model;

import com.ecommerce.product.domain.event.ProductCreatedEvent;
import com.ecommerce.product.domain.event.ProductStatusChangedEvent;
import com.ecommerce.product.domain.event.ProductStockUpdatedEvent;
import com.ecommerce.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void create_shouldCreateNewProduct() {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        Product product = Product.create(
            "iPhone 15", "Latest Apple smartphone",
            "SKU-001", price, 100, categoryId
        );

        assertNotNull(product.getId());
        assertEquals("iPhone 15", product.getName());
        assertEquals("Latest Apple smartphone", product.getDescription());
        assertEquals("SKU-001", product.getSku());
        assertEquals(price, product.getPrice());
        assertEquals(100, product.getStock().getQuantity());
        assertEquals(categoryId, product.getCategoryId());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        // Check domain event
        assertEquals(1, product.getDomainEvents().size());
        assertInstanceOf(ProductCreatedEvent.class, product.getDomainEvents().get(0));
    }

    @Test
    void create_shouldThrowExceptionForNullName() {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        assertThrows(NullPointerException.class, () ->
            Product.create(null, "Description", "SKU-001", price, 100, categoryId)
        );
    }

    @Test
    void create_shouldThrowExceptionForEmptyName() {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        assertThrows(IllegalArgumentException.class, () ->
            Product.create("  ", "Description", "SKU-001", price, 100, categoryId)
        );
    }

    @Test
    void create_shouldThrowExceptionForNegativePrice() {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("-100"));

        assertThrows(IllegalArgumentException.class, () ->
            Product.create("iPhone 15", "Description", "SKU-001", price, 100, categoryId)
        );
    }

    @Test
    void constructor_shouldReconstituteProduct() {
        ProductId id = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(100, 10);
        Instant now = Instant.now();

        Product product = new Product(
            id, "iPhone 15", "Description", "SKU-001",
            price, stock, categoryId, ProductStatus.INACTIVE, "https://example.com/image.jpg",
            now, now
        );

        assertEquals(id, product.getId());
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        assertEquals(100, product.getStock().getQuantity());
        assertEquals(10, product.getStock().getReservedQuantity());
        assertTrue(product.getDomainEvents().isEmpty());
    }

    @Test
    void updateDetails_shouldUpdateFields() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        product.updateDetails("iPhone 15 Pro", "Updated description", "SKU-002", "https://new.url");

        assertEquals("iPhone 15 Pro", product.getName());
        assertEquals("Updated description", product.getDescription());
        assertEquals("SKU-002", product.getSku());
        assertEquals("https://new.url", product.getImageUrl());
    }

    @Test
    void updatePrice_shouldChangePrice() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        Money newPrice = Money.of(new BigDecimal("1299.99"));
        product.updatePrice(newPrice);

        assertEquals(newPrice, product.getPrice());
    }

    @Test
    void reserveStock_shouldReserveQuantity() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        product.reserveStock(20);

        assertEquals(80, product.getStock().getAvailableQuantity());
        assertEquals(20, product.getStock().getReservedQuantity());
    }

    @Test
    void reserveStock_shouldThrowExceptionWhenInsufficientStock() {
        Product product = createTestProduct();

        assertThrows(IllegalStateException.class, () -> product.reserveStock(150));
    }

    @Test
    void releaseStock_shouldReleaseReservedQuantity() {
        Product product = createTestProduct();
        product.reserveStock(30);
        product.clearDomainEvents();

        product.releaseStock(10);

        assertEquals(80, product.getStock().getAvailableQuantity());
        assertEquals(20, product.getStock().getReservedQuantity());
    }

    @Test
    void deductStock_shouldDeductFromReserved() {
        Product product = createTestProduct();
        product.reserveStock(30);
        product.clearDomainEvents();

        product.deductStock(20);

        assertEquals(80, product.getStock().getQuantity());
        assertEquals(10, product.getStock().getReservedQuantity());
    }

    @Test
    void addStock_shouldIncreaseQuantity() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        product.addStock(50);

        assertEquals(150, product.getStock().getQuantity());
        assertEquals(150, product.getStock().getAvailableQuantity());

        // Check domain event
        assertEquals(1, product.getDomainEvents().size());
        assertInstanceOf(ProductStockUpdatedEvent.class, product.getDomainEvents().get(0));
    }

    @Test
    void activate_shouldChangeStatusToActive() {
        ProductId id = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(100);
        Instant now = Instant.now();

        Product product = new Product(
            id, "iPhone 15", "Description", "SKU-001",
            price, stock, categoryId, ProductStatus.INACTIVE, null,
            now, now
        );

        product.activate();

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(1, product.getDomainEvents().size());
        assertInstanceOf(ProductStatusChangedEvent.class, product.getDomainEvents().get(0));
    }

    @Test
    void deactivate_shouldChangeStatusToInactive() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        product.deactivate();

        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        assertEquals(1, product.getDomainEvents().size());
    }

    @Test
    void discontinue_shouldChangeStatusToDiscontinued() {
        Product product = createTestProduct();
        product.clearDomainEvents();

        product.discontinue();

        assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
    }

    @Test
    void isAvailableForPurchase_shouldReturnTrueForActiveProductWithStock() {
        Product product = createTestProduct();

        assertTrue(product.isAvailableForPurchase());
    }

    @Test
    void isAvailableForPurchase_shouldReturnFalseForInactiveProduct() {
        ProductId id = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(100);
        Instant now = Instant.now();

        Product product = new Product(
            id, "iPhone 15", "Description", "SKU-001",
            price, stock, categoryId, ProductStatus.INACTIVE, null,
            now, now
        );

        assertFalse(product.isAvailableForPurchase());
    }

    @Test
    void isAvailableForPurchase_shouldReturnFalseForZeroStock() {
        ProductId id = ProductId.generate();
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));
        Stock stock = Stock.of(0);
        Instant now = Instant.now();

        Product product = new Product(
            id, "iPhone 15", "Description", "SKU-001",
            price, stock, categoryId, ProductStatus.ACTIVE, null,
            now, now
        );

        assertFalse(product.isAvailableForPurchase());
    }

    @Test
    void hasAvailableStock_shouldReturnTrueWhenSufficientStock() {
        Product product = createTestProduct();

        assertTrue(product.hasAvailableStock(50));
        assertTrue(product.hasAvailableStock(100));
    }

    @Test
    void hasAvailableStock_shouldReturnFalseWhenInsufficientStock() {
        Product product = createTestProduct();

        assertFalse(product.hasAvailableStock(150));
    }

    private Product createTestProduct() {
        CategoryId categoryId = CategoryId.generate();
        Money price = Money.of(new BigDecimal("999.99"));

        return Product.create(
            "iPhone 15", "Latest Apple smartphone",
            "SKU-001", price, 100, categoryId
        );
    }
}
