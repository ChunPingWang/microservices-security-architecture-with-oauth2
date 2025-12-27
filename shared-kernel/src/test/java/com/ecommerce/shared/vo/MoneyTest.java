package com.ecommerce.shared.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Creation tests")
    class CreationTests {

        @Test
        @DisplayName("should create money from BigDecimal")
        void shouldCreateFromBigDecimal() {
            Money money = Money.of(new BigDecimal("100.50"));

            assertThat(money.getAmount()).isEqualByComparingTo("100.50");
            assertThat(money.getCurrency()).isEqualTo(Money.DEFAULT_CURRENCY);
        }

        @Test
        @DisplayName("should create money from double")
        void shouldCreateFromDouble() {
            Money money = Money.of(99.99);

            assertThat(money.getAmount()).isEqualByComparingTo("99.99");
        }

        @Test
        @DisplayName("should create money from long")
        void shouldCreateFromLong() {
            Money money = Money.of(1000L);

            assertThat(money.getAmount()).isEqualByComparingTo("1000");
        }

        @Test
        @DisplayName("should create money from string")
        void shouldCreateFromString() {
            Money money = Money.of("250.00");

            assertThat(money.getAmount()).isEqualByComparingTo("250.00");
        }

        @Test
        @DisplayName("should create zero money")
        void shouldCreateZeroMoney() {
            assertThat(Money.ZERO.isZero()).isTrue();
        }

        @Test
        @DisplayName("should throw exception for null amount")
        void shouldThrowForNullAmount() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Arithmetic operations")
    class ArithmeticTests {

        @Test
        @DisplayName("should add two money values")
        void shouldAddMoney() {
            Money a = Money.of(100);
            Money b = Money.of(50);

            Money result = a.add(b);

            assertThat(result.getAmount()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("should subtract money values")
        void shouldSubtractMoney() {
            Money a = Money.of(100);
            Money b = Money.of(30);

            Money result = a.subtract(b);

            assertThat(result.getAmount()).isEqualByComparingTo("70");
        }

        @Test
        @DisplayName("should multiply by integer")
        void shouldMultiplyByInteger() {
            Money money = Money.of(50);

            Money result = money.multiply(3);

            assertThat(result.getAmount()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("should calculate percentage")
        void shouldCalculatePercentage() {
            Money money = Money.of(200);

            Money result = money.percentage(10);

            assertThat(result.getAmount()).isEqualByComparingTo("20");
        }

        @Test
        @DisplayName("should throw exception for different currencies")
        void shouldThrowForDifferentCurrencies() {
            Money twd = Money.of(BigDecimal.valueOf(100), Currency.getInstance("TWD"));
            Money usd = Money.of(BigDecimal.valueOf(50), Currency.getInstance("USD"));

            assertThatThrownBy(() -> twd.add(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("Comparison operations")
    class ComparisonTests {

        @Test
        @DisplayName("should check if positive")
        void shouldCheckPositive() {
            assertThat(Money.of(100).isPositive()).isTrue();
            assertThat(Money.of(-100).isPositive()).isFalse();
            assertThat(Money.of(0).isPositive()).isFalse();
        }

        @Test
        @DisplayName("should check if negative")
        void shouldCheckNegative() {
            assertThat(Money.of(-100).isNegative()).isTrue();
            assertThat(Money.of(100).isNegative()).isFalse();
        }

        @Test
        @DisplayName("should compare money values")
        void shouldCompareMoney() {
            Money a = Money.of(100);
            Money b = Money.of(50);

            assertThat(a.isGreaterThan(b)).isTrue();
            assertThat(b.isLessThan(a)).isTrue();
            assertThat(a.isGreaterThanOrEqual(Money.of(100))).isTrue();
        }
    }

    @Nested
    @DisplayName("Equality tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same amount and currency")
        void shouldBeEqualForSameAmountAndCurrency() {
            Money a = Money.of(100);
            Money b = Money.of(100);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different amounts")
        void shouldNotBeEqualForDifferentAmounts() {
            Money a = Money.of(100);
            Money b = Money.of(200);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("should handle scale differences")
        void shouldHandleScaleDifferences() {
            Money a = Money.of("100.00");
            Money b = Money.of("100");

            assertThat(a).isEqualTo(b);
        }
    }

    @Test
    @DisplayName("should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        Money money = Money.of(1234.56);

        assertThat(money.toString()).contains("1234.56");
    }
}
