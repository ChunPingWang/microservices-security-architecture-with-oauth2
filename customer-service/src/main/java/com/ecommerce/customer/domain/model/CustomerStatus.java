package com.ecommerce.customer.domain.model;

/**
 * Customer account status.
 */
public enum CustomerStatus {
    /**
     * Active account, can login and use services.
     */
    ACTIVE,

    /**
     * Temporarily locked due to failed login attempts.
     * Will be automatically unlocked after lock duration expires.
     */
    LOCKED,

    /**
     * Suspended by administrator.
     * Requires manual intervention to reactivate.
     */
    SUSPENDED,

    /**
     * Account pending email verification.
     */
    PENDING_VERIFICATION
}
