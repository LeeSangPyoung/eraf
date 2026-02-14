package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegexUtils - 정규식 유틸리티")
class RegexUtilsTest {

    @Nested
    @DisplayName("매칭")
    class Matching {

        @Test
        @DisplayName("matches")
        void matches() {
            assertTrue(RegexUtils.matches("hello123", "^[a-z]+\\d+$"));
            assertFalse(RegexUtils.matches("HELLO", "^[a-z]+$"));
            assertFalse(RegexUtils.matches(null, ".*"));
            assertFalse(RegexUtils.matches("test", null));
        }

        @Test
        @DisplayName("matchesIgnoreCase")
        void matchesIgnoreCase() {
            assertTrue(RegexUtils.matchesIgnoreCase("HELLO", "^hello$"));
            assertFalse(RegexUtils.matchesIgnoreCase(null, ".*"));
        }

        @Test
        @DisplayName("contains")
        void contains() {
            assertTrue(RegexUtils.contains("hello world", "world"));
            assertFalse(RegexUtils.contains("hello", "world"));
            assertFalse(RegexUtils.contains(null, ".*"));
        }

        @Test
        @DisplayName("startsWith / endsWith")
        void startsEndsWith() {
            assertTrue(RegexUtils.startsWith("hello world", "hello"));
            assertFalse(RegexUtils.startsWith("hello world", "world"));
            assertTrue(RegexUtils.endsWith("hello world", "world"));
            assertFalse(RegexUtils.endsWith("hello world", "hello"));
            assertFalse(RegexUtils.startsWith(null, ".*"));
            assertFalse(RegexUtils.endsWith(null, ".*"));
        }
    }

    @Nested
    @DisplayName("추출")
    class Extraction {

        @Test
        @DisplayName("extract")
        void extract() {
            assertEquals("123", RegexUtils.extract("abc123def", "\\d+"));
            assertNull(RegexUtils.extract("abc", "\\d+"));
            assertNull(RegexUtils.extract(null, "\\d+"));
        }

        @Test
        @DisplayName("extractAll")
        void extractAll() {
            List<String> results = RegexUtils.extractAll("a1b2c3", "\\d");
            assertEquals(3, results.size());
            assertTrue(RegexUtils.extractAll(null, "\\d").isEmpty());
        }

        @Test
        @DisplayName("extractGroup")
        void extractGroup() {
            assertEquals("John", RegexUtils.extractGroup("name: John age: 30", "name: (\\w+)", 1));
            assertNull(RegexUtils.extractGroup("no match", "(\\d+)", 1));
            assertNull(RegexUtils.extractGroup(null, "(\\d+)", 1));
        }

        @Test
        @DisplayName("extractGroups")
        void extractGroups() {
            List<String> groups = RegexUtils.extractGroups("2026-02-13", "(\\d{4})-(\\d{2})-(\\d{2})");
            assertEquals(3, groups.size());
            assertEquals("2026", groups.get(0));
            assertEquals("02", groups.get(1));
            assertEquals("13", groups.get(2));
            assertTrue(RegexUtils.extractGroups(null, ".*").isEmpty());
        }
    }

    @Nested
    @DisplayName("치환")
    class Replacement {

        @Test
        @DisplayName("replaceAll")
        void replaceAll() {
            assertEquals("abc___def", RegexUtils.replaceAll("abc123def", "\\d+", "___"));
            assertEquals("abc123def", RegexUtils.replaceAll("abc123def", "\\d+", null));
        }

        @Test
        @DisplayName("replaceFirst")
        void replaceFirst() {
            assertEquals("abc___def456", RegexUtils.replaceFirst("abc123def456", "\\d+", "___"));
        }

        @Test
        @DisplayName("remove")
        void remove() {
            assertEquals("abcdef", RegexUtils.remove("abc123def", "\\d+"));
        }
    }

    @Nested
    @DisplayName("분할")
    class Split {

        @Test
        @DisplayName("split")
        void split() {
            String[] parts = RegexUtils.split("a,b,c", ",");
            assertEquals(3, parts.length);
            assertEquals(0, RegexUtils.split(null, ",").length);
        }

        @Test
        @DisplayName("split limit")
        void splitLimit() {
            String[] parts = RegexUtils.split("a,b,c", ",", 2);
            assertEquals(2, parts.length);
        }
    }

    @Nested
    @DisplayName("카운트")
    class Count {

        @Test
        @DisplayName("count")
        void count() {
            assertEquals(3, RegexUtils.count("a1b2c3", "\\d"));
            assertEquals(0, RegexUtils.count("abc", "\\d"));
            assertEquals(0, RegexUtils.count(null, "\\d"));
        }
    }

    @Nested
    @DisplayName("검증 패턴")
    class ValidationPatterns {

        @Test
        @DisplayName("isEmail")
        void isEmail() {
            assertTrue(RegexUtils.isEmail("test@example.com"));
            assertFalse(RegexUtils.isEmail("invalid"));
            assertFalse(RegexUtils.isEmail(null));
        }

