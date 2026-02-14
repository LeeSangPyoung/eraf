package com.eraf.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Circuit Breaker Unit Tests
 */
class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = CircuitBreaker.builder("test")
                .failureThreshold(3)
                .successThreshold(2)
                .openTimeoutMs(1000)
                .build();
    }

    @Test
    void shouldAllowRequestWhenClosed() {
        // given & when
        boolean allowed = circuitBreaker.allowRequest();

        // then
        assertThat(allowed).isTrue();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldTransitionToOpenWhenFailureThresholdExceeded() {
        // given
        int failureThreshold = 3;

        // when
        for (int i = 0; i < failureThreshold; i++) {
            circuitBreaker.recordFailure();
        }

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getFailureCount()).isEqualTo(failureThreshold);
        assertThat(circuitBreaker.getOpenedAt()).isNotNull();
    }

    @Test
    void shouldRejectRequestWhenOpen() {
        // given
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }

        // when
        boolean allowed = circuitBreaker.allowRequest();

        // then
        assertThat(allowed).isFalse();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldTransitionToHalfOpenAfterTimeout() throws InterruptedException {
        // given
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // when
        Thread.sleep(1100); // Wait for openTimeout
        boolean allowed = circuitBreaker.allowRequest();

        // then
        assertThat(allowed).isTrue();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    @Test
    void shouldTransitionToClosedFromHalfOpenWhenSuccessThresholdMet() {
        // given
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }
        circuitBreaker.reset(); // Force CLOSED state
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        // Now OPEN, wait and transition to HALF_OPEN
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        circuitBreaker.allowRequest(); // Transition to HALF_OPEN

        // when
        circuitBreaker.recordSuccess();
        circuitBreaker.recordSuccess(); // Meet success threshold

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(circuitBreaker.getFailureCount()).isZero();
        assertThat(circuitBreaker.getSuccessCount()).isZero();
    }

    @Test
    void shouldTransitionBackToOpenFromHalfOpenOnFailure() throws InterruptedException {
        // given
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }
        Thread.sleep(1100);
        circuitBreaker.allowRequest(); // Transition to HALF_OPEN

        // when
        circuitBreaker.recordFailure();

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getOpenedAt()).isNotNull();
    }

    @Test
    void shouldResetFailureCountOnSuccessWhenClosed() {
        // given
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        assertThat(circuitBreaker.getFailureCount()).isEqualTo(2);

        // when
        circuitBreaker.recordSuccess();

        // then
        assertThat(circuitBreaker.getFailureCount()).isZero();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldManuallyTripToOpen() {
        // given
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // when
        circuitBreaker.trip();

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(circuitBreaker.getOpenedAt()).isNotNull();
    }

    @Test
    void shouldManuallyResetToClosed() {
        // given
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // when
        circuitBreaker.reset();

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(circuitBreaker.getFailureCount()).isZero();
        assertThat(circuitBreaker.getSuccessCount()).isZero();
    }

    @Test
    void shouldReturnCorrectStatus() {
        // given
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();

        // when
        CircuitBreaker.Status status = circuitBreaker.getStatus();

        // then
        assertThat(status.getName()).isEqualTo("test");
        assertThat(status.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(status.getFailureCount()).isEqualTo(2);
        assertThat(status.getFailureThreshold()).isEqualTo(3);
        assertThat(status.getSuccessThreshold()).isEqualTo(2);
    }

    @Test
    void shouldTrackLastFailureTime() {
        // given
        Instant before = Instant.now();

        // when
        circuitBreaker.recordFailure();

        // then
        Instant after = Instant.now();
        assertThat(circuitBreaker.getLastFailureTime())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }
}
