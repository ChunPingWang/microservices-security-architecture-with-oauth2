package com.ecommerce.customer.domain.model;

import com.ecommerce.customer.domain.event.CustomerLockedEvent;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;
import com.ecommerce.customer.domain.event.LoginFailedEvent;
import com.ecommerce.customer.domain.event.LoginSuccessEvent;
import com.ecommerce.shared.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Customer 聚合根測試")
class CustomerTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecureP@ssw0rd123";
    private static final String TEST_NAME = "Test User";

    @Nested
    @DisplayName("建立客戶")
    class CreateCustomer {

        @Test
        @DisplayName("應該能成功建立新客戶")
        void shouldCreateNewCustomer() {
            Customer customer = Customer.create(
                    Email.of(TEST_EMAIL),
                    TEST_PASSWORD,
                    TEST_NAME
            );

            assertThat(customer.getId()).isNotNull();
            assertThat(customer.getEmail().getValue()).isEqualTo(TEST_EMAIL);
            assertThat(customer.getName()).isEqualTo(TEST_NAME);
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getFailedLoginAttempts()).isZero();
            assertThat(customer.getLockedUntil()).isNull();
        }

        @Test
        @DisplayName("建立客戶應該產生 CustomerRegisteredEvent")
        void shouldEmitCustomerRegisteredEvent() {
            Customer customer = Customer.create(
                    Email.of(TEST_EMAIL),
                    TEST_PASSWORD,
                    TEST_NAME
            );

            assertThat(customer.getDomainEvents()).hasSize(1);
            assertThat(customer.getDomainEvents().get(0))
                    .isInstanceOf(CustomerRegisteredEvent.class);

            CustomerRegisteredEvent event = (CustomerRegisteredEvent) customer.getDomainEvents().get(0);
            assertThat(event.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(event.getName()).isEqualTo(TEST_NAME);
        }
    }

    @Nested
    @DisplayName("認證功能")
    class Authentication {

        private Customer customer;

        @BeforeEach
        void setUp() {
            customer = Customer.create(
                    Email.of(TEST_EMAIL),
                    TEST_PASSWORD,
                    TEST_NAME
            );
            customer.clearDomainEvents();
        }

        @Test
        @DisplayName("正確密碼應該認證成功")
        void shouldAuthenticateWithCorrectPassword() {
            boolean result = customer.authenticate(TEST_PASSWORD);

            assertThat(result).isTrue();
            assertThat(customer.getFailedLoginAttempts()).isZero();
        }

        @Test
        @DisplayName("認證成功應該產生 LoginSuccessEvent")
        void shouldEmitLoginSuccessEvent() {
            customer.authenticate(TEST_PASSWORD);

            assertThat(customer.getDomainEvents()).hasSize(1);
            assertThat(customer.getDomainEvents().get(0))
                    .isInstanceOf(LoginSuccessEvent.class);
        }

        @Test
        @DisplayName("錯誤密碼應該認證失敗")
        void shouldFailAuthenticationWithWrongPassword() {
            boolean result = customer.authenticate("WrongP@ss1");

            assertThat(result).isFalse();
            assertThat(customer.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("認證失敗應該產生 LoginFailedEvent")
        void shouldEmitLoginFailedEvent() {
            customer.authenticate("WrongP@ss1");

            assertThat(customer.getDomainEvents()).hasSize(1);
            assertThat(customer.getDomainEvents().get(0))
                    .isInstanceOf(LoginFailedEvent.class);
        }

        @Test
        @DisplayName("連續5次認證失敗應該鎖定帳號")
        void shouldLockAccountAfter5FailedAttempts() {
            for (int i = 0; i < 5; i++) {
                customer.authenticate("WrongP@ss1");
            }

            assertThat(customer.isLocked()).isTrue();
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.LOCKED);
            assertThat(customer.getLockedUntil()).isNotNull();
            assertThat(customer.getLockedUntil()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("鎖定帳號後應該產生 CustomerLockedEvent")
        void shouldEmitCustomerLockedEvent() {
            for (int i = 0; i < 5; i++) {
                customer.authenticate("WrongP@ss1");
            }

            assertThat(customer.getDomainEvents())
                    .extracting(e -> e.getClass().getSimpleName())
                    .contains("CustomerLockedEvent");
        }

        @Test
        @DisplayName("被鎖定的帳號即使密碼正確也應該認證失敗")
        void lockedAccountShouldFailAuthentication() {
            // 鎖定帳號
            for (int i = 0; i < 5; i++) {
                customer.authenticate("WrongP@ss1");
            }
            customer.clearDomainEvents();

            boolean result = customer.authenticate(TEST_PASSWORD);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("成功登入應該重置失敗次數")
        void successfulLoginShouldResetFailedAttempts() {
            // 先失敗3次
            for (int i = 0; i < 3; i++) {
                customer.authenticate("WrongP@ss1");
            }
            assertThat(customer.getFailedLoginAttempts()).isEqualTo(3);

            // 然後成功登入
            customer.authenticate(TEST_PASSWORD);

            assertThat(customer.getFailedLoginAttempts()).isZero();
        }
    }

    @Nested
    @DisplayName("帳號鎖定")
    class AccountLocking {

        @Test
        @DisplayName("新帳號不應該被鎖定")
        void newAccountShouldNotBeLocked() {
            Customer customer = Customer.create(
                    Email.of(TEST_EMAIL),
                    TEST_PASSWORD,
                    TEST_NAME
            );

            assertThat(customer.isLocked()).isFalse();
        }

        @Test
        @DisplayName("鎖定時間過後應該自動解鎖")
        void shouldUnlockAfterLockDuration() {
            // 建立一個過去已鎖定的客戶
            Customer customer = new Customer(
                    CustomerId.generate(),
                    Email.of(TEST_EMAIL),
                    Password.fromPlainText(TEST_PASSWORD),
                    TEST_NAME,
                    CustomerStatus.LOCKED,
                    5,
                    Instant.now().minusSeconds(60), // 鎖定時間已過
                    Instant.now(),
                    Instant.now()
            );

            assertThat(customer.isLocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("領域事件")
    class DomainEvents {

        @Test
        @DisplayName("clearDomainEvents 應該清除所有事件")
        void shouldClearDomainEvents() {
            Customer customer = Customer.create(
                    Email.of(TEST_EMAIL),
                    TEST_PASSWORD,
                    TEST_NAME
            );
            assertThat(customer.getDomainEvents()).isNotEmpty();

            customer.clearDomainEvents();

            assertThat(customer.getDomainEvents()).isEmpty();
        }
    }
}
