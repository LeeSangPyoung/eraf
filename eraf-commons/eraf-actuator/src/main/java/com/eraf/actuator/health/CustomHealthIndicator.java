package com.eraf.actuator.health;

import org.springframework.boot.actuate.health.Health;

/**
 * 커스텀 Health Indicator 인터페이스
 * 애플리케이션별 Health Check 로직을 구현하기 위한 인터페이스
 */
public interface CustomHealthIndicator {

    /**
     * Health 체크 이름
     */
    String getName();

    /**
     * Health 체크 수행
     */
    Health check();

    /**
     * Liveness Probe에 포함 여부
     * Liveness: 애플리케이션이 살아있는지 (재시작이 필요한지)
     */
    default boolean includeInLiveness() {
        return false;  // 기본적으로 Liveness에는 포함하지 않음
    }

    /**
     * Readiness Probe에 포함 여부
     * Readiness: 트래픽을 받을 준비가 되었는지
     */
    default boolean includeInReadiness() {
        return true;  // 기본적으로 Readiness에 포함
    }

    /**
     * Critical 여부
     * Critical이면 DOWN 시 전체 헬스가 DOWN
     */
    default boolean isCritical() {
        return false;
    }

    /**
     * 타임아웃 (밀리초)
     */
    default long getTimeoutMs() {
        return 5000;  // 기본 5초
    }
}
