package com.eraf.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Rate Limiter Unit Tests
 */
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = RateLimiter.builder("test")
                .permitsPerSecond(10.0)
                .maxBurstSize(10)
                .build();
    }

    @Test
    void shouldAcquireWhenPermitsAvailable() {
        // when
        boolean acquired = rateLimiter.tryAcquire();

        // then
        assertThat(acquired).isTrue();
    }

    @Test
    void shouldRejectWhenNoPermitsAvailable() {
        // given
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }

        // when
        boolean acquired = rateLimiter.tryAcquire();

        // then
        assertThat(acquired).isFalse();
    }

    @Test
    void shouldAcquireMultiplePermits() {
        // when
        boolean acquired = rateLimiter.tryAcquire(5);

        // then
        assertThat(acquired).isTrue();
        assertThat(rateLimiter.getAvailablePermits()).isLessThan(10);
    }

    @Test
    void shouldRejectWhenNotEnoughPermits() {
        // when
        boolean acquired = rateLimiter.tryAcquire(15);

        // then
        assertThat(acquired).isFalse();
    }

    @Test
    void shouldThrowExceptionForInvalidPermits() {
        // when & then
        assertThatThrownBy(() -> rateLimiter.tryAcquire(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permits must be positive");

        assertThatThrownBy(() -> rateLimiter.tryAcquire(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permits must be positive");
    }

    @Test
    void shouldRefillPermitsOverTime() throws InterruptedException {
        // given
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }
        assertThat(rateLimiter.tryAcquire()).isFalse();

        // when
        Thread.sleep(150); // Wait for refill (10 permits/sec = 1.5 permits in 150ms)

        // then
        assertThat(rateLimiter.tryAcquire()).isTrue();
    }

    @Test
    void shouldBlockingAcquireWaitForPermits() throws InterruptedException {
        // given
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }

        // when
        long startTime = System.currentTimeMillis();
        rateLimiter.acquire();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertThat(elapsedTime).isGreaterThanOrEqualTo(80); // Should wait ~100ms for 1 permit at 10/sec
    }

    @Test
    void shouldReturnCorrectAvailablePermits() {
        // given
        rateLimiter.tryAcquire(3);

        // when
        double availablePermits = rateLimiter.getAvailablePermits();

        // then
        assertThat(availablePermits).isLessThan(10);
        assertThat(availablePermits).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldReturnCorrectPermitsPerSecond() {
        // when
        double permitsPerSecond = rateLimiter.getPermitsPerSecond();

        // then
        assertThat(permitsPerSecond).isEqualTo(10.0);
    }

    @Test
    void shouldReturnCorrectName() {
        // when
        String name = rateLimiter.getName();

        // then
        assertThat(name).isEqualTo("test");
    }

    @Test
    void shouldUsePermitsPerSecondAsMaxBurstWhenNotSpecified() {
        // given
        RateLimiter limiter = RateLimiter.builder("test")
                .permitsPerSecond(5.0)
                .build();

        // when
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire();
        }

        // then
        assertThat(limiter.tryAcquire()).isFalse();
    }
}
