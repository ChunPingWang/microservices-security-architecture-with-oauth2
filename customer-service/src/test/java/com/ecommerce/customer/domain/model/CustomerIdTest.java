package com.ecommerce.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CustomerId 值物件測試")
class CustomerIdTest {

    @Test
    @DisplayName("應該能建立新的 CustomerId")
    void shouldCreateNewCustomerId() {
        CustomerId id = CustomerId.generate();

        assertThat(id).isNotNull();
        assertThat(id.getValue()).isNotNull();
    }

    @Test
    @DisplayName("應該能從 UUID 建立 CustomerId")
    void shouldCreateFromUuid() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = CustomerId.of(uuid);

        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("應該能從字串建立 CustomerId")
    void shouldCreateFromString() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = CustomerId.of(uuid.toString());

        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("相同 UUID 的 CustomerId 應該相等")
    void shouldBeEqualForSameUuid() {
        UUID uuid = UUID.randomUUID();
        CustomerId id1 = CustomerId.of(uuid);
        CustomerId id2 = CustomerId.of(uuid);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("不同 UUID 的 CustomerId 應該不相等")
    void shouldNotBeEqualForDifferentUuid() {
        CustomerId id1 = CustomerId.generate();
        CustomerId id2 = CustomerId.generate();

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("asString 應該回傳 UUID 字串")
    void asStringShouldReturnUuidString() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = CustomerId.of(uuid);

        assertThat(id.asString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("toString 應該包含 UUID 字串")
    void toStringShouldContainUuidString() {
        UUID uuid = UUID.randomUUID();
        CustomerId id = CustomerId.of(uuid);

        assertThat(id.toString()).contains(uuid.toString());
    }

    @Test
    @DisplayName("null UUID 應該拋出例外")
    void shouldThrowExceptionForNullUuid() {
        assertThatThrownBy(() -> CustomerId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("無效的 UUID 字串應該拋出例外")
    void shouldThrowExceptionForInvalidUuidString() {
        assertThatThrownBy(() -> CustomerId.of("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
