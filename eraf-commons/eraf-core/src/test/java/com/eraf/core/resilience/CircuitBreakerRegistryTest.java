package com.eraf.core.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CircuitBreakerRegistry 테스트
 */
class CircuitBreakerRegistryTest {

    private CircuitBreakerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CircuitBreakerRegistry();
    }

    @Test
    @DisplayName("새로운 CircuitBreaker 생성")
    void shouldCreateNewCircuitBreaker() {
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("test", 5, 60000);

        assertNotNull(cb);
        assertTrue(cb.allowRequest());
    }

    @Test
    @DisplayName("동일한 이름으로 동일한 CircuitBreaker 반환")
    void shouldReturnSameCircuitBreakerForSameName() {
        CircuitBreakerRegistry.CircuitBreaker cb1 = registry.get("test", 5, 60000);
        CircuitBreakerRegistry.CircuitBreaker cb2 = registry.get("test", 5, 60000);

        assertSame(cb1, cb2);
    }

    @Test
    @DisplayName("실패 임계값 도달 시 Circuit 오픈")
    void shouldOpenCircuitAfterFailureThreshold() {
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("test", 3, 60000);

        // 3번 실패 기록
        cb.recordFailure();
        cb.recordFailure();
        cb.recordFailure();

        assertFalse(cb.allowRequest());
    }

    @Test
    @DisplayName("성공 시 실패 카운트 리셋")
    void shouldResetFailureCountOnSuccess() {
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("test", 3, 60000);

        cb.recordFailure();
        cb.recordFailure();
        cb.recordSuccess();

        // 실패 카운트가 리셋되어 다시 3번 실패해야 열림
        cb.recordFailure();
        cb.recordFailure();
        assertTrue(cb.allowRequest());

        cb.recordFailure();
        assertFalse(cb.allowRequest());
    }

    @Test
    @DisplayName("타임아웃 후 Half-Open 상태로 전환")
    void shouldAllowRequestAfterTimeout() throws InterruptedException {
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("test", 2, 100); // 100ms timeout

        cb.recordFailure();
        cb.recordFailure();

        assertFalse(cb.allowRequest());

        Thread.sleep(150); // 타임아웃 대기

        assertTrue(cb.allowRequest()); // Half-open 상태에서 허용
    }

    @Test
    @DisplayName("Half-Open 상태에서 성공 시 Closed로 전환")
    void shouldCloseCircuitOnSuccessInHalfOpen() throws InterruptedException {
        CircuitBreakerRegistry.CircuitBreaker cb = registry.get("test", 2, 100);

        cb.recordFailure();
        cb.recordFailure();

        Thread.sleep(150);

        cb.allowRequest(); // Half-open 진입
        cb.recordSuccess(); // 성공 -> Closed

        cb.recordFailure(); // 다시 실패 카운트 시작
        assertTrue(cb.allowRequest()); // 아직 1번만 실패
    }

    @Test
    @DisplayName("CircuitBreaker 목록 조회")
    void shouldListAllCircuitBreakers() {
        registry.get("service1", 5, 60000);
        registry.get("service2", 5, 60000);
        registry.get("service3", 5, 60000);

        var all = registry.getAll();

        assertEquals(3, all.size());
        assertTrue(all.containsKey("service1"));
        assertTrue(all.containsKey("service2"));
        assertTrue(all.containsKey("service3"));
    }
}
