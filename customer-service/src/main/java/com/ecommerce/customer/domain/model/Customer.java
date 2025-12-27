package com.ecommerce.customer.domain.model;

import com.ecommerce.customer.domain.event.CustomerLockedEvent;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;
import com.ecommerce.customer.domain.event.LoginFailedEvent;
import com.ecommerce.customer.domain.event.LoginSuccessEvent;
import com.ecommerce.shared.domain.AggregateRoot;
import com.ecommerce.shared.vo.Email;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Customer Aggregate Root.
 * Handles customer registration, authentication, and account management.
 */
public class Customer extends AggregateRoot<CustomerId> {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final CustomerId id;
    private Email email;
    private Password password;
    private String name;
    private CustomerStatus status;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private Instant createdAt;
    private Instant updatedAt;

    // Private constructor for creating new customer
    private Customer(CustomerId id, Email email, Password password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.status = CustomerStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Public constructor for reconstitution from persistence.
     */
    public Customer(CustomerId id, Email email, Password password, String name,
                    CustomerStatus status, int failedLoginAttempts, Instant lockedUntil,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new customer (alias for register).
     */
    public static Customer create(Email email, String plainPassword, String name) {
        return register(email, plainPassword, name);
    }

    /**
     * Register a new customer.
     */
    public static Customer register(Email email, String plainPassword, String name) {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(plainPassword, "Password cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        CustomerId id = CustomerId.generate();
        Password password = Password.fromPlainText(plainPassword);
        Customer customer = new Customer(id, email, password, name.trim());

        customer.registerEvent(new CustomerRegisteredEvent(
                id.asString(),
                email.getValue(),
                name
        ));

        return customer;
    }

    /**
     * Authenticate customer with password.
     *
     * @return true if authentication successful
     */
    public boolean authenticate(String plainPassword) {
        // Check if account is locked
        if (isLocked()) {
            return false;
        }

        // Check if account is active
        if (status != CustomerStatus.ACTIVE && status != CustomerStatus.LOCKED) {
            return false;
        }

        // Auto-unlock if lock duration has passed
        if (status == CustomerStatus.LOCKED && !isLocked()) {
            unlock();
        }

        if (password.matches(plainPassword)) {
            onLoginSuccess();
            return true;
        } else {
            onLoginFailure();
            return false;
        }
    }

    private void onLoginSuccess() {
        failedLoginAttempts = 0;
        lockedUntil = null;
        updatedAt = Instant.now();

        registerEvent(new LoginSuccessEvent(id.asString(), email.getValue()));
    }

    private void onLoginFailure() {
        failedLoginAttempts++;
        updatedAt = Instant.now();

        registerEvent(new LoginFailedEvent(id.asString(), email.getValue(), failedLoginAttempts));

        if (failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            lock();
        }
    }

    /**
     * Lock the account for the specified duration.
     */
    public void lock() {
        this.status = CustomerStatus.LOCKED;
        this.lockedUntil = Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES);
        this.updatedAt = Instant.now();

        registerEvent(new CustomerLockedEvent(
                id.asString(),
                email.getValue(),
                lockedUntil
        ));
    }

    /**
     * Unlock the account.
     */
    public void unlock() {
        this.status = CustomerStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Check if the account is currently locked.
     */
    public boolean isLocked() {
        if (status != CustomerStatus.LOCKED) {
            return false;
        }
        if (lockedUntil == null) {
            return false;
        }
        return Instant.now().isBefore(lockedUntil);
    }

    /**
     * Suspend the account (admin action).
     */
    public void suspend() {
        this.status = CustomerStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivate a suspended account (admin action).
     */
    public void reactivate() {
        this.status = CustomerStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Update customer profile.
     */
    public void updateProfile(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Change password.
     */
    public void changePassword(String currentPassword, String newPassword) {
        if (!password.matches(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        this.password = Password.fromPlainText(newPassword);
        this.updatedAt = Instant.now();
    }

    // Getters
    @Override
    public CustomerId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean canLogin() {
        return status == CustomerStatus.ACTIVE ||
               (status == CustomerStatus.LOCKED && !isLocked());
    }
}
