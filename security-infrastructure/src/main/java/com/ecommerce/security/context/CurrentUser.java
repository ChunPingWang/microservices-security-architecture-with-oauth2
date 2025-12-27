package com.ecommerce.security.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the current user context into controller methods.
 *
 * Usage:
 * <pre>
 * @GetMapping("/profile")
 * public ResponseEntity<?> getProfile(@CurrentUser CurrentUserContext user) {
 *     String userId = user.requireUserId();
 *     // ...
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
