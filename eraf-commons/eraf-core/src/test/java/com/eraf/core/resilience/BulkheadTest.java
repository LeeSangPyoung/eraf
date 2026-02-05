package com.eraf.core.resilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bulkhead 어노테이션 테스트
 */
class BulkheadTest {

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() throws NoSuchMethodException {
        Bulkhead annotation = TestService.class.getMethod("defaultMethod")
                .getAnnotation(Bulkhead.class);

        assertEquals("", annotation.name());
        assertEquals(10, annotation.maxConcurrent());
        assertEquals(0, annotation.maxWait());
    }

    @Test
    @DisplayName("커스텀 값 설정")
    void shouldAllowCustomValues() throws NoSuchMethodException {
        Bulkhead annotation = TestService.class.getMethod("customMethod")
                .getAnnotation(Bulkhead.class);

        assertEquals("apiService", annotation.name());
        assertEquals(5, annotation.maxConcurrent());
        assertEquals(1000, annotation.maxWait());
    }

    @Test
    @DisplayName("어노테이션 메타데이터 확인")
    void shouldHaveCorrectMetadata() {
        assertTrue(Bulkhead.class.isAnnotation());

        var retention = Bulkhead.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());

        var target = Bulkhead.class.getAnnotation(java.lang.annotation.Target.class);
        assertEquals(1, target.value().length);
        assertEquals(java.lang.annotation.ElementType.METHOD, target.value()[0]);
    }

    static class TestService {
        @Bulkhead
        public void defaultMethod() {}

        @Bulkhead(name = "apiService", maxConcurrent = 5, maxWait = 1000)
        public void customMethod() {}
    }
}
