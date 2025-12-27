package com.ecommerce.security.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CurrentUserContext Tests")
class CurrentUserContextTest {

    private CurrentUserContext context;

    @BeforeEach
    void setUp() {
        context = new CurrentUserContext();
    }

    @Nested
    @DisplayName("Initial state tests")
    class InitialStateTests {

        @Test
        @DisplayName("should not be authenticated initially")
        void shouldNotBeAuthenticatedInitially() {
            assertThat(context.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("should return empty optionals initially")
        void shouldReturnEmptyOptionalsInitially() {
            assertThat(context.getUserId()).isEmpty();
            assertThat(context.getEmail()).isEmpty();
            assertThat(context.getRole()).isEmpty();
        }

        @Test
        @DisplayName("should not be service account initially")
        void shouldNotBeServiceAccountInitially() {
            assertThat(context.isServiceAccount()).isFalse();
        }
    }

    @Nested
    @DisplayName("After setting user")
    class AfterSettingUserTests {

        @BeforeEach
        void setUser() {
            context.setUser("user-123", "test@example.com", "CUSTOMER");
        }

        @Test
        @DisplayName("should be authenticated")
        void shouldBeAuthenticated() {
            assertThat(context.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("should return user ID")
        void shouldReturnUserId() {
            assertThat(context.getUserId()).isPresent().contains("user-123");
        }

        @Test
        @DisplayName("should return email")
        void shouldReturnEmail() {
            assertThat(context.getEmail()).isPresent().contains("test@example.com");
        }

        @Test
        @DisplayName("should return role")
        void shouldReturnRole() {
            assertThat(context.getRole()).isPresent().contains("CUSTOMER");
        }

        @Test
        @DisplayName("requireUserId should return user ID")
        void requireUserIdShouldReturnUserId() {
            assertThat(context.requireUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("requireEmail should return email")
        void requireEmailShouldReturnEmail() {
            assertThat(context.requireEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Role checks")
    class RoleCheckTests {

        @Test
        @DisplayName("should detect customer role")
        void shouldDetectCustomerRole() {
            context.setUser("user-123", "test@example.com", "CUSTOMER");

            assertThat(context.isCustomer()).isTrue();
            assertThat(context.isAdmin()).isFalse();
            assertThat(context.hasRole("CUSTOMER")).isTrue();
        }

        @Test
        @DisplayName("should detect admin role")
        void shouldDetectAdminRole() {
            context.setUser("admin-123", "admin@example.com", "ADMIN");

            assertThat(context.isAdmin()).isTrue();
            assertThat(context.isCustomer()).isFalse();
            assertThat(context.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("should detect service account")
        void shouldDetectServiceAccount() {
            context.setUser("order-service", "order-service@internal", "SERVICE");

            assertThat(context.isServiceAccount()).isTrue();
            assertThat(context.hasRole("SERVICE")).isTrue();
        }
    }

    @Nested
    @DisplayName("Clear context tests")
    class ClearContextTests {

        @Test
        @DisplayName("should clear all data")
        void shouldClearAllData() {
            context.setUser("user-123", "test@example.com", "CUSTOMER");
            context.clear();

            assertThat(context.isAuthenticated()).isFalse();
            assertThat(context.getUserId()).isEmpty();
            assertThat(context.getEmail()).isEmpty();
            assertThat(context.getRole()).isEmpty();
            assertThat(context.isServiceAccount()).isFalse();
        }
    }

    @Nested
    @DisplayName("Require methods when not authenticated")
    class RequireMethodsNotAuthenticatedTests {

        @Test
        @DisplayName("requireUserId should throw when not authenticated")
        void requireUserIdShouldThrowWhenNotAuthenticated() {
            assertThatThrownBy(() -> context.requireUserId())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not authenticated");
        }

        @Test
        @DisplayName("requireEmail should throw when not authenticated")
        void requireEmailShouldThrowWhenNotAuthenticated() {
            assertThatThrownBy(() -> context.requireEmail())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not authenticated");
        }
    }

    @Test
    @DisplayName("toString should include user info when authenticated")
    void toStringShouldIncludeUserInfoWhenAuthenticated() {
        context.setUser("user-123", "test@example.com", "CUSTOMER");

        String str = context.toString();

        assertThat(str)
                .contains("user-123")
                .contains("test@example.com")
                .contains("CUSTOMER");
    }

    @Test
    @DisplayName("toString should indicate anonymous when not authenticated")
    void toStringShouldIndicateAnonymousWhenNotAuthenticated() {
        String str = context.toString();

        assertThat(str).contains("anonymous");
    }
}
