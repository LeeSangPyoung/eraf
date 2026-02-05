package com.eraf.core.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiter 테스트
 */
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // 초당 10개 허용
        rateLimiter = new RateLimiter(10, 1000);
    }

    @Test
    @DisplayName("허용 범위 내 요청은 통과")
    void shouldAllowRequestsWithinLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.tryAcquire(), "Request " + i + " should be allowed");
        }
    }

    @Test
    @DisplayName("허용 범위 초과 요청은 거부")
    void shouldRejectRequestsOverLimit() {
        // 10개 소비
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }

        // 11번째는 거부
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    @DisplayName("시간 경과 후 토큰 보충")
    void shouldRefillTokensAfterTime() throws InterruptedException {
        // 모든 토큰 소비
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }

        assertFalse(rateLimiter.tryAcquire());

        // 시간 경과 (토큰 보충)
        Thread.sleep(200); // 200ms -> 약 2개 토큰 보충

        assertTrue(rateLimiter.tryAcquire());
    }

    @Test
    @DisplayName("RateLimiter.Registry 테스트")
    void shouldManageMultipleLimiters() {
        RateLimiter.Registry registry = new RateLimiter.Registry();

        RateLimiter limiter1 = registry.get("api1", 5, 1000);
        RateLimiter limiter2 = registry.get("api2", 10, 1000);
        RateLimiter limiter1Again = registry.get("api1", 5, 1000);

        assertNotNull(limiter1);
        assertNotNull(limiter2);
        assertSame(limiter1, limiter1Again);
        assertNotSame(limiter1, limiter2);
    }

    @Test
    @DisplayName("Registry에서 모든 RateLimiter 조회")
    void shouldListAllLimiters() {
        RateLimiter.Registry registry = new RateLimiter.Registry();

        registry.get("api1", 5, 1000);
        registry.get("api2", 10, 1000);
        registry.get("api3", 15, 1000);

        var all = registry.getAll();

        assertEquals(3, all.size());
        assertTrue(all.containsKey("api1"));
        assertTrue(all.containsKey("api2"));
        assertTrue(all.containsKey("api3"));
    }

    @Test
    @DisplayName("토큰이 최대값을 초과하지 않음")
    void shouldNotExceedMaxTokens() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(5, 1000);

        // 충분한 시간 대기 (토큰 과잉 보충 시도)
        Thread.sleep(500);

        // 최대 5개만 성공해야 함
        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            if (limiter.tryAcquire()) {
                successCount++;
            }
        }

        assertEquals(5, successCount);
    }
}
