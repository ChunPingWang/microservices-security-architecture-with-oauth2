package com.ecommerce.security.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

/**
 * Request-scoped component to hold the current authenticated user's context.
 * This provides easy access to user information throughout the request lifecycle.
 */
@Component
@RequestScope
public class CurrentUserContext {

    private String userId;
    private String email;
    private String role;
    private boolean authenticated;
    private boolean serviceAccount;

    /**
     * Set the current user from JWT claims.
     */
    public void setUser(String userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.authenticated = true;
        this.serviceAccount = "SERVICE".equals(role);
    }

    /**
     * Clear the current user context.
     */
    public void clear() {
        this.userId = null;
        this.email = null;
        this.role = null;
        this.authenticated = false;
        this.serviceAccount = false;
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getRole() {
        return Optional.ofNullable(role);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isServiceAccount() {
        return serviceAccount;
    }

    /**
     * Get user ID or throw exception if not authenticated.
     */
    public String requireUserId() {
        return getUserId().orElseThrow(() ->
                new IllegalStateException("User not authenticated"));
    }

    /**
     * Get email or throw exception if not authenticated.
     */
    public String requireEmail() {
        return getEmail().orElseThrow(() ->
                new IllegalStateException("User not authenticated"));
    }

    /**
     * Check if current user has the specified role.
     */
    public boolean hasRole(String role) {
        return this.role != null && this.role.equals(role);
    }

    /**
     * Check if current user is an admin.
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is a customer.
     */
    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    @Override
    public String toString() {
        if (!authenticated) {
            return "CurrentUserContext[anonymous]";
        }
        return "CurrentUserContext[userId=" + userId + ", email=" + email + ", role=" + role + "]";
    }
}
