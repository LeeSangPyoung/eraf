package com.eraf.core.resilience;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Collection;
import java.util.Collections;

/**
 * Circuit Breaker 레지스트리
 * 여러 서킷브레이커를 중앙에서 관리
 */
public class CircuitBreakerRegistry {

    private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    private final int defaultFailureThreshold;
    private final int defaultSuccessThreshold;
    private final long defaultOpenTimeoutMs;

    public CircuitBreakerRegistry() {
        this(5, 3, 30000);
    }

    public CircuitBreakerRegistry(int defaultFailureThreshold, int defaultSuccessThreshold, long defaultOpenTimeoutMs) {
        this.defaultFailureThreshold = defaultFailureThreshold;
        this.defaultSuccessThreshold = defaultSuccessThreshold;
        this.defaultOpenTimeoutMs = defaultOpenTimeoutMs;
    }

    /**
     * 서킷브레이커 조회 또는 생성
     */
    public CircuitBreaker getOrCreate(String name) {
        return circuitBreakers.computeIfAbsent(name, this::createDefault);
    }

    /**
     * 서킷브레이커 조회 또는 커스텀 설정으로 생성
     */
    public CircuitBreaker getOrCreate(String name, int failureThreshold, int successThreshold, long openTimeoutMs) {
        return circuitBreakers.computeIfAbsent(name, n ->
                CircuitBreaker.builder(n)
                        .failureThreshold(failureThreshold)
                        .successThreshold(successThreshold)
                        .openTimeoutMs(openTimeoutMs)
                        .build()
        );
    }

    /**
     * 서킷브레이커 조회
     */
    public CircuitBreaker get(String name) {
        return circuitBreakers.get(name);
    }

    /**
     * 모든 서킷브레이커 조회
     */
    public Collection<CircuitBreaker> getAll() {
        return Collections.unmodifiableCollection(circuitBreakers.values());
    }

    /**
     * 서킷브레이커 제거
     */
    public CircuitBreaker remove(String name) {
        return circuitBreakers.remove(name);
    }

    /**
     * 모든 서킷브레이커 리셋
     */
    public void resetAll() {
        circuitBreakers.values().forEach(CircuitBreaker::reset);
    }

    /**
     * 특정 서킷브레이커 존재 여부
     */
    public boolean contains(String name) {
        return circuitBreakers.containsKey(name);
    }

    /**
     * 등록된 서킷브레이커 수
     */
    public int size() {
        return circuitBreakers.size();
    }

    private CircuitBreaker createDefault(String name) {
        return CircuitBreaker.builder(name)
                .failureThreshold(defaultFailureThreshold)
                .successThreshold(defaultSuccessThreshold)
                .openTimeoutMs(defaultOpenTimeoutMs)
                .build();
    }
}
