package com.ecommerce.shared.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneNumber Value Object Tests")
class PhoneNumberTest {

    @Nested
    @DisplayName("Valid phone number creation")
    class ValidPhoneTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "0912345678",
                "0912-345-678",
                "+886912345678",
                "(02)12345678",
                "02-1234-5678"
        })
        @DisplayName("should accept valid phone formats")
        void shouldAcceptValidPhones(String phone) {
            PhoneNumber result = PhoneNumber.of(phone);

            assertThat(result.getValue()).isNotEmpty();
        }

        @Test
        @DisplayName("should normalize phone number")
        void shouldNormalizePhoneNumber() {
            PhoneNumber phone = PhoneNumber.of("0912-345-678");

            assertThat(phone.getNormalizedValue()).isEqualTo("0912345678");
        }
    }

    @Nested
    @DisplayName("Invalid phone rejection")
    class InvalidPhoneTests {

        @Test
        @DisplayName("should reject null phone")
        void shouldRejectNull() {
            assertThatThrownBy(() -> PhoneNumber.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty phone")
        void shouldRejectEmpty() {
            assertThatThrownBy(() -> PhoneNumber.of(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject invalid format")
        void shouldRejectInvalidFormat() {
            assertThatThrownBy(() -> PhoneNumber.of("abc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid phone number format");
        }
    }

    @Nested
    @DisplayName("Mobile detection")
    class MobileDetectionTests {

        @Test
        @DisplayName("should detect Taiwan mobile number")
        void shouldDetectTaiwanMobile() {
            PhoneNumber phone = PhoneNumber.of("0912345678");

            assertThat(phone.isMobile()).isTrue();
        }

        @Test
        @DisplayName("should detect international Taiwan mobile")
        void shouldDetectInternationalMobile() {
            PhoneNumber phone = PhoneNumber.of("+886912345678");

            assertThat(phone.isMobile()).isTrue();
        }

        @Test
        @DisplayName("should not detect landline as mobile")
        void shouldNotDetectLandlineAsMobile() {
            PhoneNumber phone = PhoneNumber.of("02-12345678");

            assertThat(phone.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same normalized number")
        void shouldBeEqualForSameNormalized() {
            PhoneNumber a = PhoneNumber.of("0912-345-678");
            PhoneNumber b = PhoneNumber.of("0912345678");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different numbers")
        void shouldNotBeEqualForDifferent() {
            PhoneNumber a = PhoneNumber.of("0912345678");
            PhoneNumber b = PhoneNumber.of("0987654321");

            assertThat(a).isNotEqualTo(b);
        }
    }
}
