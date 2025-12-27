package com.ecommerce.customer.domain.model;

import com.ecommerce.shared.domain.ValueObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Password Value Object.
 * Handles password encryption and validation.
 */
public class Password extends ValueObject<Password> {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);

    // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    /**
     * Create a new password from plain text.
     * The password will be hashed using BCrypt.
     */
    public static Password fromPlainText(String plainText) {
        Objects.requireNonNull(plainText, "Password cannot be null");

        if (!isValidFormat(plainText)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and contain uppercase, lowercase, and digit");
        }

        String hashed = PASSWORD_ENCODER.encode(plainText);
        return new Password(hashed);
    }

    /**
     * Create a Password object from an already hashed value.
     * Used when loading from database.
     */
    public static Password fromHash(String hashedValue) {
        Objects.requireNonNull(hashedValue, "Hashed password cannot be null");
        return new Password(hashedValue);
    }

    /**
     * Validate password format before hashing.
     */
    public static boolean isValidFormat(String plainText) {
        return plainText != null && PASSWORD_PATTERN.matcher(plainText).matches();
    }

    /**
     * Check if the given plain text matches this password.
     */
    public boolean matches(String plainText) {
        if (plainText == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(plainText, hashedValue);
    }

    public String getHashedValue() {
        return hashedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
