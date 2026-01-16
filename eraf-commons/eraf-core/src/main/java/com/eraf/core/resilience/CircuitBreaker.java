package com.eraf.core.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit Breaker 구현
 *
 * 마이크로서비스 환경에서 장애 전파를 방지하는 패턴
 *
 * 상태 전이:
 * - CLOSED (정상) → OPEN (차단): 실패 횟수가 임계값 도달
 * - OPEN (차단) → HALF_OPEN (시험): 일정 시간 경과 후
 * - HALF_OPEN (시험) → CLOSED (정상): 성공 횟수가 임계값 도달
 * - HALF_OPEN (시험) → OPEN (차단): 실패 발생
 *
 * 사용 예:
 * <pre>
 * CircuitBreaker cb = CircuitBreaker.builder("myService")
 *     .failureThreshold(5)
 *     .successThreshold(3)
 *     .openTimeoutMs(30000)
 *     .build();
 *
 * if (cb.allowRequest()) {
 *     try {
 *         // 외부 서비스 호출
 *         cb.recordSuccess();
 *     } catch (Exception e) {
 *         cb.recordFailure();
 *     }
 * }
 * </pre>
 */
public class CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    private final String name;
    private final int failureThreshold;
    private final int successThreshold;
    private final long openTimeoutMs;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile Instant lastFailureTime;
    private volatile Instant openedAt;

    /**
     * Circuit Breaker 상태
     */
    public enum State {
        /** 정상 상태, 요청 허용 */
        CLOSED,
        /** 차단 상태, 요청 거부 */
        OPEN,
        /** 시험 상태, 제한된 요청 허용 */
        HALF_OPEN
    }

    private CircuitBreaker(String name, int failureThreshold, int successThreshold, long openTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.openTimeoutMs = openTimeoutMs;
    }

    /**
     * Builder 생성
     */
    public static Builder builder(String name) {
        return new Builder(name);
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

    private boolean shouldAttemptReset() {
        return openedAt != null && Instant.now().isAfter(openedAt.plusMillis(openTimeoutMs));
    }

    // Getters

    public String getName() {
        return name;
    }

    public State getState() {
        return state.get();
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public Instant getLastFailureTime() {
        return lastFailureTime;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public int getSuccessThreshold() {
        return successThreshold;
    }

    public long getOpenTimeoutMs() {
        return openTimeoutMs;
    }

    /**
     * 현재 상태 정보 반환
     */
    public Status getStatus() {
        return new Status(
                name,
                state.get(),
                failureCount.get(),
                failureThreshold,
                successCount.get(),
                successThreshold,
                lastFailureTime,
                openedAt
        );
    }

    /**
     * Circuit Breaker 상태 정보
     */
    public static class Status {
        private final String name;
        private final State state;
        private final int failureCount;
        private final int failureThreshold;
        private final int successCount;
        private final int successThreshold;
        private final Instant lastFailureTime;
        private final Instant openedAt;

        public Status(String name, State state, int failureCount, int failureThreshold,
                      int successCount, int successThreshold, Instant lastFailureTime, Instant openedAt) {
            this.name = name;
            this.state = state;
            this.failureCount = failureCount;
            this.failureThreshold = failureThreshold;
            this.successCount = successCount;
            this.successThreshold = successThreshold;
            this.lastFailureTime = lastFailureTime;
            this.openedAt = openedAt;
        }

        public String getName() { return name; }
        public State getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public int getFailureThreshold() { return failureThreshold; }
        public int getSuccessCount() { return successCount; }
        public int getSuccessThreshold() { return successThreshold; }
        public Instant getLastFailureTime() { return lastFailureTime; }
        public Instant getOpenedAt() { return openedAt; }
    }

    /**
     * Circuit Breaker Builder
     */
    public static class Builder {
        private final String name;
        private int failureThreshold = 5;
        private int successThreshold = 3;
        private long openTimeoutMs = 30000;

        public Builder(String name) {
            this.name = name;
        }

        /**
         * 실패 임계값 설정 (기본값: 5)
         */
        public Builder failureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
            return this;
        }

        /**
         * HALF_OPEN에서 CLOSED로 전환하기 위한 성공 임계값 (기본값: 3)
         */
        public Builder successThreshold(int successThreshold) {
            this.successThreshold = successThreshold;
            return this;
        }

        /**
         * OPEN 상태 유지 시간 (밀리초, 기본값: 30000)
         */
        public Builder openTimeoutMs(long openTimeoutMs) {
            this.openTimeoutMs = openTimeoutMs;
            return this;
        }

        public CircuitBreaker build() {
            return new CircuitBreaker(name, failureThreshold, successThreshold, openTimeoutMs);
        }
    }
}
