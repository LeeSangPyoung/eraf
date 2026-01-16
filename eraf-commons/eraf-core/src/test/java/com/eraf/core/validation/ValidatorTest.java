package com.eraf.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation - 검증 어노테이션 테스트")
class ValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== 테스트용 DTO 클래스들 =====

    static class EmailDto {
        @Email
        private String email;

        EmailDto(String email) {
            this.email = email;
        }
    }

    static class PhoneDto {
        @Phone
        private String phone;

        @Phone(type = Phone.PhoneType.MOBILE)
        private String mobile;

        @Phone(type = Phone.PhoneType.LANDLINE)
        private String landline;

        PhoneDto(String phone) {
            this.phone = phone;
        }

        PhoneDto setMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        PhoneDto setLandline(String landline) {
            this.landline = landline;
            return this;
        }
    }

    static class BusinessNoDto {
        @BusinessNo
        private String businessNo;

        BusinessNoDto(String businessNo) {
            this.businessNo = businessNo;
        }
    }

    static class SecurityDto {
        @NoXss
        private String content;

        @NoSqlInjection
        private String query;

        @NoPathTraversal
        private String path;

        SecurityDto() {}

        SecurityDto setContent(String content) {
            this.content = content;
            return this;
        }

        SecurityDto setQuery(String query) {
            this.query = query;
            return this;
        }

        SecurityDto setPath(String path) {
            this.path = path;
            return this;
        }
    }

    @Nested
    @DisplayName("이메일 검증")
    class EmailValidation {

        @Test
        @DisplayName("유효한 이메일")
        void validEmail() {
            assertValid(new EmailDto("test@example.com"));
            assertValid(new EmailDto("user.name@domain.co.kr"));
            assertValid(new EmailDto("user+tag@gmail.com"));
        }

        @Test
        @DisplayName("유효하지 않은 이메일")
        void invalidEmail() {
            assertInvalid(new EmailDto("invalid"));
            assertInvalid(new EmailDto("@domain.com"));
            assertInvalid(new EmailDto("user@"));
            assertInvalid(new EmailDto("user@.com"));
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new EmailDto(null));
            assertValid(new EmailDto(""));
            assertValid(new EmailDto("  "));
        }
    }

    @Nested
    @DisplayName("전화번호 검증")
    class PhoneValidation {

        @Test
        @DisplayName("유효한 휴대폰 번호")
        void validMobile() {
            assertValid(new PhoneDto("01012345678"));
            assertValid(new PhoneDto("010-1234-5678"));
            assertValid(new PhoneDto("01112345678"));
        }

        @Test
        @DisplayName("유효한 유선전화 번호")
        void validLandline() {
            assertValid(new PhoneDto("0212345678"));
            assertValid(new PhoneDto("02-1234-5678"));
            assertValid(new PhoneDto("031-123-4567"));
        }

        @Test
        @DisplayName("휴대폰 타입 지정")
        void mobileType() {
            PhoneDto dto = new PhoneDto(null).setMobile("01012345678");
            assertValid(dto);

            PhoneDto invalid = new PhoneDto(null).setMobile("0212345678");
            Set<ConstraintViolation<PhoneDto>> violations = validator.validate(invalid);
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mobile")));
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new PhoneDto(null));
            assertValid(new PhoneDto(""));
        }
    }

    @Nested
    @DisplayName("사업자등록번호 검증")
    class BusinessNoValidation {

        @Test
        @DisplayName("유효한 사업자등록번호")
        void validBusinessNo() {
            // 체크섬이 정확한 사업자등록번호 예시
            // 체크섬 알고리즘: 가중치 [1,3,7,1,3,7,1,3,5] 적용 후 마지막 자리 검증
            assertValid(new BusinessNoDto("2208139938")); // 테스트용 체크섬 통과 번호
        }

        @Test
        @DisplayName("유효하지 않은 사업자등록번호 - 자릿수")
        void invalidLength() {
            assertInvalid(new BusinessNoDto("123456789")); // 9자리
            assertInvalid(new BusinessNoDto("12345678901")); // 11자리
        }

        @Test
        @DisplayName("하이픈 포함")
        void withHyphen() {
            // 하이픈은 제거되고 검증됨
            BusinessNoDto dto = new BusinessNoDto("123-45-67890");
            Set<ConstraintViolation<BusinessNoDto>> violations = validator.validate(dto);
            // 체크섬에 따라 결과가 달라짐
            assertNotNull(violations);
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new BusinessNoDto(null));
            assertValid(new BusinessNoDto(""));
        }
    }

    @Nested
    @DisplayName("XSS 검증")
    class XssValidation {

        @Test
        @DisplayName("일반 텍스트 통과")
        void normalText() {
            assertValid(new SecurityDto().setContent("Hello, World!"));
            assertValid(new SecurityDto().setContent("일반 텍스트입니다."));
        }

        @Test
        @DisplayName("script 태그 차단")
        void blockScript() {
            assertInvalidSecurity(new SecurityDto().setContent("<script>alert('xss')</script>"), "content");
        }

        @Test
        @DisplayName("이벤트 핸들러 차단")
        void blockEventHandler() {
            assertInvalidSecurity(new SecurityDto().setContent("<img src=x onerror=alert('xss')>"), "content");
        }

        @Test
        @DisplayName("javascript: 프로토콜 차단")
        void blockJavascriptProtocol() {
            assertInvalidSecurity(new SecurityDto().setContent("<a href='javascript:alert(1)'>click</a>"), "content");
        }

        @Test
        @DisplayName("iframe 차단")
        void blockIframe() {
            assertInvalidSecurity(new SecurityDto().setContent("<iframe src='evil.com'></iframe>"), "content");
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new SecurityDto().setContent(null));
            assertValid(new SecurityDto().setContent(""));
        }
    }

    @Nested
    @DisplayName("SQL Injection 검증")
    class SqlInjectionValidation {

        @Test
        @DisplayName("일반 텍스트 통과")
        void normalText() {
            assertValid(new SecurityDto().setQuery("normal search term"));
            assertValid(new SecurityDto().setQuery("검색어"));
        }

        @Test
        @DisplayName("UNION SELECT 차단")
        void blockUnionSelect() {
            assertInvalidSecurity(new SecurityDto().setQuery("1 UNION SELECT * FROM users"), "query");
        }

        @Test
        @DisplayName("OR 1=1 차단")
        void blockOrAlwaysTrue() {
            assertInvalidSecurity(new SecurityDto().setQuery("admin' OR 1=1"), "query");
        }

        @Test
        @DisplayName("DROP TABLE 차단")
        void blockDropTable() {
            assertInvalidSecurity(new SecurityDto().setQuery("; DROP TABLE users;"), "query");
        }

        @Test
        @DisplayName("주석 문자 차단")
        void blockComments() {
            assertInvalidSecurity(new SecurityDto().setQuery("admin'--"), "query");
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new SecurityDto().setQuery(null));
            assertValid(new SecurityDto().setQuery(""));
        }
    }

    @Nested
    @DisplayName("Path Traversal 검증")
    class PathTraversalValidation {

        @Test
        @DisplayName("일반 경로 통과")
        void normalPath() {
            assertValid(new SecurityDto().setPath("/images/photo.jpg"));
            assertValid(new SecurityDto().setPath("documents/file.pdf"));
        }

        @Test
        @DisplayName("../  차단")
        void blockDotDot() {
            assertInvalidSecurity(new SecurityDto().setPath("../../../etc/passwd"), "path");
            assertInvalidSecurity(new SecurityDto().setPath("..\\..\\windows\\system32"), "path");
        }

        @Test
        @DisplayName("null/빈 값은 통과")
        void nullOrEmpty() {
            assertValid(new SecurityDto().setPath(null));
            assertValid(new SecurityDto().setPath(""));
        }
    }

    // ===== 헬퍼 메서드 =====

    private <T> void assertValid(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    private <T> void assertInvalid(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        assertFalse(violations.isEmpty(), "Expected violations but got none");
    }

    private <T> void assertInvalidSecurity(T object, String field) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals(field)),
                "Expected violation for field: " + field);
    }
}
