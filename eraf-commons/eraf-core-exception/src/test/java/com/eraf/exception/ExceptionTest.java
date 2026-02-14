package com.eraf.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    // ========== BusinessException ==========
    @Nested
    @DisplayName("BusinessException")
    class BusinessExceptionTest {

        @Test
        @DisplayName("ErrorCode로 생성")
        void createWithErrorCode() {
            BusinessException ex = new BusinessException(CommonErrorCode.NOT_FOUND);

            assertEquals("NOT_FOUND", ex.getCode());
            assertEquals(404, ex.getStatus());
            assertEquals("리소스를 찾을 수 없습니다.", ex.getMessage());
            assertSame(CommonErrorCode.NOT_FOUND, ex.getErrorCode());
            assertNull(ex.getArgs());
        }

        @Test
        @DisplayName("ErrorCode + 인자로 생성 (메시지 포맷팅)")
        void createWithArgs() {
            BusinessException ex = new BusinessException(CommonErrorCode.FEATURE_DISABLED, "myFeature");

            assertEquals("FEATURE_DISABLED", ex.getCode());
            assertEquals(403, ex.getStatus());
            assertEquals("기능이 비활성화되어 있습니다: myFeature", ex.getMessage());
            assertNotNull(ex.getArgs());
            assertEquals("myFeature", ex.getArgs()[0]);
        }

        @Test
        @DisplayName("ErrorCode + cause로 생성")
        void createWithCause() {
            RuntimeException cause = new RuntimeException("original");
            BusinessException ex = new BusinessException(CommonErrorCode.INTERNAL_ERROR, cause);

            assertSame(cause, ex.getCause());
            assertEquals("INTERNAL_ERROR", ex.getCode());
            assertEquals(500, ex.getStatus());
        }

        @Test
        @DisplayName("ErrorCode + cause + args로 생성")
        void createWithCauseAndArgs() {
            RuntimeException cause = new RuntimeException("original");
            BusinessException ex = new BusinessException(
                    CommonErrorCode.FEATURE_FLAG_NOT_FOUND, cause, "flag-key");

            assertSame(cause, ex.getCause());
            assertEquals("Feature Flag를 찾을 수 없습니다: flag-key", ex.getMessage());
            assertEquals("FEATURE_FLAG_NOT_FOUND", ex.getCode());
        }

        @Test
        @DisplayName("메시지 포맷 실패 시 원본 메시지 반환")
        void formatMessageFallback() {
            // args가 null인 경우
            BusinessException ex = new BusinessException(CommonErrorCode.BAD_REQUEST);
            assertEquals("잘못된 요청입니다.", ex.getMessage());
        }

        @Test
        @DisplayName("빈 args 배열")
        void emptyArgs() {
            BusinessException ex = new BusinessException(CommonErrorCode.BAD_REQUEST, new Object[]{});
            assertEquals("잘못된 요청입니다.", ex.getMessage());
        }
    }

    // ========== SystemException ==========
    @Nested
    @DisplayName("SystemException")
    class SystemExceptionTest {

        @Test
        @DisplayName("메시지로 생성 (기본 에러코드)")
        void createWithMessage() {
            SystemException ex = new SystemException("시스템 오류 발생");

            assertEquals("SYSTEM_ERROR", ex.getErrorCode());
            assertEquals("시스템 오류 발생", ex.getMessage());
        }

        @Test
        @DisplayName("에러코드 + 메시지로 생성")
        void createWithCodeAndMessage() {
            SystemException ex = new SystemException("DB_CONNECTION_ERROR", "DB 연결 실패");

            assertEquals("DB_CONNECTION_ERROR", ex.getErrorCode());
            assertEquals("DB 연결 실패", ex.getMessage());
        }

        @Test
        @DisplayName("메시지 + cause로 생성")
        void createWithMessageAndCause() {
            Exception cause = new Exception("root cause");
            SystemException ex = new SystemException("시스템 오류", cause);

            assertEquals("SYSTEM_ERROR", ex.getErrorCode());
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("에러코드 + 메시지 + cause로 생성")
        void createWithAll() {
            Exception cause = new Exception("root cause");
            SystemException ex = new SystemException("TIMEOUT", "요청 시간 초과", cause);

            assertEquals("TIMEOUT", ex.getErrorCode());
            assertEquals("요청 시간 초과", ex.getMessage());
            assertSame(cause, ex.getCause());
        }
    }

    // ========== ValidationException ==========
    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTest {

        @Test
        @DisplayName("메시지로 생성")
        void createWithMessage() {
            ValidationException ex = new ValidationException("검증 실패");

            assertEquals("검증 실패", ex.getMessage());
            assertFalse(ex.hasFieldErrors());
            assertTrue(ex.getFieldErrors().isEmpty());
        }

        @Test
        @DisplayName("필드 + 메시지로 생성")
        void createWithFieldAndMessage() {
            ValidationException ex = new ValidationException("email", "이메일 형식이 잘못됐습니다");

            assertEquals("이메일 형식이 잘못됐습니다", ex.getMessage());
            assertTrue(ex.hasFieldErrors());
            assertEquals(1, ex.getFieldErrors().size());
            assertEquals("이메일 형식이 잘못됐습니다", ex.getFieldErrors().get("email"));
        }

        @Test
        @DisplayName("필드 에러 맵으로 생성")
        void createWithFieldErrorMap() {
            Map<String, String> errors = Map.of(
                    "email", "이메일 필수",
                    "name", "이름 필수"
            );
            ValidationException ex = new ValidationException(errors);

            assertEquals("유효성 검증에 실패했습니다.", ex.getMessage());
            assertTrue(ex.hasFieldErrors());
            assertEquals(2, ex.getFieldErrors().size());
        }

        @Test
        @DisplayName("null 필드 에러 맵 처리")
        void nullFieldErrorMap() {
            ValidationException ex = new ValidationException((Map<String, String>) null);
            assertFalse(ex.hasFieldErrors());
        }

        @Test
        @DisplayName("fieldErrors는 불변")
        void fieldErrorsImmutable() {
            ValidationException ex = new ValidationException("email", "에러");
            assertThrows(UnsupportedOperationException.class,
                    () -> ex.getFieldErrors().put("hack", "value"));
        }

        @Nested
        @DisplayName("Builder")
        class BuilderTest {

            @Test
            @DisplayName("기본 빌더 사용")
            void basicBuilder() {
                ValidationException ex = ValidationException.builder()
                        .message("커스텀 메시지")
                        .addFieldError("name", "이름 필수")
                        .addFieldError("age", "나이 필수")
                        .build();

                assertEquals("커스텀 메시지", ex.getMessage());
                assertEquals(2, ex.getFieldErrors().size());
            }

            @Test
            @DisplayName("addFieldErrors 벌크 추가")
            void addFieldErrors() {
                Map<String, String> errors = Map.of("a", "err1", "b", "err2");
                ValidationException ex = ValidationException.builder()
                        .addFieldErrors(errors)
                        .build();

                assertEquals(2, ex.getFieldErrors().size());
            }

            @Test
            @DisplayName("hasErrors 확인")
            void hasErrors() {
                ValidationException.Builder builder = ValidationException.builder();
                assertFalse(builder.hasErrors());

                builder.addFieldError("field", "error");
                assertTrue(builder.hasErrors());
            }

            @Test
            @DisplayName("throwIfErrors - 에러 있으면 예외 발생")
            void throwIfErrorsWithErrors() {
                assertThrows(ValidationException.class, () ->
                        ValidationException.builder()
                                .addFieldError("field", "error")
                                .throwIfErrors());
            }

            @Test
            @DisplayName("throwIfErrors - 에러 없으면 통과")
            void throwIfErrorsNoErrors() {
                assertDoesNotThrow(() ->
                        ValidationException.builder().throwIfErrors());
            }
        }
    }

    // ========== CommonErrorCode ==========
    @Nested
    @DisplayName("CommonErrorCode")
    class CommonErrorCodeTest {

        @Test
        @DisplayName("ErrorCode 인터페이스 구현")
        void implementsErrorCode() {
            ErrorCode ec = CommonErrorCode.BAD_REQUEST;
            assertEquals("BAD_REQUEST", ec.getCode());
            assertEquals("잘못된 요청입니다.", ec.getMessage());
            assertEquals(400, ec.getStatus());
        }

        @Test
        @DisplayName("4xx 에러 코드 HTTP 상태")
        void clientErrorCodes() {
            assertEquals(400, CommonErrorCode.BAD_REQUEST.getStatus());
            assertEquals(400, CommonErrorCode.INVALID_INPUT.getStatus());
            assertEquals(400, CommonErrorCode.VALIDATION_ERROR.getStatus());
            assertEquals(401, CommonErrorCode.UNAUTHORIZED.getStatus());
            assertEquals(401, CommonErrorCode.INVALID_TOKEN.getStatus());
            assertEquals(401, CommonErrorCode.EXPIRED_TOKEN.getStatus());
            assertEquals(403, CommonErrorCode.FORBIDDEN.getStatus());
            assertEquals(403, CommonErrorCode.ACCESS_DENIED.getStatus());
            assertEquals(404, CommonErrorCode.NOT_FOUND.getStatus());
            assertEquals(404, CommonErrorCode.RESOURCE_NOT_FOUND.getStatus());
            assertEquals(409, CommonErrorCode.CONFLICT.getStatus());
            assertEquals(409, CommonErrorCode.DUPLICATE_RESOURCE.getStatus());
        }

        @Test
        @DisplayName("5xx 에러 코드 HTTP 상태")
        void serverErrorCodes() {
            assertEquals(500, CommonErrorCode.INTERNAL_ERROR.getStatus());
            assertEquals(500, CommonErrorCode.SYSTEM_ERROR.getStatus());
        }

        @Test
        @DisplayName("Feature Flag 에러 코드")
        void featureFlagErrorCodes() {
            assertEquals(404, CommonErrorCode.FEATURE_FLAG_NOT_FOUND.getStatus());
            assertEquals(409, CommonErrorCode.FEATURE_FLAG_ALREADY_EXISTS.getStatus());
            assertEquals(400, CommonErrorCode.INVALID_TARGETING_RULES.getStatus());
            assertEquals(400, CommonErrorCode.INVALID_FALLBACK_VALUE.getStatus());
        }

        @Test
        @DisplayName("모든 에러 코드는 non-null 값")
        void allCodesNonNull() {
            for (CommonErrorCode code : CommonErrorCode.values()) {
                assertNotNull(code.getCode());
                assertNotNull(code.getMessage());
                assertTrue(code.getStatus() >= 400);
            }
        }
    }
}
