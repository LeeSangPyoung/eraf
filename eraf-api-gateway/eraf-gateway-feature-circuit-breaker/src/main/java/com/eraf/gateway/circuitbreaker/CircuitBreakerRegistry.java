package com.eraf.gateway.circuitbreaker;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Circuit Breaker 레지스트리
 * 서비스/엔드포인트별 Circuit Breaker 관리
 */
public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    private final int defaultFailureThreshold;
    private final int defaultSuccessThreshold;
    private final long defaultOpenTimeoutMs;

    public CircuitBreakerRegistry(int defaultFailureThreshold, int defaultSuccessThreshold, long defaultOpenTimeoutMs) {
        this.defaultFailureThreshold = defaultFailureThreshold;
        this.defaultSuccessThreshold = defaultSuccessThreshold;
        this.defaultOpenTimeoutMs = defaultOpenTimeoutMs;
    }

    /**
     * Circuit Breaker 조회 (없으면 생성)
     */
    public CircuitBreaker getOrCreate(String name) {
        return circuitBreakers.computeIfAbsent(name, n ->
                new CircuitBreaker(n, defaultFailureThreshold, defaultSuccessThreshold, defaultOpenTimeoutMs)
        );
    }

    /**
     * Circuit Breaker 조회
     */
    public Optional<CircuitBreaker> get(String name) {
        return Optional.ofNullable(circuitBreakers.get(name));
    }

    /**
     * Circuit Breaker 등록
     */
    public void register(String name, int failureThreshold, int successThreshold, long openTimeoutMs) {
        circuitBreakers.put(name, new CircuitBreaker(name, failureThreshold, successThreshold, openTimeoutMs));
    }

    /**
     * Circuit Breaker 제거
     */
    public void remove(String name) {
        circuitBreakers.remove(name);
    }

    /**
     * 모든 Circuit Breaker 조회
     */
    public Collection<CircuitBreaker> getAll() {
        return circuitBreakers.values();
    }

    /**
     * 모든 Circuit Breaker 리셋
     */
    public void resetAll() {
        circuitBreakers.values().forEach(CircuitBreaker::reset);
    }
}
