package com.eraf.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtils - 문자열 유틸리티 테스트")
class StringUtilsTest {

    @Nested
    @DisplayName("Null/Empty 체크")
    class NullEmptyCheck {

        @Test
        @DisplayName("isEmpty 체크")
        void isEmpty() {
            assertTrue(StringUtils.isEmpty(null));
            assertTrue(StringUtils.isEmpty(""));
            assertFalse(StringUtils.isEmpty(" "));
            assertFalse(StringUtils.isEmpty("text"));
        }

        @Test
        @DisplayName("isNotEmpty 체크")
        void isNotEmpty() {
            assertFalse(StringUtils.isNotEmpty(null));
            assertFalse(StringUtils.isNotEmpty(""));
            assertTrue(StringUtils.isNotEmpty(" "));
            assertTrue(StringUtils.isNotEmpty("text"));
        }

        @Test
        @DisplayName("isBlank 체크")
        void isBlank() {
            assertTrue(StringUtils.isBlank(null));
            assertTrue(StringUtils.isBlank(""));
            assertTrue(StringUtils.isBlank(" "));
            assertTrue(StringUtils.isBlank("   "));
            assertFalse(StringUtils.isBlank("text"));
        }

        @Test
        @DisplayName("isNotBlank 체크")
        void isNotBlank() {
            assertFalse(StringUtils.isNotBlank(null));
            assertFalse(StringUtils.isNotBlank(""));
            assertFalse(StringUtils.isNotBlank(" "));
            assertTrue(StringUtils.isNotBlank("text"));
        }
    }

    @Nested
    @DisplayName("Null-safe 연산")
    class NullSafeOperations {

        @Test
        @DisplayName("defaultIfEmpty")
        void defaultIfEmpty() {
            assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
            assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
            assertEquals(" ", StringUtils.defaultIfEmpty(" ", "default"));
            assertEquals("text", StringUtils.defaultIfEmpty("text", "default"));
        }

        @Test
        @DisplayName("defaultIfBlank")
        void defaultIfBlank() {
            assertEquals("default", StringUtils.defaultIfBlank(null, "default"));
            assertEquals("default", StringUtils.defaultIfBlank("", "default"));
            assertEquals("default", StringUtils.defaultIfBlank(" ", "default"));
            assertEquals("text", StringUtils.defaultIfBlank("text", "default"));
        }

        @Test
        @DisplayName("nullToEmpty")
        void nullToEmpty() {
            assertEquals("", StringUtils.nullToEmpty(null));
            assertEquals("", StringUtils.nullToEmpty(""));
            assertEquals("text", StringUtils.nullToEmpty("text"));
        }

        @Test
        @DisplayName("emptyToNull")
        void emptyToNull() {
            assertNull(StringUtils.emptyToNull(null));
            assertNull(StringUtils.emptyToNull(""));
            assertEquals("text", StringUtils.emptyToNull("text"));
        }

        @Test
        @DisplayName("trim")
        void trim() {
            assertNull(StringUtils.trim(null));
            assertEquals("", StringUtils.trim(""));
            assertEquals("", StringUtils.trim("   "));
            assertEquals("text", StringUtils.trim("  text  "));
        }

        @Test
        @DisplayName("trimToNull")
        void trimToNull() {
            assertNull(StringUtils.trimToNull(null));
            assertNull(StringUtils.trimToNull(""));
            assertNull(StringUtils.trimToNull("   "));
            assertEquals("text", StringUtils.trimToNull("  text  "));
        }

        @Test
        @DisplayName("trimToEmpty")
        void trimToEmpty() {
            assertEquals("", StringUtils.trimToEmpty(null));
            assertEquals("", StringUtils.trimToEmpty(""));
            assertEquals("", StringUtils.trimToEmpty("   "));
            assertEquals("text", StringUtils.trimToEmpty("  text  "));
        }
    }

    @Nested
    @DisplayName("길이 제한")
    class Truncation {

        @Test
        @DisplayName("truncate")
        void truncate() {
            assertEquals("Hello", StringUtils.truncate("Hello, World!", 5));
            assertEquals("Hi", StringUtils.truncate("Hi", 10)); // 길이보다 작으면 그대로
            assertNull(StringUtils.truncate(null, 5));
        }

        @Test
        @DisplayName("truncateWithEllipsis")
        void truncateWithEllipsis() {
            assertEquals("Hello...", StringUtils.truncateWithEllipsis("Hello, World!", 8));
            assertEquals("Hi", StringUtils.truncateWithEllipsis("Hi", 10));
            assertEquals("He", StringUtils.truncateWithEllipsis("Hello", 2)); // 3자 이하면 생략기호 없음
        }

        @Test
        @DisplayName("truncateBytes")
        void truncateBytes() {
            String korean = "안녕하세요";
            // UTF-8에서 한글 한 글자 = 3바이트

            String truncated = StringUtils.truncateBytes(korean, 9); // 3글자까지
            assertEquals("안녕하", truncated);

            assertNull(StringUtils.truncateBytes(null, 10));
        }
    }

    @Nested
    @DisplayName("패딩")
    class Padding {

        @Test
        @DisplayName("leftPad")
        void leftPad() {
            assertEquals("  abc", StringUtils.leftPad("abc", 5));
            assertEquals("00123", StringUtils.leftPad("123", 5, '0'));
            assertEquals("abc", StringUtils.leftPad("abc", 2)); // 길이보다 길면 그대로
            assertEquals("     ", StringUtils.leftPad(null, 5));
        }

