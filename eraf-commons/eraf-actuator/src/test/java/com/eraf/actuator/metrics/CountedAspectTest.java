package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CountedAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class CountedAspectTest {

    private CountedAspect aspect;
    private MeterRegistry meterRegistry;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        aspect = new CountedAspect(meterRegistry);
    }

    @Test
    @DisplayName("메서드 호출 카운트")
    void shouldCountMethodInvocation() throws Throwable {
        // Given
        setupMocks("countedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Counted annotation = createAnnotation("method.calls", "Call count", new String[]{});

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("result", result);

        Counter counter = meterRegistry.find("method.calls").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("태그가 있는 카운터")
    void shouldRecordCounterWithTags() throws Throwable {
        // Given
        setupMocks("countedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Counted annotation = createAnnotation("api.calls", "",
                new String[]{"endpoint=/orders", "status=success"});

        // When
        aspect.around(joinPoint, annotation);

        // Then
        Counter counter = meterRegistry.find("api.calls")
                .tags("endpoint", "/orders", "status", "success")
                .counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("여러 번 호출 시 카운트 증가")
    void shouldIncrementCountOnMultipleCalls() throws Throwable {
        // Given
        setupMocks("countedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Counted annotation = createAnnotation("multi.count", "", new String[]{});

        // When
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);

        // Then
        Counter counter = meterRegistry.find("multi.count").counter();
        assertNotNull(counter);
        assertEquals(5.0, counter.count());
    }

    @Test
    @DisplayName("예외 발생해도 카운트 (recordOnException=true)")
    void shouldCountEvenOnException() throws Throwable {
        // Given
        setupMocks("countedMethod");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        Counted annotation = createAnnotationWithRecordOnException("error.count", "", new String[]{});

        // When & Then
        assertThrows(RuntimeException.class, () ->
                aspect.around(joinPoint, annotation));

        Counter counter = meterRegistry.find("error.count").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("다른 태그는 별도 카운터")
    void shouldUseSeparateCountersForDifferentTags() throws Throwable {
        // Given
        setupMocks("countedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Counted annotation1 = createAnnotation("http.requests", "", new String[]{"status=200"});
        Counted annotation2 = createAnnotation("http.requests", "", new String[]{"status=404"});

        // When
        aspect.around(joinPoint, annotation1);
        aspect.around(joinPoint, annotation1);
        aspect.around(joinPoint, annotation2);

        // Then
        Counter counter200 = meterRegistry.find("http.requests").tags("status", "200").counter();
        Counter counter404 = meterRegistry.find("http.requests").tags("status", "404").counter();

        assertEquals(2.0, counter200.count());
        assertEquals(1.0, counter404.count());
    }

    @Test
    @DisplayName("빈 이름은 메서드 이름 사용")
    void shouldUseMethodNameWhenNameEmpty() throws Throwable {
        // Given
        setupMocks("specificMethod");
        when(joinPoint.proceed()).thenReturn("result");
        when(methodSignature.getDeclaringType()).thenReturn((Class) TestService.class);

        Counted annotation = createAnnotation("", "", new String[]{});

        // When
        aspect.around(joinPoint, annotation);

        // Then
        Counter counter = meterRegistry.find("TestService.specificMethod.count").counter();
        assertNotNull(counter);
    }

    private void setupMocks(String methodName) throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod(methodName));
        lenient().when(methodSignature.getDeclaringType()).thenReturn((Class) TestService.class);
        lenient().when(methodSignature.toShortString()).thenReturn("TestService." + methodName + "()");
    }

    private Counted createAnnotation(String value, String description, String[] extraTags) {
        return createAnnotation(value, description, extraTags, false);
    }

    private Counted createAnnotationWithRecordOnException(String value, String description, String[] extraTags) {
        return createAnnotation(value, description, extraTags, true);
    }

    private Counted createAnnotation(String value, String description, String[] extraTags, boolean recordOnException) {
        return new Counted() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Counted.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String[] extraTags() {
                return extraTags;
            }

            @Override
            public boolean recordOnException() {
                return recordOnException;
            }
        };
    }

    static class TestService {
        public String countedMethod() { return "result"; }
        public String specificMethod() { return "result"; }
    }
}
