package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BooleanUtils - Boolean 유틸리티")
class BooleanUtilsTest {

    @Nested
    @DisplayName("Null-safe 체크")
    class NullSafeCheck {

        @Test
        @DisplayName("isTrue / isFalse")
        void isTrueIsFalse() {
            assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
            assertFalse(BooleanUtils.isTrue(Boolean.FALSE));
            assertFalse(BooleanUtils.isTrue(null));

            assertTrue(BooleanUtils.isFalse(Boolean.FALSE));
            assertFalse(BooleanUtils.isFalse(Boolean.TRUE));
            assertFalse(BooleanUtils.isFalse(null));
        }

        @Test
        @DisplayName("isNotTrue / isNotFalse")
        void isNotTrueIsNotFalse() {
            assertTrue(BooleanUtils.isNotTrue(Boolean.FALSE));
            assertTrue(BooleanUtils.isNotTrue(null));
            assertFalse(BooleanUtils.isNotTrue(Boolean.TRUE));

            assertTrue(BooleanUtils.isNotFalse(Boolean.TRUE));
            assertTrue(BooleanUtils.isNotFalse(null));
            assertFalse(BooleanUtils.isNotFalse(Boolean.FALSE));
        }
    }

    @Nested
    @DisplayName("문자열 변환")
    class StringConversion {

