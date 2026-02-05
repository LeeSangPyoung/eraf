package com.eraf.core.resilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Retry 어노테이션 테스트
 */
class RetryAnnotationTest {

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() throws NoSuchMethodException {
        Retry annotation = TestService.class.getMethod("defaultMethod")
                .getAnnotation(Retry.class);

        assertEquals(3, annotation.maxAttempts());
        assertEquals(1000, annotation.delay());
        assertEquals(1.0, annotation.multiplier());
        assertEquals(0, annotation.maxDelay());
        assertEquals(0, annotation.retryOn().length);
        assertEquals(0, annotation.ignoreOn().length);
    }

    @Test
    @DisplayName("커스텀 재시도 횟수 설정")
    void shouldAllowCustomMaxAttempts() throws NoSuchMethodException {
        Retry annotation = TestService.class.getMethod("customAttemptsMethod")
                .getAnnotation(Retry.class);

        assertEquals(5, annotation.maxAttempts());
    }

    @Test
    @DisplayName("지수 백오프 설정")
    void shouldAllowExponentialBackoff() throws NoSuchMethodException {
        Retry annotation = TestService.class.getMethod("exponentialBackoffMethod")
                .getAnnotation(Retry.class);

        assertEquals(100, annotation.delay());
        assertEquals(2.0, annotation.multiplier());
        assertEquals(5000, annotation.maxDelay());
    }

    @Test
    @DisplayName("재시도 대상 예외 지정")
    void shouldAllowRetryOnSpecificExceptions() throws NoSuchMethodException {
        Retry annotation = TestService.class.getMethod("retryOnMethod")
                .getAnnotation(Retry.class);

        assertEquals(2, annotation.retryOn().length);
        assertEquals(IOException.class, annotation.retryOn()[0]);
        assertEquals(RuntimeException.class, annotation.retryOn()[1]);
    }

    @Test
    @DisplayName("재시도 제외 예외 지정")
    void shouldAllowIgnoreOnSpecificExceptions() throws NoSuchMethodException {
        Retry annotation = TestService.class.getMethod("ignoreOnMethod")
                .getAnnotation(Retry.class);

        assertEquals(1, annotation.ignoreOn().length);
        assertEquals(IllegalArgumentException.class, annotation.ignoreOn()[0]);
    }

    @Test
    @DisplayName("어노테이션 메타데이터 확인")
    void shouldHaveCorrectMetadata() {
        assertTrue(Retry.class.isAnnotation());

        var retention = Retry.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());

        var target = Retry.class.getAnnotation(java.lang.annotation.Target.class);
        assertEquals(1, target.value().length);
        assertEquals(java.lang.annotation.ElementType.METHOD, target.value()[0]);
    }

    static class TestService {
        @Retry
        public void defaultMethod() {}

        @Retry(maxAttempts = 5)
        public void customAttemptsMethod() {}

        @Retry(delay = 100, multiplier = 2.0, maxDelay = 5000)
        public void exponentialBackoffMethod() {}

        @Retry(retryOn = {IOException.class, RuntimeException.class})
        public void retryOnMethod() {}

        @Retry(ignoreOn = {IllegalArgumentException.class})
        public void ignoreOnMethod() {}
    }
}
