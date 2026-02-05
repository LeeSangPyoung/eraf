package com.eraf.core.resilience;

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
 * RateLimitAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    private RateLimitAspect aspect;
    private RateLimiter.Registry registry;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        registry = new RateLimiter.Registry();
        aspect = new RateLimitAspect(registry);
    }

    @Test
    @DisplayName("Rate Limit 내에서 정상 실행")
    void shouldExecuteWithinRateLimit() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed()).thenReturn("success");

        RateLimit annotation = createAnnotation("testApi", 10, 1000, "");

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Rate Limit 초과 시 예외 발생")
    void shouldThrowExceptionWhenRateLimitExceeded() throws Throwable {
        // Given
        setupMocks();
        RateLimit annotation = createAnnotation("limitedApi", 2, 1000, "");

        // 먼저 한도 소진
        when(joinPoint.proceed()).thenReturn("success");
        aspect.around(joinPoint, annotation);
        aspect.around(joinPoint, annotation);

        // When & Then - 3번째 호출은 실패
        assertThrows(RateLimitExceededException.class, () ->
                aspect.around(joinPoint, annotation));
    }

    @Test
    @DisplayName("다른 키는 별도 Rate Limit 적용")
    void shouldApplySeparateLimitsForDifferentKeys() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed()).thenReturn("success");

        RateLimit annotation1 = createAnnotation("api1", 1, 1000, "");
        RateLimit annotation2 = createAnnotation("api2", 1, 1000, "");

        // When
        aspect.around(joinPoint, annotation1);
        aspect.around(joinPoint, annotation2);

        // Then - 각각 1번씩 성공
        verify(joinPoint, times(2)).proceed();
    }

    @Test
    @DisplayName("SpEL 키 지원")
    void shouldSupportSpelKey() throws Throwable {
        // Given
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod("methodWithArg", String.class));
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"user123"});
        when(joinPoint.proceed()).thenReturn("success");

        RateLimit annotation = createAnnotation("", 10, 1000, "#userId");

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    private void setupMocks() throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod("testMethod"));
    }

    private RateLimit createAnnotation(String name, int permits, long period, String key) {
        return new RateLimit() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RateLimit.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int permits() {
                return permits;
            }

            @Override
            public long period() {
                return period;
            }

            @Override
            public String key() {
                return key;
            }
        };
    }

    static class TestService {
        public String testMethod() {
            return "result";
        }

        public String methodWithArg(String userId) {
            return "result for " + userId;
        }
    }
}
