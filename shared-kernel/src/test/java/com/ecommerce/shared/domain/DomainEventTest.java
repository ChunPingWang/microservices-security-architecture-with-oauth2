package com.ecommerce.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DomainEvent Tests")
class DomainEventTest {

    // Test implementation of DomainEvent
    static class TestEvent extends DomainEvent {
        private final String data;

        public TestEvent(String aggregateId, String data) {
            super(aggregateId, "TestAggregate");
            this.data = data;
        }

        public TestEvent(String aggregateId, String data, int version) {
            super(aggregateId, "TestAggregate", version);
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests {

        @Test
        @DisplayName("should create event with required fields")
        void shouldCreateWithRequiredFields() {
            Instant before = Instant.now();
            TestEvent event = new TestEvent("aggregate-123", "test data");
            Instant after = Instant.now();

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo("aggregate-123");
            assertThat(event.getAggregateType()).isEqualTo("TestAggregate");
            assertThat(event.getVersion()).isEqualTo(1);
            assertThat(event.getOccurredOn())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("should create event with custom version")
        void shouldCreateWithCustomVersion() {
            TestEvent event = new TestEvent("aggregate-123", "test data", 5);

            assertThat(event.getVersion()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw for null aggregate ID")
        void shouldThrowForNullAggregateId() {
            assertThatThrownBy(() -> new TestEvent(null, "data"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should return event type as simple class name")
        void shouldReturnEventType() {
            TestEvent event = new TestEvent("aggregate-123", "data");

            assertThat(event.getEventType()).isEqualTo("TestEvent");
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("events with same ID should be equal")
        void eventsWithSameIdShouldBeEqual() {
            TestEvent event1 = new TestEvent("agg-1", "data1");
            // Events are equal by eventId, not by content
            assertThat(event1).isEqualTo(event1);
        }

        @Test
        @DisplayName("different events should not be equal")
        void differentEventsShouldNotBeEqual() {
            TestEvent event1 = new TestEvent("agg-1", "data1");
            TestEvent event2 = new TestEvent("agg-1", "data1");

            // Different events have different eventIds
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    @Test
    @DisplayName("toString should include event details")
    void toStringShouldIncludeDetails() {
        TestEvent event = new TestEvent("agg-123", "data");

        String str = event.toString();

        assertThat(str)
                .contains("TestEvent")
                .contains("agg-123")
                .contains("TestAggregate");
    }
}