        @Test
        @DisplayName("rightPad")
        void rightPad() {
            assertEquals("abc  ", StringUtils.rightPad("abc", 5));
            assertEquals("123XX", StringUtils.rightPad("123", 5, 'X'));
            assertEquals("abc", StringUtils.rightPad("abc", 2)); // 길이보다 길면 그대로
        }

        @Test
        @DisplayName("zeroPad")
        void zeroPad() {
            assertEquals("00123", StringUtils.zeroPad(123, 5));
            assertEquals("00001", StringUtils.zeroPad(1, 5));
            assertEquals("123456", StringUtils.zeroPad(123456, 5)); // 길이 초과
        }
    }

    @Nested
    @DisplayName("케이스 변환")
    class CaseConversion {

        @Test
        @DisplayName("capitalize")
        void capitalize() {
            assertEquals("Hello", StringUtils.capitalize("hello"));
            assertEquals("H", StringUtils.capitalize("h"));
            assertNull(StringUtils.capitalize(null));
            assertEquals("", StringUtils.capitalize(""));
        }

        @Test
        @DisplayName("uncapitalize")
        void uncapitalize() {
            assertEquals("hello", StringUtils.uncapitalize("Hello"));
            assertEquals("h", StringUtils.uncapitalize("H"));
            assertNull(StringUtils.uncapitalize(null));
        }

        @Test
        @DisplayName("toCamelCase")
        void toCamelCase() {
            assertEquals("helloWorld", StringUtils.toCamelCase("hello_world"));
            assertEquals("helloWorld", StringUtils.toCamelCase("hello-world"));
            assertEquals("helloWorld", StringUtils.toCamelCase("hello world"));
            assertEquals("helloWorldTest", StringUtils.toCamelCase("HELLO_WORLD_TEST"));
        }

        @Test
        @DisplayName("toSnakeCase")
        void toSnakeCase() {
            assertEquals("hello_world", StringUtils.toSnakeCase("helloWorld"));
            assertEquals("hello_world_test", StringUtils.toSnakeCase("HelloWorldTest"));
        }

        @Test
        @DisplayName("toKebabCase")
        void toKebabCase() {
            assertEquals("hello-world", StringUtils.toKebabCase("helloWorld"));
            assertEquals("hello-world-test", StringUtils.toKebabCase("HelloWorldTest"));
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("equals (null-safe)")
        void equals() {
            assertTrue(StringUtils.equals("abc", "abc"));
            assertFalse(StringUtils.equals("abc", "ABC"));
            assertFalse(StringUtils.equals(null, "abc"));
            assertFalse(StringUtils.equals("abc", null));
            assertTrue(StringUtils.equals(null, null)); // 같은 참조
        }

        @Test
        @DisplayName("equalsIgnoreCase")
        void equalsIgnoreCase() {
            assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
            assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
            assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        }

        @Test
        @DisplayName("contains")
        void contains() {
            assertTrue(StringUtils.contains("Hello, World!", "World"));
            assertFalse(StringUtils.contains("Hello, World!", "world"));
            assertFalse(StringUtils.contains(null, "test"));
            assertFalse(StringUtils.contains("test", null));
        }

        @Test
        @DisplayName("containsIgnoreCase")
        void containsIgnoreCase() {
            assertTrue(StringUtils.containsIgnoreCase("Hello, World!", "world"));
            assertTrue(StringUtils.containsIgnoreCase("Hello, World!", "WORLD"));
            assertFalse(StringUtils.containsIgnoreCase(null, "test"));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utilities {

        @Test
        @DisplayName("reverse")
        void reverse() {
            assertEquals("dcba", StringUtils.reverse("abcd"));
            assertEquals("", StringUtils.reverse(""));
            assertNull(StringUtils.reverse(null));
        }

        @Test
        @DisplayName("remove")
        void remove() {
            assertEquals("heo", StringUtils.remove("hello", "l"));
            assertEquals("hello", StringUtils.remove("hello", "x"));
            assertNull(StringUtils.remove(null, "x"));
            assertEquals("hello", StringUtils.remove("hello", null));
        }

        @Test
        @DisplayName("removeWhitespace")
        void removeWhitespace() {
            assertEquals("abc", StringUtils.removeWhitespace("  a b c  "));
            assertEquals("abc", StringUtils.removeWhitespace("a\tb\nc"));
            assertNull(StringUtils.removeWhitespace(null));
        }

        @Test
        @DisplayName("length")
        void length() {
            assertEquals(5, StringUtils.length("hello"));
            assertEquals(0, StringUtils.length(""));
            assertEquals(0, StringUtils.length(null));
        }

        @Test
        @DisplayName("byteLength")
        void byteLength() {
            assertEquals(5, StringUtils.byteLength("hello"));
            assertEquals(15, StringUtils.byteLength("안녕하세요")); // UTF-8: 한글 1자 = 3바이트
            assertEquals(0, StringUtils.byteLength(null));

            // 다른 인코딩
            assertEquals(10, StringUtils.byteLength("안녕하세요", StandardCharsets.UTF_16BE)); // UTF-16: 한글 1자 = 2바이트
        }
    }
}
