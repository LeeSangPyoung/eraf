package com.eraf.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BasicValidatorTest {

    // ========== EmailValidator ==========
    @Nested
    @DisplayName("EmailValidator")
    class EmailValidatorTest {

        private final EmailValidator validator = new EmailValidator();

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효 (다른 어노테이션에 위임)")
        void nullAndBlankAreValid(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "test.name@domain.co.kr",
                "user+tag@gmail.com",
                "a@b.cc",
                "user123@sub.domain.com"
        })
        @DisplayName("유효한 이메일 주소")
        void validEmails(String email) {
            assertTrue(validator.isValid(email, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "plaintext",
                "@missing-local.com",
                "user@",
                "user@.com",
                "user@domain",
                "user space@domain.com"
        })
        @DisplayName("유효하지 않은 이메일 주소")
        void invalidEmails(String email) {
            assertFalse(validator.isValid(email, null));
        }
    }

    // ========== BusinessNoValidator ==========
    @Nested
    @DisplayName("BusinessNoValidator (사업자등록번호)")
    class BusinessNoValidatorTest {

        private final BusinessNoValidator validator = new BusinessNoValidator();

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            assertTrue(validator.isValid(value, null));
        }

        @Test
        @DisplayName("유효한 사업자등록번호 (체크섬 통과)")
        void validBusinessNo() {
            // 1234567891: sum=1+6+21+4+15+42+7+24+45+(9*5/10)=169, checkDigit=(10-9)%10=1, last=1 -> valid
            assertTrue(validator.isValid("1234567891", null));
        }

        @Test
        @DisplayName("하이픈 포함 사업자등록번호")
        void validWithHyphens() {
            // 하이픈 제거 후 검증되므로 동일 결과
            String cleaned = "1234567891";
            String withHyphens = cleaned.substring(0, 3) + "-" + cleaned.substring(3, 5) + "-" + cleaned.substring(5);
            assertEquals(validator.isValid(cleaned, null), validator.isValid(withHyphens, null));
        }

        @Test
        @DisplayName("체크섬 불일치 시 유효하지 않음")
        void invalidChecksum() {
            // 마지막 자리만 바꿔서 체크섬 실패
            assertFalse(validator.isValid("1234567890", null));
        }

        @Test
        @DisplayName("10자리가 아닌 경우 유효하지 않음")
        void invalidLength() {
            assertFalse(validator.isValid("12345", null));
            assertFalse(validator.isValid("12345678901", null));
        }

        @Test
        @DisplayName("숫자가 아닌 문자 포함 시 길이 체크")
        void nonNumeric() {
            assertFalse(validator.isValid("abcdefghij", null));
        }
    }

    // ========== PhoneValidator ==========
    @Nested
    @DisplayName("PhoneValidator (전화번호)")
    class PhoneValidatorTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            PhoneValidator validator = createValidator(Phone.PhoneType.ALL);
            assertTrue(validator.isValid(value, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "01012345678",
                "010-1234-5678",
                "01112345678",
                "01612345678"
        })
        @DisplayName("유효한 휴대폰 번호 (MOBILE)")
        void validMobile(String phone) {
            PhoneValidator validator = createValidator(Phone.PhoneType.MOBILE);
            assertTrue(validator.isValid(phone, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "0212345678",
                "02-1234-5678",
                "03112345678",
                "031-123-4567"
        })
        @DisplayName("유효한 유선전화 번호 (LANDLINE)")
        void validLandline(String phone) {
            PhoneValidator validator = createValidator(Phone.PhoneType.LANDLINE);
            assertTrue(validator.isValid(phone, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "01012345678",
                "0212345678",
                "031-123-4567"
        })
        @DisplayName("ALL 타입은 모두 허용")
        void validAll(String phone) {
            PhoneValidator validator = createValidator(Phone.PhoneType.ALL);
            assertTrue(validator.isValid(phone, null));
        }

        @Test
        @DisplayName("유선번호를 MOBILE로 검증하면 실패")
        void landlineNotMobile() {
            PhoneValidator validator = createValidator(Phone.PhoneType.MOBILE);
            assertFalse(validator.isValid("0212345678", null));
        }

        private PhoneValidator createValidator(Phone.PhoneType type) {
            PhoneValidator validator = new PhoneValidator();
            Phone mockAnnotation = mock(Phone.class);
            when(mockAnnotation.type()).thenReturn(type);
            validator.initialize(mockAnnotation);
            return validator;
        }
    }

    // ========== PasswordValidator ==========
    @Nested
    @DisplayName("PasswordValidator (비밀번호)")
    class PasswordValidatorTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            PasswordValidator validator = createDefaultValidator();
            assertTrue(validator.isValid(value, mockContext()));
        }

        @Test
        @DisplayName("유효한 비밀번호 (기본 설정)")
        void validPassword() {
            PasswordValidator validator = createDefaultValidator();
            // 연속/반복 없는 비밀번호: Xz9m!@Qw
            assertTrue(validator.isValid("Xz9m!@Qw", mockContext()));
        }

        @Test
        @DisplayName("길이 부족 시 실패")
        void tooShort() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Ab1!", mockContext()));
        }

        @Test
        @DisplayName("길이 초과 시 실패")
        void tooLong() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Abcdefghij12345!@#$%^", mockContext()));
        }

        @Test
        @DisplayName("대문자 없으면 실패")
        void missingUppercase() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("abcd12!@", mockContext()));
        }

        @Test
        @DisplayName("소문자 없으면 실패")
        void missingLowercase() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("ABCD12!@", mockContext()));
        }

        @Test
        @DisplayName("숫자 없으면 실패")
        void missingDigit() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Abcdefg!", mockContext()));
        }

        @Test
        @DisplayName("특수문자 없으면 실패")
        void missingSpecial() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Abcdefg1", mockContext()));
        }

        @Test
        @DisplayName("연속 문자 검사 (abc)")
        void consecutiveChars() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Xabcd1!@", mockContext()));
        }

        @Test
        @DisplayName("반복 문자 검사 (aaa)")
        void repeatedChars() {
            PasswordValidator validator = createDefaultValidator();
            assertFalse(validator.isValid("Xaaab1!@", mockContext()));
        }

        @Test
        @DisplayName("연속/반복 검사 비활성화")
        void disableConsecutiveAndRepeated() {
            PasswordValidator validator = createValidator(8, 20, true, true, true, true, 0, 0);
            ConstraintValidatorContext ctx = mockContext();
            assertTrue(validator.isValid("Xaaab1!@", ctx));
            assertTrue(validator.isValid("Xabcd1!@", ctx));
        }

        @Test
        @DisplayName("특수문자 요구 비활성화")
        void noSpecialRequired() {
            PasswordValidator validator = createValidator(8, 20, true, true, true, false, 0, 0);
            assertTrue(validator.isValid("Abcdefg1", mockContext()));
        }

        private ConstraintValidatorContext mockContext() {
            ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);
            ConstraintValidatorContext.ConstraintViolationBuilder builder =
                    mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            when(ctx.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            return ctx;
        }

        private PasswordValidator createDefaultValidator() {
            return createValidator(8, 20, true, true, true, true, 3, 3);
        }

        private PasswordValidator createValidator(
                int minLen, int maxLen,
                boolean upper, boolean lower, boolean digit, boolean special,
                int maxConsecutive, int maxRepeated) {

            PasswordValidator validator = new PasswordValidator();
            Password mockAnnotation = mock(Password.class);
            when(mockAnnotation.minLength()).thenReturn(minLen);
            when(mockAnnotation.maxLength()).thenReturn(maxLen);
            when(mockAnnotation.requireUppercase()).thenReturn(upper);
            when(mockAnnotation.requireLowercase()).thenReturn(lower);
            when(mockAnnotation.requireDigit()).thenReturn(digit);
            when(mockAnnotation.requireSpecial()).thenReturn(special);
            when(mockAnnotation.maxConsecutive()).thenReturn(maxConsecutive);
            when(mockAnnotation.maxRepeated()).thenReturn(maxRepeated);
            validator.initialize(mockAnnotation);
            return validator;
        }
    }

    // ========== FileExtensionValidator ==========
    @Nested
    @DisplayName("FileExtensionValidator (파일 확장자)")
    class FileExtensionValidatorTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("null, 빈값, 공백은 유효")
        void nullAndBlankAreValid(String value) {
            FileExtensionValidator validator = createDeniedValidator("exe", "bat");
            assertTrue(validator.isValid(value, null));
        }

        @Test
        @DisplayName("허용 목록 기반 검증")
        void allowedExtensions() {
            FileExtensionValidator validator = createAllowedValidator("jpg", "png", "gif");
            assertTrue(validator.isValid("photo.jpg", null));
            assertTrue(validator.isValid("photo.png", null));
            assertFalse(validator.isValid("file.exe", null));
            assertFalse(validator.isValid("file.pdf", null));
        }

        @Test
        @DisplayName("금지 목록 기반 검증")
        void deniedExtensions() {
            FileExtensionValidator validator = createDeniedValidator("exe", "bat", "sh");
            assertTrue(validator.isValid("document.pdf", null));
            assertTrue(validator.isValid("photo.jpg", null));
            assertFalse(validator.isValid("malware.exe", null));
            assertFalse(validator.isValid("script.bat", null));
        }

        @Test
        @DisplayName("대소문자 무시 (기본)")
        void caseInsensitive() {
            FileExtensionValidator validator = createDeniedValidator("exe");
            assertFalse(validator.isValid("file.EXE", null));
            assertFalse(validator.isValid("file.Exe", null));
        }

        @Test
        @DisplayName("대소문자 구분")
        void caseSensitive() {
            FileExtensionValidator validator = createValidator(new String[]{}, new String[]{"exe"}, true);
            assertFalse(validator.isValid("file.exe", null));
            assertTrue(validator.isValid("file.EXE", null));
        }

        @Test
        @DisplayName("확장자 없는 파일은 유효")
        void noExtension() {
            FileExtensionValidator validator = createDeniedValidator("exe");
            assertTrue(validator.isValid("filename", null));
            assertTrue(validator.isValid("filename.", null));
        }

        private FileExtensionValidator createAllowedValidator(String... allowed) {
            return createValidator(allowed, new String[]{}, false);
        }

        private FileExtensionValidator createDeniedValidator(String... denied) {
            return createValidator(new String[]{}, denied, false);
        }

        private FileExtensionValidator createValidator(String[] allowed, String[] denied, boolean caseSensitive) {
            FileExtensionValidator validator = new FileExtensionValidator();
            FileExtension mockAnnotation = mock(FileExtension.class);
            when(mockAnnotation.allowed()).thenReturn(allowed);
            when(mockAnnotation.denied()).thenReturn(denied);
            when(mockAnnotation.caseSensitive()).thenReturn(caseSensitive);
            validator.initialize(mockAnnotation);
            return validator;
        }
    }
}
