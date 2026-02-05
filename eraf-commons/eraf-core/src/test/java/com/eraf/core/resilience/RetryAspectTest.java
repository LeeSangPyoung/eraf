package com.eraf.core.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RetryAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class RetryAspectTest {

    private RetryAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aspect = new RetryAspect();
    }

    @Test
    @DisplayName("첫 시도에 성공하면 재시도 없음")
    void shouldNotRetryOnFirstSuccess() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenReturn("success");
        Retry annotation = createAnnotation(3, 10, 1.0, 0, new Class[]{}, new Class[]{});

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("실패 후 재시도하여 성공")
    void shouldRetryAndSucceed() throws Throwable {
        // Given
        when(joinPoint.proceed())
                .thenThrow(new RuntimeException("fail"))
                .thenThrow(new RuntimeException("fail"))
                .thenReturn("success");

        Retry annotation = createAnnotation(3, 10, 1.0, 0, new Class[]{}, new Class[]{});

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
        verify(joinPoint, times(3)).proceed();
    }

    @Test
    @DisplayName("최대 재시도 후에도 실패하면 예외")
    void shouldThrowAfterMaxRetries() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenThrow(new RuntimeException("always fail"));
        Retry annotation = createAnnotation(3, 10, 1.0, 0, new Class[]{}, new Class[]{});

        // When & Then
        assertThrows(RuntimeException.class, () ->
                aspect.around(joinPoint, annotation));

        verify(joinPoint, times(3)).proceed();
    }

    @Test
    @DisplayName("retryOn에 지정된 예외만 재시도")
    void shouldOnlyRetryOnSpecifiedExceptions() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenThrow(new IOException("io error"));
        Retry annotation = createAnnotation(3, 10, 1.0, 0,
                new Class[]{IOException.class}, new Class[]{});

        // When & Then
        assertThrows(IOException.class, () ->
                aspect.around(joinPoint, annotation));

        verify(joinPoint, times(3)).proceed();
    }

    @Test
    @DisplayName("retryOn에 없는 예외는 즉시 실패")
    void shouldNotRetryOnNonMatchingException() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("invalid"));
        Retry annotation = createAnnotation(3, 10, 1.0, 0,
                new Class[]{IOException.class}, new Class[]{});

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                aspect.around(joinPoint, annotation));

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("ignoreOn에 지정된 예외는 재시도 안함")
    void shouldNotRetryOnIgnoredExceptions() throws Throwable {
        // Given
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("ignored"));
        Retry annotation = createAnnotation(3, 10, 1.0, 0,
                new Class[]{}, new Class[]{IllegalArgumentException.class});

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                aspect.around(joinPoint, annotation));

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("지수 백오프 적용")
    void shouldApplyExponentialBackoff() throws Throwable {
        // Given
        when(joinPoint.proceed())
                .thenThrow(new RuntimeException("fail"))
                .thenThrow(new RuntimeException("fail"))
                .thenReturn("success");

        Retry annotation = createAnnotation(3, 50, 2.0, 1000, new Class[]{}, new Class[]{});
        long startTime = System.currentTimeMillis();

        // When
        aspect.around(joinPoint, annotation);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Then - 50ms + 100ms = 최소 150ms
        assertTrue(elapsedTime >= 100, "Expected at least 100ms delay, got " + elapsedTime);
    }

    @Test
    @DisplayName("maxDelay 제한 적용")
    void shouldRespectMaxDelay() throws Throwable {
        // Given
        when(joinPoint.proceed())
                .thenThrow(new RuntimeException("fail"))
                .thenThrow(new RuntimeException("fail"))
                .thenReturn("success");

        // multiplier가 큰 경우에도 maxDelay로 제한
        Retry annotation = createAnnotation(3, 100, 10.0, 150, new Class[]{}, new Class[]{});
        long startTime = System.currentTimeMillis();

        // When
        aspect.around(joinPoint, annotation);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Then - 100ms + 150ms(maxDelay) = 250ms 이하
        assertTrue(elapsedTime < 500, "Max delay should be applied");
    }

    @SuppressWarnings("unchecked")
    private Retry createAnnotation(int maxAttempts, long delay, double multiplier,
                                    long maxDelay, Class<?>[] retryOn, Class<?>[] ignoreOn) {
        return new Retry() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Retry.class;
            }

            @Override
            public int maxAttempts() {
                return maxAttempts;
            }

            @Override
            public long delay() {
                return delay;
            }

            @Override
            public double multiplier() {
                return multiplier;
            }

            @Override
            public long maxDelay() {
                return maxDelay;
            }

            @Override
            public Class<? extends Throwable>[] retryOn() {
                return (Class<? extends Throwable>[]) retryOn;
            }

            @Override
            public Class<? extends Throwable>[] ignoreOn() {
                return (Class<? extends Throwable>[]) ignoreOn;
            }
        };
    }
}