        @Test
        @DisplayName("toBoolean 문자열 → Boolean")
        void toBoolean() {
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("true"));
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("yes"));
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("y"));
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("1"));
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("on"));
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("TRUE"));

            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("false"));
            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("no"));
            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("n"));
            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("0"));
            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("off"));

            assertNull(BooleanUtils.toBoolean((String) null));
            assertNull(BooleanUtils.toBoolean(""));
            assertNull(BooleanUtils.toBoolean("maybe"));
        }

        @Test
        @DisplayName("toBooleanDefaultFalse / toBooleanDefaultTrue")
        void toBooleanDefaults() {
            assertTrue(BooleanUtils.toBooleanDefaultFalse("true"));
            assertFalse(BooleanUtils.toBooleanDefaultFalse(null));
            assertFalse(BooleanUtils.toBooleanDefaultFalse("maybe"));

            assertTrue(BooleanUtils.toBooleanDefaultTrue(null));
            assertTrue(BooleanUtils.toBooleanDefaultTrue("maybe"));
            assertFalse(BooleanUtils.toBooleanDefaultTrue("false"));
        }

        @Test
        @DisplayName("커스텀 true/false 문자열")
        void customTrueFalse() {
            assertEquals(Boolean.TRUE, BooleanUtils.toBoolean("Y", "Y", "N"));
            assertEquals(Boolean.FALSE, BooleanUtils.toBoolean("N", "Y", "N"));
            assertNull(BooleanUtils.toBoolean("X", "Y", "N"));
            assertNull(BooleanUtils.toBoolean(null, "Y", "N"));
        }

        @Test
        @DisplayName("정수 → Boolean")
        void intToBoolean() {
            assertTrue(BooleanUtils.toBoolean(1));
            assertTrue(BooleanUtils.toBoolean(-1));
            assertFalse(BooleanUtils.toBoolean(0));
        }
    }

    @Nested
    @DisplayName("Boolean → 문자열")
    class BooleanToString {

        @Test
        @DisplayName("toString 기본")
        void toStringBasic() {
            assertEquals("true", BooleanUtils.toString(Boolean.TRUE));
            assertEquals("false", BooleanUtils.toString(Boolean.FALSE));
            assertNull(BooleanUtils.toString(null));
        }

        @Test
        @DisplayName("toString 커스텀")
        void toStringCustom() {
            assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N"));
            assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N"));
            assertNull(BooleanUtils.toString(null, "Y", "N"));
        }

        @Test
        @DisplayName("toString 3인자 (null 포함)")
        void toStringWithNull() {
            assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "-"));
            assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "-"));
            assertEquals("-", BooleanUtils.toString(null, "Y", "N", "-"));
        }

        @Test
        @DisplayName("Y/N, Yes/No, ON/OFF, 1/0")
        void predefinedFormats() {
            assertEquals("Y", BooleanUtils.toYN(Boolean.TRUE));
            assertEquals("N", BooleanUtils.toYN(Boolean.FALSE));
            assertEquals("Yes", BooleanUtils.toYesNo(Boolean.TRUE));
            assertEquals("No", BooleanUtils.toYesNo(Boolean.FALSE));
            assertEquals("ON", BooleanUtils.toOnOff(Boolean.TRUE));
            assertEquals("OFF", BooleanUtils.toOnOff(Boolean.FALSE));
            assertEquals("1", BooleanUtils.to10(Boolean.TRUE));
            assertEquals("0", BooleanUtils.to10(Boolean.FALSE));
        }
    }

    @Nested
    @DisplayName("정수 변환")
    class IntConversion {

        @Test
        @DisplayName("toInt")
        void toInt() {
            assertEquals(1, BooleanUtils.toInt(Boolean.TRUE));
            assertEquals(0, BooleanUtils.toInt(Boolean.FALSE));
            assertEquals(0, BooleanUtils.toInt(null));
        }

        @Test
        @DisplayName("toInteger")
        void toInteger() {
            assertEquals(Integer.valueOf(1), BooleanUtils.toInteger(Boolean.TRUE));
            assertEquals(Integer.valueOf(0), BooleanUtils.toInteger(Boolean.FALSE));
            assertNull(BooleanUtils.toInteger(null));
        }
    }

    @Nested
    @DisplayName("논리 연산")
    class LogicalOps {

        @Test
        @DisplayName("and (varargs)")
        void andVarargs() {
            assertTrue(BooleanUtils.and(Boolean.TRUE, Boolean.TRUE));
            assertFalse(BooleanUtils.and(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.and((Boolean[]) null));
            assertFalse(BooleanUtils.and());
        }

        @Test
        @DisplayName("or (varargs)")
        void orVarargs() {
            assertTrue(BooleanUtils.or(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.or(Boolean.FALSE, Boolean.FALSE));
            assertFalse(BooleanUtils.or((Boolean[]) null));
        }

        @Test
        @DisplayName("xor / not")
        void xorNot() {
            assertTrue(BooleanUtils.xor(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.xor(Boolean.TRUE, Boolean.TRUE));

            assertEquals(Boolean.FALSE, BooleanUtils.not(Boolean.TRUE));
            assertEquals(Boolean.TRUE, BooleanUtils.not(Boolean.FALSE));
            assertNull(BooleanUtils.not(null));
        }

        @Test
        @DisplayName("nand / nor")
        void nandNor() {
            assertFalse(BooleanUtils.nand(Boolean.TRUE, Boolean.TRUE));
            assertTrue(BooleanUtils.nand(Boolean.TRUE, Boolean.FALSE));

            assertTrue(BooleanUtils.nor(Boolean.FALSE, Boolean.FALSE));
            assertFalse(BooleanUtils.nor(Boolean.TRUE, Boolean.FALSE));
        }

        @Test
        @DisplayName("and/or (2인자 Boolean 반환)")
        void andOr2Args() {
            assertEquals(Boolean.TRUE, BooleanUtils.and(Boolean.TRUE, Boolean.TRUE));
            assertEquals(Boolean.FALSE, BooleanUtils.and(Boolean.FALSE, Boolean.TRUE));
            assertNull(BooleanUtils.and(null, Boolean.TRUE));

            assertEquals(Boolean.TRUE, BooleanUtils.or(Boolean.TRUE, Boolean.FALSE));
            assertEquals(Boolean.FALSE, BooleanUtils.or(Boolean.FALSE, Boolean.FALSE));
            assertNull(BooleanUtils.or(null, Boolean.FALSE));
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("equals")
        void equalsTest() {
            assertTrue(BooleanUtils.equals(Boolean.TRUE, Boolean.TRUE));
            assertTrue(BooleanUtils.equals(null, null));
            assertFalse(BooleanUtils.equals(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.equals(Boolean.TRUE, null));
        }

        @Test
        @DisplayName("allTrue / allFalse / anyTrue / anyFalse")
        void arrayChecks() {
            assertTrue(BooleanUtils.allTrue(Boolean.TRUE, Boolean.TRUE));
            assertFalse(BooleanUtils.allTrue(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.allTrue((Boolean[]) null));

            assertTrue(BooleanUtils.allFalse(Boolean.FALSE, Boolean.FALSE));
            assertFalse(BooleanUtils.allFalse(Boolean.FALSE, Boolean.TRUE));

            assertTrue(BooleanUtils.anyTrue(Boolean.FALSE, Boolean.TRUE));
            assertFalse(BooleanUtils.anyTrue(Boolean.FALSE, Boolean.FALSE));

            assertTrue(BooleanUtils.anyFalse(Boolean.TRUE, Boolean.FALSE));
            assertFalse(BooleanUtils.anyFalse(Boolean.TRUE, Boolean.TRUE));
        }

        @Test
        @DisplayName("countTrue / countFalse")
        void counting() {
            assertEquals(2, BooleanUtils.countTrue(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE));
            assertEquals(1, BooleanUtils.countFalse(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE));
            assertEquals(0, BooleanUtils.countTrue((Boolean[]) null));
            assertEquals(0, BooleanUtils.countFalse((Boolean[]) null));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("nullToFalse / nullToTrue")
        void nullDefaults() {
            assertTrue(BooleanUtils.nullToFalse(Boolean.TRUE));
            assertFalse(BooleanUtils.nullToFalse(null));

            assertTrue(BooleanUtils.nullToTrue(null));
            assertFalse(BooleanUtils.nullToTrue(Boolean.FALSE));
        }

        @Test
        @DisplayName("falseToNull / trueToNull")
        void toNull() {
            assertEquals(Boolean.TRUE, BooleanUtils.falseToNull(Boolean.TRUE));
            assertNull(BooleanUtils.falseToNull(Boolean.FALSE));

            assertEquals(Boolean.FALSE, BooleanUtils.trueToNull(Boolean.FALSE));
            assertNull(BooleanUtils.trueToNull(Boolean.TRUE));
        }

        @Test
        @DisplayName("toggle")
        void toggle() {
            assertEquals(Boolean.FALSE, BooleanUtils.toggle(Boolean.TRUE));
            assertEquals(Boolean.TRUE, BooleanUtils.toggle(Boolean.FALSE));
            assertNull(BooleanUtils.toggle((Boolean) null));
            assertTrue(BooleanUtils.toggle(false));
            assertFalse(BooleanUtils.toggle(true));
        }

        @Test
        @DisplayName("parseYN / parse10")
        void parseSpecial() {
            assertEquals(Boolean.TRUE, BooleanUtils.parseYN("Y"));
            assertEquals(Boolean.TRUE, BooleanUtils.parseYN("y"));
            assertEquals(Boolean.FALSE, BooleanUtils.parseYN("N"));
            assertNull(BooleanUtils.parseYN("X"));
            assertNull(BooleanUtils.parseYN(null));

            assertEquals(Boolean.TRUE, BooleanUtils.parse10("1"));
            assertEquals(Boolean.FALSE, BooleanUtils.parse10("0"));
            assertNull(BooleanUtils.parse10("2"));
            assertNull(BooleanUtils.parse10(null));
        }
    }
}
