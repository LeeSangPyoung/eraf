package com.eraf.core.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BulkheadAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
class BulkheadAspectTest {

    private BulkheadAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        aspect = new BulkheadAspect();
    }

    @Test
    @DisplayName("동시 실행 제한 내 정상 실행")
    void shouldExecuteWithinConcurrencyLimit() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed()).thenReturn("success");
        Bulkhead annotation = createAnnotation("test", 5, 0);

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    @Test
    @DisplayName("동시 실행 제한 초과 시 예외 발생")
    void shouldRejectWhenConcurrencyLimitExceeded() throws Throwable {
        // Given
        Bulkhead annotation = createAnnotation("limited", 1, 0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch blockLatch = new CountDownLatch(1);
        AtomicInteger rejections = new AtomicInteger(0);

        setupMocks();
        when(joinPoint.proceed()).thenAnswer(inv -> {
            startLatch.countDown();
            blockLatch.await(); // 블로킹
            return "success";
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 첫 번째 요청 시작 (블로킹됨)
        executor.submit(() -> {
            try {
                aspect.around(joinPoint, annotation);
            } catch (Throwable ignored) {}
        });

        startLatch.await(); // 첫 번째 요청이 시작될 때까지 대기

        // 두 번째 요청 - 거부되어야 함
        try {
            aspect.around(joinPoint, annotation);
        } catch (BulkheadFullException e) {
            rejections.incrementAndGet();
        }

        blockLatch.countDown(); // 첫 번째 요청 해제
        executor.shutdown();

        assertEquals(1, rejections.get());
    }

    @Test
    @DisplayName("maxWait 시간 내 세마포어 획득")
    void shouldAcquireWithinMaxWait() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed()).thenReturn("success");
        Bulkhead annotation = createAnnotation("waitTest", 1, 1000);

        // When
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    @Test
    @DisplayName("다른 이름은 별도 Bulkhead")
    void shouldUseSeparateBulkheadsForDifferentNames() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed()).thenReturn("success");

        Bulkhead annotation1 = createAnnotation("service1", 1, 0);
        Bulkhead annotation2 = createAnnotation("service2", 1, 0);

        // When
        aspect.around(joinPoint, annotation1);
        aspect.around(joinPoint, annotation2);

        // Then - 둘 다 성공
        verify(joinPoint, times(2)).proceed();
    }

    @Test
    @DisplayName("예외 발생해도 세마포어 해제")
    void shouldReleaseSemaphoreOnException() throws Throwable {
        // Given
        setupMocks();
        when(joinPoint.proceed())
                .thenThrow(new RuntimeException("error"))
                .thenReturn("success");

        Bulkhead annotation = createAnnotation("errorTest", 1, 0);

        // When
        try {
            aspect.around(joinPoint, annotation);
        } catch (RuntimeException ignored) {}

        // 세마포어가 해제되어 다음 요청 가능해야 함
        Object result = aspect.around(joinPoint, annotation);

        // Then
        assertEquals("success", result);
    }

    private void setupMocks() throws NoSuchMethodException {
        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getMethod()).thenReturn(
                TestService.class.getMethod("testMethod"));
        lenient().when(methodSignature.toShortString()).thenReturn("TestService.testMethod()");
    }

    private Bulkhead createAnnotation(String name, int maxConcurrent, long maxWait) {
        return new Bulkhead() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Bulkhead.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int maxConcurrent() {
                return maxConcurrent;
            }

            @Override
            public long maxWait() {
                return maxWait;
            }
        };
    }

    static class TestService {
        public String testMethod() {
            return "result";
        }
    }
}
