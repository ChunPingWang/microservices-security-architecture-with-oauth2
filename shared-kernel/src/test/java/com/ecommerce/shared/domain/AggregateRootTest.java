package com.ecommerce.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AggregateRoot Tests")
class AggregateRootTest {

    // Test EntityId implementation
    static class TestId extends EntityId<TestId> {
        public TestId() {
            super();
        }

        public TestId(UUID value) {
            super(value);
        }
    }

    // Test DomainEvent implementation
    static class TestCreatedEvent extends DomainEvent {
        public TestCreatedEvent(String aggregateId) {
            super(aggregateId, "TestAggregate");
        }
    }

    static class TestUpdatedEvent extends DomainEvent {
        public TestUpdatedEvent(String aggregateId) {
            super(aggregateId, "TestAggregate");
        }
    }

    // Test AggregateRoot implementation
    static class TestAggregate extends AggregateRoot<TestId> {
        private final TestId id;
        private String name;

        public TestAggregate(TestId id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public TestId getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void create() {
            registerEvent(new TestCreatedEvent(id.asString()));
        }

        public void update(String newName) {
            this.name = newName;
            registerEvent(new TestUpdatedEvent(id.asString()));
        }
    }

    @Nested
    @DisplayName("Domain events management")
    class DomainEventsTests {

        @Test
        @DisplayName("should have no events initially")
        void shouldHaveNoEventsInitially() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");

            assertThat(aggregate.hasDomainEvents()).isFalse();
            assertThat(aggregate.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("should collect registered events")
        void shouldCollectRegisteredEvents() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");

            aggregate.create();

            assertThat(aggregate.hasDomainEvents()).isTrue();
            assertThat(aggregate.getDomainEvents()).hasSize(1);
            assertThat(aggregate.getDomainEvents().get(0)).isInstanceOf(TestCreatedEvent.class);
        }

        @Test
        @DisplayName("should collect multiple events in order")
        void shouldCollectMultipleEventsInOrder() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");

            aggregate.create();
            aggregate.update("new name");

            assertThat(aggregate.getDomainEvents()).hasSize(2);
            assertThat(aggregate.getDomainEvents().get(0)).isInstanceOf(TestCreatedEvent.class);
            assertThat(aggregate.getDomainEvents().get(1)).isInstanceOf(TestUpdatedEvent.class);
        }

        @Test
        @DisplayName("should clear events")
        void shouldClearEvents() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");
            aggregate.create();

            aggregate.clearDomainEvents();

            assertThat(aggregate.hasDomainEvents()).isFalse();
            assertThat(aggregate.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable events list")
        void shouldReturnUnmodifiableList() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");
            aggregate.create();

            assertThatThrownBy(() -> aggregate.getDomainEvents().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should throw for null event")
        void shouldThrowForNullEvent() {
            class NullEventAggregate extends AggregateRoot<TestId> {
                private final TestId id = new TestId();

                @Override
                public TestId getId() {
                    return id;
                }

                public void registerNullEvent() {
                    registerEvent(null);
                }
            }

            NullEventAggregate aggregate = new NullEventAggregate();

            assertThatThrownBy(aggregate::registerNullEvent)
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Version management")
    class VersionTests {

        @Test
        @DisplayName("should have initial version zero")
        void shouldHaveInitialVersionZero() {
            TestAggregate aggregate = new TestAggregate(new TestId(), "test");

            assertThat(aggregate.getVersion()).isZero();
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("aggregates with same ID should be equal")
        void aggregatesWithSameIdShouldBeEqual() {
            TestId id = new TestId();
            TestAggregate a = new TestAggregate(id, "name1");
            TestAggregate b = new TestAggregate(id, "name2");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("aggregates with different IDs should not be equal")
        void aggregatesWithDifferentIdsShouldNotBeEqual() {
            TestAggregate a = new TestAggregate(new TestId(), "name");
            TestAggregate b = new TestAggregate(new TestId(), "name");

            assertThat(a).isNotEqualTo(b);
        }
    }
}
