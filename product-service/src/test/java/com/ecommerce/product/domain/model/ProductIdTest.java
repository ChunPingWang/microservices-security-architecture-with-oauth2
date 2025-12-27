package com.ecommerce.product.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductIdTest {

    @Test
    void generate_shouldCreateNewProductIdWithValidUUID() {
        ProductId productId = ProductId.generate();

        assertNotNull(productId);
        assertNotNull(productId.getValue());
        assertDoesNotThrow(() -> UUID.fromString(productId.getValue().toString()));
    }

    @Test
    void of_shouldCreateProductIdFromValidUUID() {
        UUID uuid = UUID.randomUUID();
        ProductId productId = ProductId.of(uuid);

        assertNotNull(productId);
        assertEquals(uuid, productId.getValue());
    }

    @Test
    void of_shouldCreateProductIdFromValidString() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        ProductId productId = ProductId.of(uuidString);

        assertNotNull(productId);
        assertEquals(uuidString, productId.getValue().toString());
    }

    @Test
    void of_shouldThrowExceptionForInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> ProductId.of("invalid-uuid"));
    }

    @Test
    void of_shouldThrowExceptionForNull() {
        assertThrows(NullPointerException.class, () -> ProductId.of((String) null));
    }

    @Test
    void of_shouldThrowExceptionForEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> ProductId.of(""));
    }

    @Test
    void equals_shouldReturnTrueForSameValue() {
        UUID uuid = UUID.randomUUID();
        ProductId id1 = ProductId.of(uuid);
        ProductId id2 = ProductId.of(uuid);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentValue() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();

        assertNotEquals(id1, id2);
    }

    @Test
    void asString_shouldReturnUUIDString() {
        UUID uuid = UUID.randomUUID();
        ProductId productId = ProductId.of(uuid);

        assertEquals(uuid.toString(), productId.asString());
    }

    @Test
    void toString_shouldReturnFormattedString() {
        UUID uuid = UUID.randomUUID();
        ProductId productId = ProductId.of(uuid);

        assertEquals("ProductId[" + uuid.toString() + "]", productId.toString());
    }
}
