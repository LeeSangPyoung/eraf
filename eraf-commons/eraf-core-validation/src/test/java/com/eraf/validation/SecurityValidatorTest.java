package com.eraf.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SecurityValidatorTest {

    // ========== NoXssValidator ==========
    @Nested
    @DisplayName("NoXssValidator (XSS 방지)")
    class NoXssValidatorTest {

        private final NoXssValidator validator = new NoXssValidator();

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Hello World",
                "This is a <b>bold</b> text",
                "Price is $100",
                "foo < bar && baz > 0",
                "<div class='safe'>content</div>"
        })
        @DisplayName("안전한 입력")
        void safeInput(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "<script>alert('xss')</script>",
                "<SCRIPT>alert(1)</SCRIPT>",
                "<script src='evil.js'></script>"
        })
        @DisplayName("script 태그 차단")
        void blockScriptTags(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "<div onclick=alert(1)>",
                "<img onerror=alert(1)>",
                "<body onload=alert(1)>"
        })
        @DisplayName("이벤트 핸들러 차단")
        void blockEventHandlers(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "javascript:alert(1)",
                "JAVASCRIPT:void(0)",
                "vbscript:msgbox"
        })
        @DisplayName("javascript/vbscript 프로토콜 차단")
        void blockScriptProtocols(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "<iframe src='evil.com'>",
                "<object data='evil.swf'>",
                "<embed src='evil'>",
                "<form action='evil'>",
                "<input type='hidden' value='xss'>"
        })
        @DisplayName("위험한 HTML 태그 차단")
        void blockDangerousTags(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @Test
        @DisplayName("data:text/html 차단")
        void blockDataUri() {
            assertFalse(validator.isValid("data: text/html,<script>alert(1)</script>", null));
        }
    }

    // ========== NoSqlInjectionValidator ==========
    @Nested
    @DisplayName("NoSqlInjectionValidator (SQL Injection 방지)")
    class NoSqlInjectionValidatorTest {

        private final NoSqlInjectionValidator validator = new NoSqlInjectionValidator();

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "John Doe",
                "user@example.com",
                "12345",
                "normal text with numbers 123"
        })
        @DisplayName("안전한 입력")
        void safeInput(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "'; DROP TABLE users; --",
                "; delete from users",
                "; truncate table users",
                "; update users set admin=1"
        })
        @DisplayName("SQL 명령어 차단")
        void blockSqlCommands(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1 or 1=1",
                "admin' and 1=1",
                "test' OR 1=1"
        })
        @DisplayName("논리 조건 삽입 차단")
        void blockLogicalConditions(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "UNION SELECT * FROM users",
                "select password from users",
                "insert into users values",
                "delete from sessions",
                "drop table users",
                "truncate table logs"
        })
        @DisplayName("SQL 키워드 조합 차단")
        void blockSqlKeywords(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @Test
        @DisplayName("주석 문자 차단")
        void blockCommentChars() {
            assertFalse(validator.isValid("admin'--", null));
            assertFalse(validator.isValid("admin#", null));
        }

        @Test
        @DisplayName("싱글 쿼트 차단")
        void blockSingleQuote() {
            assertFalse(validator.isValid("O'Brien", null));
        }
    }

    // ========== NoPathTraversalValidator ==========
    @Nested
    @DisplayName("NoPathTraversalValidator (Path Traversal 방지)")
    class NoPathTraversalValidatorTest {

        private final NoPathTraversalValidator validator = new NoPathTraversalValidator();

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "document.pdf",
                "path/to/file.txt",
                "uploads/image.jpg",
                "normal-filename"
        })
        @DisplayName("안전한 경로")
        void safePaths(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "../etc/passwd",
                "..\\windows\\system32",
                "path/../../secret",
                "files/../../../etc/shadow"
        })
        @DisplayName("기본 path traversal 차단")
        void blockBasicTraversal(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "%2e%2e/etc/passwd",
                "%2e%2e\\secret",
                "%252e%252e/etc/passwd",
                "%252e%252e\\secret"
        })
        @DisplayName("URL 인코딩된 path traversal 차단")
        void blockEncodedTraversal(String value) {
            assertFalse(validator.isValid(value, null));
        }

        @Test
        @DisplayName("시작이 ../ 인 경우 차단")
        void blockStartsWithTraversal() {
            assertFalse(validator.isValid("../secret", null));
            assertFalse(validator.isValid("..\\secret", null));
        }

        @Test
        @DisplayName("슬래시 뒤 .. 차단")
        void blockSlashDotDot() {
            assertFalse(validator.isValid("/..secret", null));
            assertFalse(validator.isValid("\\..\\secret", null));
        }
    }
}
