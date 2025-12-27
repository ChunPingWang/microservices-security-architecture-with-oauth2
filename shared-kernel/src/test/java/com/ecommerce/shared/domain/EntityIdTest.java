package com.ecommerce.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EntityId Tests")
class EntityIdTest {

    // Test implementation of EntityId
    static class TestEntityId extends EntityId<TestEntityId> {
        public TestEntityId() {
            super();
        }

        public TestEntityId(UUID value) {
            super(value);
        }

        public TestEntityId(String value) {
            super(value);
        }
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests {

        @Test
        @DisplayName("should generate random UUID when created without value")
        void shouldGenerateRandomUUID() {
            TestEntityId id = new TestEntityId();

            assertThat(id.getValue()).isNotNull();
        }

        @Test
        @DisplayName("should create from UUID")
        void shouldCreateFromUUID() {
            UUID uuid = UUID.randomUUID();
            TestEntityId id = new TestEntityId(uuid);

            assertThat(id.getValue()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("should create from string")
        void shouldCreateFromString() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            TestEntityId id = new TestEntityId(uuidStr);

            assertThat(id.asString()).isEqualTo(uuidStr);
        }

        @Test
        @DisplayName("should throw for null UUID")
        void shouldThrowForNullUUID() {
            assertThatThrownBy(() -> new TestEntityId((UUID) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for invalid UUID string")
        void shouldThrowForInvalidString() {
            assertThatThrownBy(() -> new TestEntityId("not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same UUID value")
        void shouldBeEqualForSameValue() {
            UUID uuid = UUID.randomUUID();
            TestEntityId a = new TestEntityId(uuid);
            TestEntityId b = new TestEntityId(uuid);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different UUIDs")
        void shouldNotBeEqualForDifferentUUIDs() {
            TestEntityId a = new TestEntityId();
            TestEntityId b = new TestEntityId();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("should be comparable")
        void shouldBeComparable() {
            TestEntityId a = new TestEntityId();
            TestEntityId b = new TestEntityId();

            // Should not throw
            int comparison = a.compareTo(b);
            assertThat(comparison).isNotNull();
        }
    }

    @Test
    @DisplayName("toString should include class name and UUID")
    void toStringShouldIncludeClassNameAndUUID() {
        UUID uuid = UUID.randomUUID();
        TestEntityId id = new TestEntityId(uuid);

        assertThat(id.toString())
                .contains("TestEntityId")
                .contains(uuid.toString());
    }
}
