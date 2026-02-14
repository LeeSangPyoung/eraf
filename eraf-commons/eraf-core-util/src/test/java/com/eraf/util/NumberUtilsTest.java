package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NumberUtils - 숫자 유틸리티")
class NumberUtilsTest {

    @Nested
    @DisplayName("문자열 → int 변환")
    class ToInt {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(42, NumberUtils.toInt("42"));
            assertEquals(-1, NumberUtils.toInt("-1"));
            assertEquals(42, NumberUtils.toInt(" 42 "));
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(10, NumberUtils.toInt("abc", 10));
            assertEquals(0, NumberUtils.toInt(null, 0));
            assertEquals(0, NumberUtils.toInt("", 0));
            assertEquals(0, NumberUtils.toInt("  ", 0));
        }
    }

    @Nested
    @DisplayName("문자열 → long 변환")
    class ToLong {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(123456789L, NumberUtils.toLong("123456789"));
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(99L, NumberUtils.toLong("abc", 99L));
            assertEquals(0L, NumberUtils.toLong(null, 0L));
        }
    }

    @Nested
    @DisplayName("문자열 → double 변환")
    class ToDouble {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(3.14, NumberUtils.toDouble("3.14"), 0.001);
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(1.0, NumberUtils.toDouble("abc", 1.0), 0.001);
            assertEquals(0.0, NumberUtils.toDouble(null, 0.0), 0.001);
        }
    }

    @Nested
    @DisplayName("문자열 → float 변환")
    class ToFloat {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(3.14f, NumberUtils.toFloat("3.14"), 0.01f);
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(1.0f, NumberUtils.toFloat("abc", 1.0f), 0.01f);
            assertEquals(0.0f, NumberUtils.toFloat(null, 0.0f), 0.01f);
        }
    }

    @Nested
    @DisplayName("문자열 → BigDecimal 변환")
    class ToBigDecimal {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(new BigDecimal("123.45"), NumberUtils.toBigDecimal("123.45"));
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(BigDecimal.TEN, NumberUtils.toBigDecimal("abc", BigDecimal.TEN));
            assertEquals(BigDecimal.ZERO, NumberUtils.toBigDecimal(null, BigDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("문자열 → BigInteger 변환")
    class ToBigInteger {

        @Test
        @DisplayName("정상 변환")
        void valid() {
            assertEquals(new BigInteger("123456789"), NumberUtils.toBigInteger("123456789"));
        }

        @Test
        @DisplayName("기본값 반환")
        void withDefault() {
            assertEquals(BigInteger.ONE, NumberUtils.toBigInteger("abc", BigInteger.ONE));
            assertEquals(BigInteger.ZERO, NumberUtils.toBigInteger(null, BigInteger.ZERO));
        }
    }

    @Nested
    @DisplayName("검증")
    class Validation {

        @Test
        @DisplayName("isNumber")
        void isNumber() {
            assertTrue(NumberUtils.isNumber("123"));
            assertTrue(NumberUtils.isNumber("3.14"));
            assertTrue(NumberUtils.isNumber("-1"));
            assertFalse(NumberUtils.isNumber("abc"));
            assertFalse(NumberUtils.isNumber(null));
            assertFalse(NumberUtils.isNumber(""));
            assertFalse(NumberUtils.isNumber("  "));
        }

        @Test
        @DisplayName("isInteger")
        void isInteger() {
            assertTrue(NumberUtils.isInteger("123"));
            assertTrue(NumberUtils.isInteger("-1"));
            assertFalse(NumberUtils.isInteger("3.14"));
            assertFalse(NumberUtils.isInteger("abc"));
            assertFalse(NumberUtils.isInteger(null));
        }

        @Test
        @DisplayName("isPositive / isNegative / isZero")
        void signChecks() {
            assertTrue(NumberUtils.isPositive(5));
            assertTrue(NumberUtils.isPositive(3.14));
            assertFalse(NumberUtils.isPositive(-1));
            assertFalse(NumberUtils.isPositive(null));

            assertTrue(NumberUtils.isNegative(-1));
            assertFalse(NumberUtils.isNegative(5));
            assertFalse(NumberUtils.isNegative(null));

            assertTrue(NumberUtils.isZero(0));
            assertTrue(NumberUtils.isZero(0.0));
            assertFalse(NumberUtils.isZero(1));
            assertFalse(NumberUtils.isZero(null));
        }
    }

    @Nested
    @DisplayName("비교 - min/max")
    class MinMax {

        @Test
        @DisplayName("int min/max")
        void intMinMax() {
            assertEquals(1, NumberUtils.min(3, 1, 2));
            assertEquals(3, NumberUtils.max(3, 1, 2));
        }

        @Test
        @DisplayName("long min/max")
        void longMinMax() {
            assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
            assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        }

        @Test
        @DisplayName("double min/max")
        void doubleMinMax() {
            assertEquals(1.0, NumberUtils.min(3.0, 1.0, 2.0), 0.001);
            assertEquals(3.0, NumberUtils.max(3.0, 1.0, 2.0), 0.001);
        }

        @Test
        @DisplayName("빈 배열 예외")
        void emptyArray() {
            assertThrows(IllegalArgumentException.class, () -> NumberUtils.min(new int[0]));
            assertThrows(IllegalArgumentException.class, () -> NumberUtils.max(new int[0]));
        }
    }
}
