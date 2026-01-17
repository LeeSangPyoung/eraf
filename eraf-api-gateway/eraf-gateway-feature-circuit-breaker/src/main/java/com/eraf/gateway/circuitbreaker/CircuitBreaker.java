package com.eraf.gateway.circuitbreaker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit Breaker 구현
 * 상태: CLOSED (정상) -> OPEN (차단) -> HALF_OPEN (시험) -> CLOSED
 */
@Slf4j
public class CircuitBreaker {

    private final String name;
    private final int failureThreshold;
    private final int successThreshold;
    private final long openTimeoutMs;

    @Getter
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile Instant lastFailureTime;
    private volatile Instant openedAt;

    public enum State {
        CLOSED,     // 정상 상태, 요청 허용
        OPEN,       // 차단 상태, 요청 거부
        HALF_OPEN   // 시험 상태, 제한된 요청 허용
    }

    public CircuitBreaker(String name, int failureThreshold, int successThreshold, long openTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.openTimeoutMs = openTimeoutMs;
    }

    /**
     * 요청 허용 여부 확인
     */
    public boolean allowRequest() {
        State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                if (shouldAttemptReset()) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        log.info("Circuit breaker [{}] transitioning from OPEN to HALF_OPEN", name);
                        successCount.set(0);
                    }
                    return true;
                }
                return false;

            case HALF_OPEN:
                return true;

            default:
                return false;
        }
    }

    /**
     * 성공 기록
     */
    public void recordSuccess() {
        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            if (successes >= successThreshold) {
                if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                    log.info("Circuit breaker [{}] transitioning from HALF_OPEN to CLOSED", name);
                    failureCount.set(0);
                    successCount.set(0);
                }
            }
        } else if (currentState == State.CLOSED) {
            failureCount.set(0);
        }
    }

    /**
     * 실패 기록
     */
    public void recordFailure() {
        lastFailureTime = Instant.now();
        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                log.warn("Circuit breaker [{}] transitioning from HALF_OPEN to OPEN", name);
                openedAt = Instant.now();
            }
        } else if (currentState == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                    log.warn("Circuit breaker [{}] transitioning from CLOSED to OPEN after {} failures", name, failures);
                    openedAt = Instant.now();
                }
            }
        }
    }

    /**
     * 강제로 OPEN 상태로 전환
     */
    public void trip() {
        state.set(State.OPEN);
        openedAt = Instant.now();
        log.warn("Circuit breaker [{}] manually tripped to OPEN", name);
    }

    /**
     * 강제로 CLOSED 상태로 리셋
     */
    public void reset() {
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        log.info("Circuit breaker [{}] manually reset to CLOSED", name);
    }

    /**
     * OPEN -> HALF_OPEN 전환 시도 여부
     */
    private boolean shouldAttemptReset() {
        return openedAt != null &&
                Instant.now().isAfter(openedAt.plusMillis(openTimeoutMs));
    }

    /**
     * 현재 상태 정보
     */
    public CircuitBreakerStatus getStatus() {
        return CircuitBreakerStatus.builder()
                .name(name)
                .state(state.get())
                .failureCount(failureCount.get())
                .failureThreshold(failureThreshold)
                .successCount(successCount.get())
                .successThreshold(successThreshold)
                .lastFailureTime(lastFailureTime)
                .openedAt(openedAt)
                .build();
    }

    public String getName() {
        return name;
    }

    @lombok.Builder
    @lombok.Getter
    public static class CircuitBreakerStatus {
        private final String name;
        private final State state;
        private final int failureCount;
        private final int failureThreshold;
        private final int successCount;
        private final int successThreshold;
        private final Instant lastFailureTime;
        private final Instant openedAt;
    }
}
