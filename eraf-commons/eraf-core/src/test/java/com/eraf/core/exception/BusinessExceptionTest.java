package com.eraf.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode만으로 예외 생성")
    void testCreateWithErrorCode() {
        // Given
        ErrorCode errorCode = CommonErrorCode.BAD_REQUEST;

        // When
        BusinessException exception = new BusinessException(errorCode);

        // Then
        assertEquals("잘못된 요청입니다.", exception.getMessage());
        assertEquals("BAD_REQUEST", exception.getCode());
        assertEquals(400, exception.getStatus());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getArgs());
    }

    @Test
    @DisplayName("ErrorCode와 인자로 예외 생성")
    void testCreateWithArgs() {
        // Given
        ErrorCode errorCode = CommonErrorCode.FEATURE_DISABLED;

        // When
        BusinessException exception = new BusinessException(errorCode, "darkMode");

        // Then
        assertTrue(exception.getMessage().contains("darkMode"));
        assertEquals("FEATURE_DISABLED", exception.getCode());
        assertEquals(403, exception.getStatus());
    }

    @Test
    @DisplayName("ErrorCode와 원인 예외로 생성")
    void testCreateWithCause() {
        // Given
        ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;
        RuntimeException cause = new RuntimeException("Original error");

        // When
        BusinessException exception = new BusinessException(errorCode, cause);

        // Then
        assertEquals("서버 내부 오류가 발생했습니다.", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(500, exception.getStatus());
    }

    @Test
    @DisplayName("ErrorCode, 원인 예외, 인자로 생성")
    void testCreateWithCauseAndArgs() {
        // Given
        ErrorCode errorCode = CommonErrorCode.FEATURE_DISABLED;
        RuntimeException cause = new RuntimeException("Feature error");

        // When
        BusinessException exception = new BusinessException(errorCode, cause, "testFeature");

        // Then
        assertTrue(exception.getMessage().contains("testFeature"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("여러 에러 코드 테스트")
    void testMultipleErrorCodes() {
        // 401 Unauthorized
        BusinessException unauthorized = new BusinessException(CommonErrorCode.UNAUTHORIZED);
        assertEquals(401, unauthorized.getStatus());
        assertEquals("UNAUTHORIZED", unauthorized.getCode());

        // 403 Forbidden
        BusinessException forbidden = new BusinessException(CommonErrorCode.FORBIDDEN);
        assertEquals(403, forbidden.getStatus());

        // 404 Not Found
        BusinessException notFound = new BusinessException(CommonErrorCode.NOT_FOUND);
        assertEquals(404, notFound.getStatus());

        // 409 Conflict
        BusinessException conflict = new BusinessException(CommonErrorCode.CONFLICT);
        assertEquals(409, conflict.getStatus());
    }

    @Test
    @DisplayName("인자 없이 포맷된 메시지 처리")
    void testFormatMessageWithNoArgs() {
        // Given
        BusinessException exception = new BusinessException(CommonErrorCode.FEATURE_DISABLED);

        // Then - 인자 없으면 원본 메시지 그대로
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("getArgs 반환 확인")
    void testGetArgs() {
        // Given
        Object[] args = new Object[]{"arg1", 123};

        // When
        BusinessException exception = new BusinessException(CommonErrorCode.BAD_REQUEST, args);

        // Then
        assertNotNull(exception.getArgs());
        assertEquals(2, exception.getArgs().length);
        assertEquals("arg1", exception.getArgs()[0]);
        assertEquals(123, exception.getArgs()[1]);
    }
}
