package com.ecommerce.shared.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Address Value Object Tests")
class AddressTest {

    @Nested
    @DisplayName("Creation tests")
    class CreationTests {

        @Test
        @DisplayName("should create address with all fields")
        void shouldCreateWithAllFields() {
            Address address = Address.builder()
                    .country("Taiwan")
                    .city("Taipei")
                    .district("Da'an")
                    .postalCode("106")
                    .street("Xinyi Road")
                    .detail("No. 123, 5F")
                    .build();

            assertThat(address.getCountry()).isEqualTo("Taiwan");
            assertThat(address.getCity()).isEqualTo("Taipei");
            assertThat(address.getDistrict()).isEqualTo("Da'an");
            assertThat(address.getPostalCode()).isEqualTo("106");
            assertThat(address.getStreet()).isEqualTo("Xinyi Road");
            assertThat(address.getDetail()).isEqualTo("No. 123, 5F");
        }

        @Test
        @DisplayName("should create address with required fields only")
        void shouldCreateWithRequiredFieldsOnly() {
            Address address = Address.builder()
                    .city("Taipei")
                    .street("Xinyi Road")
                    .build();

            assertThat(address.getCity()).isEqualTo("Taipei");
            assertThat(address.getStreet()).isEqualTo("Xinyi Road");
            assertThat(address.getCountry()).isEqualTo("Taiwan"); // default
        }

        @Test
        @DisplayName("should throw exception for null city")
        void shouldThrowForNullCity() {
            assertThatThrownBy(() -> Address.builder()
                    .street("Street")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("City");
        }

        @Test
        @DisplayName("should throw exception for null street")
        void shouldThrowForNullStreet() {
            assertThatThrownBy(() -> Address.builder()
                    .city("Taipei")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Street");
        }
    }

    @Nested
    @DisplayName("Full address formatting")
    class FullAddressTests {

        @Test
        @DisplayName("should format full address")
        void shouldFormatFullAddress() {
            Address address = Address.builder()
                    .postalCode("106")
                    .city("Taipei")
                    .district("Da'an")
                    .street("Xinyi Road")
                    .detail("No. 123")
                    .build();

            String fullAddress = address.getFullAddress();

            assertThat(fullAddress).contains("106", "Taipei", "Da'an", "Xinyi Road", "No. 123");
        }

        @Test
        @DisplayName("should format address without optional fields")
        void shouldFormatWithoutOptionalFields() {
            Address address = Address.builder()
                    .city("Taipei")
                    .street("Main Street")
                    .build();

            String fullAddress = address.getFullAddress();

            assertThat(fullAddress).contains("Taipei", "Main Street");
            assertThat(fullAddress).doesNotContain("null");
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same addresses")
        void shouldBeEqualForSame() {
            Address a = Address.builder()
                    .city("Taipei")
                    .street("Main Street")
                    .build();

            Address b = Address.builder()
                    .city("Taipei")
                    .street("Main Street")
                    .build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different addresses")
        void shouldNotBeEqualForDifferent() {
            Address a = Address.builder()
                    .city("Taipei")
                    .street("Street A")
                    .build();

            Address b = Address.builder()
                    .city("Taipei")
                    .street("Street B")
                    .build();

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Test
    @DisplayName("toString should return full address")
    void toStringShouldReturnFullAddress() {
        Address address = Address.builder()
                .city("Taipei")
                .street("Main Street")
                .build();

        assertThat(address.toString()).isEqualTo(address.getFullAddress());
    }
}
