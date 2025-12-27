package com.ecommerce.shared.vo;

import com.ecommerce.shared.domain.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a phone number.
 * Supports various phone number formats.
 */
public class PhoneNumber extends ValueObject<PhoneNumber> {

    // Pattern supports: +886912345678, 0912345678, 0912-345-678, (02)12345678
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+?\\d{1,4})?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$"
    );

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\D");

    private final String value;
    private final String normalizedValue;

    private PhoneNumber(String value) {
        this.value = value.trim();
        this.normalizedValue = DIGITS_ONLY.matcher(this.value).replaceAll("");
    }

    public static PhoneNumber of(String value) {
        Objects.requireNonNull(value, "Phone number cannot be null");

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        }

        return new PhoneNumber(trimmed);
    }

    public String getValue() {
        return value;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public boolean isMobile() {
        // Taiwan mobile phone starts with 09
        return normalizedValue.startsWith("09") ||
               normalizedValue.startsWith("8869");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(normalizedValue, that.normalizedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedValue);
    }

    @Override
    public String toString() {
        return value;
    }
}
