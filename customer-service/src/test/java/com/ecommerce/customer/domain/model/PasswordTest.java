package com.ecommerce.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password 值物件測試")
class PasswordTest {

    // Strong passwords that meet the validation criteria
    private static final String VALID_PASSWORD = "SecureP@ss1";
    private static final String ANOTHER_VALID_PASSWORD = "MyStr0ngPwd";

    @Test
    @DisplayName("應該能從明文建立密碼")
    void shouldCreateFromPlainText() {
        Password password = Password.fromPlainText(VALID_PASSWORD);

        assertThat(password).isNotNull();
        assertThat(password.getHashedValue()).isNotNull();
        assertThat(password.getHashedValue()).startsWith("$2a$");
    }

    @Test
    @DisplayName("應該能從雜湊值建立密碼")
    void shouldCreateFromHash() {
        String hash = "$2a$12$N9qo8uLOickgx2ZMRZoMy.MqD7Lk3S5x7Q8YvL3wuC7X3/4aXXXXX";
        Password password = Password.fromHash(hash);

        assertThat(password.getHashedValue()).isEqualTo(hash);
    }

    @Test
    @DisplayName("密碼驗證應該成功")
    void shouldMatchCorrectPassword() {
        Password password = Password.fromPlainText(VALID_PASSWORD);

        assertThat(password.matches(VALID_PASSWORD)).isTrue();
    }

    @Test
    @DisplayName("錯誤密碼驗證應該失敗")
    void shouldNotMatchWrongPassword() {
        Password password = Password.fromPlainText(VALID_PASSWORD);

        assertThat(password.matches("WrongP@ss1")).isFalse();
    }

    @Test
    @DisplayName("相同密碼每次雜湊結果應該不同")
    void hashShouldBeDifferentEachTime() {
        Password password1 = Password.fromPlainText(VALID_PASSWORD);
        Password password2 = Password.fromPlainText(VALID_PASSWORD);

        assertThat(password1.getHashedValue()).isNotEqualTo(password2.getHashedValue());
        // 但兩個都應該能驗證原始密碼
        assertThat(password1.matches(VALID_PASSWORD)).isTrue();
        assertThat(password2.matches(VALID_PASSWORD)).isTrue();
    }

    @Test
    @DisplayName("弱密碼應該拋出例外")
    void shouldThrowExceptionForWeakPassword() {
        // 沒有大寫字母
        assertThatThrownBy(() -> Password.fromPlainText("weakpassword1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase");

        // 沒有數字
        assertThatThrownBy(() -> Password.fromPlainText("WeakPassword"))
                .isInstanceOf(IllegalArgumentException.class);

        // 太短
        assertThatThrownBy(() -> Password.fromPlainText("Pass1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 密碼應該拋出例外")
    void shouldThrowExceptionForNullPassword() {
        assertThatThrownBy(() -> Password.fromPlainText(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("null 雜湊值應該拋出例外")
    void shouldThrowExceptionForNullHash() {
        assertThatThrownBy(() -> Password.fromHash(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Password 物件相等性測試")
    void equalityTest() {
        String hash = "$2a$12$N9qo8uLOickgx2ZMRZoMy.MqD7Lk3S5x7Q8YvL3wuC7X3/4aXXXXX";
        Password password1 = Password.fromHash(hash);
        Password password2 = Password.fromHash(hash);

        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("isValidFormat 應該正確驗證密碼格式")
    void shouldValidatePasswordFormat() {
        assertThat(Password.isValidFormat(VALID_PASSWORD)).isTrue();
        assertThat(Password.isValidFormat("short1A")).isFalse();  // 太短
        assertThat(Password.isValidFormat("nouppercase1")).isFalse();  // 沒有大寫
        assertThat(Password.isValidFormat("NOLOWERCASE1")).isFalse();  // 沒有小寫
        assertThat(Password.isValidFormat("NoDigitsHere")).isFalse();  // 沒有數字
        assertThat(Password.isValidFormat(null)).isFalse();  // null
    }

    @Test
    @DisplayName("null 密碼驗證應該返回 false")
    void matchesShouldReturnFalseForNull() {
        Password password = Password.fromPlainText(VALID_PASSWORD);
        assertThat(password.matches(null)).isFalse();
    }

    @Test
    @DisplayName("toString 應該隱藏密碼")
    void toStringShouldHidePassword() {
        Password password = Password.fromPlainText(VALID_PASSWORD);
        assertThat(password.toString()).doesNotContain(VALID_PASSWORD);
        assertThat(password.toString()).isEqualTo("[PROTECTED]");
    }
}
