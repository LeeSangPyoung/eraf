package com.eraf.core.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CircuitBreakerAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class CircuitBreakerAspectTest {

    private CircuitBreakerAspect aspect;
    private CircuitBreakerRegistry registry;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        registry = new CircuitBreakerRegistry();
        aspect = new CircuitBreakerAspect(registry);
    }

    @Test
    @DisplayName("Circuit Closed 상태에서 정상 실행")
    void shouldExecuteWhenCircuitClosed() throws Throwable {
        // Given
        setupMocks("testMethod", "test", 3, 60000, "");
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.around(joinPoint, createAnnotation("test", 3, 60000, ""));

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("예외 발생 시 실패 기록")
    void shouldRecordFailureOnException() throws Throwable {
        // Given
        setupMocks("testMethod", "test", 3, 60000, "");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                aspect.around(joinPoint, createAnnotation("test", 3, 60000, "")));
    }

    @Test
    @DisplayName("Circuit Open 시 CircuitBreakerOpenException 발생")
    void shouldThrowExceptionWhenCircuitOpen() throws Throwable {
        // Given
        setupMocks("testMethod", "openCircuit", 2, 60000, "");
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("openCircuit", 2, 60000);

        // Circuit을 Open 상태로 만듦
        cb.recordFailure();
        cb.recordFailure();

        // When & Then
        assertThrows(CircuitBreakerOpenException.class, () ->
                aspect.around(joinPoint, createAnnotation("openCircuit", 2, 60000, "")));

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Fallback 메서드 호출")
    void shouldCallFallbackMethod() throws Throwable {
        // Given
        TestService service = new TestService();
        when(joinPoint.getTarget()).thenReturn(service);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod("methodWithFallback"));
        when(methodSignature.getDeclaringType()).thenReturn(TestService.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        // Circuit을 Open 상태로 만듦
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("withFallback", 1, 60000);
        cb.recordFailure();

        // When
        Object result = aspect.around(joinPoint, createAnnotation("withFallback", 1, 60000, "fallbackMethod"));

        // Then
        assertEquals("fallback", result);
    }

    private void setupMocks(String methodName, String cbName, int threshold, long timeout, String fallback) throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod(methodName));
        when(methodSignature.getDeclaringType()).thenReturn(TestService.class);
        when(joinPoint.getTarget()).thenReturn(new TestService());
    }

    private EnableCircuitBreaker createAnnotation(String name, int threshold, long timeout, String fallback) {
        return new EnableCircuitBreaker() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return EnableCircuitBreaker.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int failureThreshold() {
                return threshold;
            }

            @Override
            public long timeout() {
                return timeout;
            }

            @Override
            public String fallbackMethod() {
                return fallback;
            }
        };
    }

    static class TestService {
        public String testMethod() {
            return "result";
        }

        public String methodWithFallback() {
            return "result";
        }

        public String fallbackMethod() {
            return "fallback";
        }
    }
}
