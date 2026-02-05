package com.eraf.core.resilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Timeout 어노테이션 테스트
 */
class TimeoutAnnotationTest {

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("defaultMethod")
                .getAnnotation(Timeout.class);

        assertEquals(5000, annotation.value());
        assertEquals(TimeUnit.MILLISECONDS, annotation.unit());
    }

    @Test
    @DisplayName("커스텀 타임아웃 값 설정")
    void shouldAllowCustomTimeout() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("customTimeoutMethod")
                .getAnnotation(Timeout.class);

        assertEquals(10000, annotation.value());
    }

    @Test
    @DisplayName("다른 시간 단위 설정")
    void shouldAllowDifferentTimeUnit() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("secondsMethod")
                .getAnnotation(Timeout.class);

        assertEquals(30, annotation.value());
        assertEquals(TimeUnit.SECONDS, annotation.unit());
    }

    @Test
    @DisplayName("분 단위 타임아웃")
    void shouldAllowMinutesUnit() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("minutesMethod")
                .getAnnotation(Timeout.class);

        assertEquals(5, annotation.value());
        assertEquals(TimeUnit.MINUTES, annotation.unit());
    }

    @Test
    @DisplayName("어노테이션 메타데이터 확인")
    void shouldHaveCorrectMetadata() {
        assertTrue(Timeout.class.isAnnotation());

        var retention = Timeout.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());

        var target = Timeout.class.getAnnotation(java.lang.annotation.Target.class);
        assertEquals(1, target.value().length);
        assertEquals(java.lang.annotation.ElementType.METHOD, target.value()[0]);
    }

    @Test
    @DisplayName("밀리초를 초로 변환")
    void shouldConvertMillisecondsToSeconds() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("defaultMethod")
                .getAnnotation(Timeout.class);

        long milliseconds = annotation.unit().toMillis(annotation.value());
        assertEquals(5000, milliseconds);
    }

    @Test
    @DisplayName("초를 밀리초로 변환")
    void shouldConvertSecondsToMilliseconds() throws NoSuchMethodException {
        Timeout annotation = TestService.class.getMethod("secondsMethod")
                .getAnnotation(Timeout.class);

        long milliseconds = annotation.unit().toMillis(annotation.value());
        assertEquals(30000, milliseconds);
    }

    static class TestService {
        @Timeout
        public void defaultMethod() {}

        @Timeout(10000)
        public void customTimeoutMethod() {}

        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        public void secondsMethod() {}

        @Timeout(value = 5, unit = TimeUnit.MINUTES)
        public void minutesMethod() {}
    }
}
