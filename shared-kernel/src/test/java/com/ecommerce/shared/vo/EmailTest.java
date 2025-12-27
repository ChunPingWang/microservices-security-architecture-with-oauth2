package com.ecommerce.shared.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Nested
    @DisplayName("Valid email creation")
    class ValidEmailTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.org",
                "user+tag@example.co.tw",
                "firstname.lastname@company.com"
        })
        @DisplayName("should accept valid email formats")
        void shouldAcceptValidEmails(String email) {
            Email result = Email.of(email);

            assertThat(result.getValue()).isEqualTo(email.toLowerCase());
        }

        @Test
        @DisplayName("should normalize email to lowercase")
        void shouldNormalizeToLowercase() {
            Email email = Email.of("Test@Example.COM");

            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should trim whitespace")
        void shouldTrimWhitespace() {
            Email email = Email.of("  test@example.com  ");

            assertThat(email.getValue()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Invalid email rejection")
    class InvalidEmailTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "notanemail",
                "@nodomain.com",
                "no@domain",
                "spaces in@email.com",
                "missing@.com"
        })
        @DisplayName("should reject invalid email formats")
        void shouldRejectInvalidEmails(String email) {
            assertThatThrownBy(() -> Email.of(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty email")
        void shouldRejectEmptyEmail() {
            assertThatThrownBy(() -> Email.of(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Email parts extraction")
    class EmailPartsTests {

        @Test
        @DisplayName("should extract local part")
        void shouldExtractLocalPart() {
            Email email = Email.of("john.doe@example.com");

            assertThat(email.getLocalPart()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("should extract domain")
        void shouldExtractDomain() {
            Email email = Email.of("john.doe@example.com");

            assertThat(email.getDomain()).isEqualTo("example.com");
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same email")
        void shouldBeEqualForSameEmail() {
            Email a = Email.of("test@example.com");
            Email b = Email.of("test@example.com");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should be equal for different case")
        void shouldBeEqualForDifferentCase() {
            Email a = Email.of("Test@Example.com");
            Email b = Email.of("test@example.com");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("should not be equal for different emails")
        void shouldNotBeEqualForDifferentEmails() {
            Email a = Email.of("user1@example.com");
            Email b = Email.of("user2@example.com");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Test
    @DisplayName("toString should return email value")
    void toStringShouldReturnValue() {
        Email email = Email.of("test@example.com");

        assertThat(email.toString()).isEqualTo("test@example.com");
    }
}
