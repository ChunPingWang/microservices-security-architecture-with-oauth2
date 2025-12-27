package com.ecommerce.shared.vo;

import com.ecommerce.shared.domain.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Validates email format and provides domain extraction.
 */
public class Email extends ValueObject<Email> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }

    public static Email of(String value) {
        Objects.requireNonNull(value, "Email cannot be null");

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }

        return new Email(trimmed);
    }

    public String getValue() {
        return value;
    }

    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
