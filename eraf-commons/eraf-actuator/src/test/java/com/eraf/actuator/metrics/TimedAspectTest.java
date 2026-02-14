package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
 * TimedAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class TimedAspectTest {

    private TimedAspect aspect;
    private MeterRegistry meterRegistry;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        aspect = new TimedAspect(meterRegistry);
    }

    @Test
    @DisplayName("메서드 실행 시간 측정")
    void shouldMeasureMethodExecutionTime() throws Throwable {
        // Given
        setupMocks("timedMethod");
        when(joinPoint.proceed()).thenAnswer(inv -> {
            Thread.sleep(50);
            return "result";
        });

        Timed annotation = createAnnotation("test.method", "Test method", new String[]{});

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("result", result);

        Timer timer = meterRegistry.find("test.method").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) >= 50);
    }

    @Test
    @DisplayName("태그가 있는 타이머")
    void shouldRecordTimerWithTags() throws Throwable {
        // Given
        setupMocks("timedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Timed annotation = createAnnotation("api.latency", "API Latency",
                new String[]{"endpoint=/users", "method=GET"});

        // When
        aspect.around(joinPoint, annotation);

        // Then
        Timer timer = meterRegistry.find("api.latency")
                .tags("endpoint", "/users", "method", "GET")
                .timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("예외 발생해도 시간 측정")
    void shouldMeasureTimeEvenOnException() throws Throwable {
        // Given
        setupMocks("timedMethod");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        Timed annotation = createAnnotation("error.method", "", new String[]{});

        // When & Then
        assertThrows(RuntimeException.class, () ->
                aspect.around(joinPoint, annotation));

        Timer timer = meterRegistry.find("error.method").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("여러 번 호출 시 카운트 증가")
    void shouldIncrementCountOnMultipleCalls() throws Throwable {
        // Given
        setupMocks("timedMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Timed annotation = createAnnotation("multi.call", "", new String[]{});

        // When
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);

        // Then
        Timer timer = meterRegistry.find("multi.call").timer();
        assertNotNull(timer);
        assertEquals(3, timer.count());
    }

    @Test
    @DisplayName("빈 이름은 메서드 이름 사용")
    void shouldUseMethodNameWhenNameEmpty() throws Throwable {
        // Given
        setupMocks("specificMethod");
        when(joinPoint.proceed()).thenReturn("result");
        when(methodSignature.getDeclaringType()).thenReturn((Class) TestService.class);

        Timed annotation = createAnnotation("", "", new String[]{});

        // When
        aspect.around(joinPoint, annotation);

        // Then
        Timer timer = meterRegistry.find("TestService.specificMethod").timer();
        assertNotNull(timer);
    }

    private void setupMocks(String methodName) throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod(methodName));
        lenient().when(methodSignature.getDeclaringType()).thenReturn((Class) TestService.class);
        lenient().when(methodSignature.toShortString()).thenReturn("TestService." + methodName + "()");
    }

    private Timed createAnnotation(String value, String description, String[] extraTags) {
        return new Timed() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Timed.class;
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
            public boolean histogram() {
                return false;
            }

            @Override
            public double[] percentiles() {
                return new double[0];
            }

            @Override
            public boolean recordOnException() {
                return true;
            }
        };
    }

    static class TestService {
        public String timedMethod() { return "result"; }
        public String specificMethod() { return "result"; }
    }
}