        @Test
        @DisplayName("isPhoneNumber")
        void isPhoneNumber() {
            assertTrue(RegexUtils.isPhoneNumber("01012345678"));
            assertTrue(RegexUtils.isPhoneNumber("010-1234-5678"));
            assertFalse(RegexUtils.isPhoneNumber(null));
            assertFalse(RegexUtils.isPhoneNumber("123"));
        }

        @Test
        @DisplayName("isUrl")
        void isUrl() {
            assertTrue(RegexUtils.isUrl("http://example.com"));
            assertTrue(RegexUtils.isUrl("https://example.com/path"));
            assertFalse(RegexUtils.isUrl("not-url"));
        }

        @Test
        @DisplayName("isIpAddress")
        void isIpAddress() {
            assertTrue(RegexUtils.isIpAddress("192.168.1.1"));
            assertFalse(RegexUtils.isIpAddress("999.999.999.999"));
        }

        @Test
        @DisplayName("isNumeric / isAlpha / isAlphanumeric")
        void charTypes() {
            assertTrue(RegexUtils.isNumeric("12345"));
            assertFalse(RegexUtils.isNumeric("123a"));

            assertTrue(RegexUtils.isAlpha("abcDEF"));
            assertFalse(RegexUtils.isAlpha("abc123"));

            assertTrue(RegexUtils.isAlphanumeric("abc123"));
            assertFalse(RegexUtils.isAlphanumeric("abc-123"));
        }

        @Test
        @DisplayName("isDate / isTime / isDateTime")
        void dateTimeFormats() {
            assertTrue(RegexUtils.isDate("2026-02-13"));
            assertFalse(RegexUtils.isDate("13/02/2026"));

            assertTrue(RegexUtils.isTime("14:30:00"));
            assertFalse(RegexUtils.isTime("14:30"));

            assertTrue(RegexUtils.isDateTime("2026-02-13 14:30:00"));
            assertFalse(RegexUtils.isDateTime("2026-02-13T14:30:00"));
        }

        @Test
        @DisplayName("isKorean / isUpperCase / isLowerCase")
        void specialChecks() {
            assertTrue(RegexUtils.isKorean("한글테스트"));
            assertFalse(RegexUtils.isKorean("abc"));

            assertTrue(RegexUtils.isUpperCase("ABC"));
            assertFalse(RegexUtils.isUpperCase("Abc"));

            assertTrue(RegexUtils.isLowerCase("abc"));
            assertFalse(RegexUtils.isLowerCase("Abc"));
        }
    }

    @Nested
    @DisplayName("Pattern/Matcher 유틸")
    class PatternUtils {

        @Test
        @DisplayName("compile")
        void compile() {
            Pattern p = RegexUtils.compile("\\d+");
            assertNotNull(p);

            Pattern pFlags = RegexUtils.compile("hello", Pattern.CASE_INSENSITIVE);
            assertTrue(pFlags.matcher("HELLO").matches());
        }

        @Test
        @DisplayName("escape")
        void escape() {
            String escaped = RegexUtils.escape("a.b+c");
            assertNotNull(escaped);
            assertNull(RegexUtils.escape(null));
        }

        @Test
        @DisplayName("escapeSpecialChars")
        void escapeSpecialChars() {
            String escaped = RegexUtils.escapeSpecialChars("a.b+c");
            assertNotNull(escaped);
            assertNull(RegexUtils.escapeSpecialChars(null));
        }

        @Test
        @DisplayName("matcher")
        void matcher() {
            Matcher m = RegexUtils.matcher("hello123", "\\d+");
            assertNotNull(m);
            assertTrue(m.find());
            assertNull(RegexUtils.matcher(null, "\\d+"));
        }

        @Test
        @DisplayName("isValidRegex")
        void isValidRegex() {
            assertTrue(RegexUtils.isValidRegex("\\d+"));
            assertFalse(RegexUtils.isValidRegex("[invalid"));
            assertFalse(RegexUtils.isValidRegex(null));
        }
    }

    @Test
    @DisplayName("패턴 상수 확인")
    void patternConstants() {
        assertNotNull(RegexUtils.EMAIL_PATTERN);
        assertNotNull(RegexUtils.PHONE_PATTERN);
        assertNotNull(RegexUtils.URL_PATTERN);
        assertNotNull(RegexUtils.IP_PATTERN);
        assertNotNull(RegexUtils.NUMERIC_PATTERN);
        assertNotNull(RegexUtils.ALPHA_PATTERN);
        assertNotNull(RegexUtils.ALPHANUMERIC_PATTERN);
        assertNotNull(RegexUtils.DATE_PATTERN);
        assertNotNull(RegexUtils.TIME_PATTERN);
        assertNotNull(RegexUtils.DATETIME_PATTERN);
    }
}
