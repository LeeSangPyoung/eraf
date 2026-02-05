package com.eraf.core.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TimeoutAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class TimeoutAspectTest {

    private TimeoutAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aspect = new TimeoutAspect();
    }

    @Test
    @DisplayName("타임아웃 내 정상 완료")
    void shouldCompleteWithinTimeout() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenReturn("success");
        Timeout annotation = createAnnotation(5000, TimeUnit.MILLISECONDS);

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    @Test
    @DisplayName("타임아웃 초과 시 TimeoutException 발생")
    void shouldThrowTimeoutException() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(500); // 500ms 대기
            return "delayed";
        });
        Timeout annotation = createAnnotation(100, TimeUnit.MILLISECONDS);

        // When & Then
        assertThrows(TimeoutException.class, () ->
                aspect.around(joinPoint, annotation));
    }

    @Test
    @DisplayName("메서드 예외는 그대로 전파")
    void shouldPropagateMethodException() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("invalid"));
        Timeout annotation = createAnnotation(5000, TimeUnit.MILLISECONDS);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                aspect.around(joinPoint, annotation));
    }

    @Test
    @DisplayName("초 단위 타임아웃")
    void shouldSupportSecondsUnit() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenReturn("success");
        Timeout annotation = createAnnotation(1, TimeUnit.SECONDS);

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    @Test
    @DisplayName("null 반환 처리")
    void shouldHandleNullReturn() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenReturn(null);
        Timeout annotation = createAnnotation(5000, TimeUnit.MILLISECONDS);

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertNull(result);
    }

    private Timeout createAnnotation(long value, TimeUnit unit) {
        return new Timeout() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Timeout.class;
            }

            @Override
            public long value() {
                return value;
            }

            @Override
            public TimeUnit unit() {
                return unit;
            }
        };
    }
}
