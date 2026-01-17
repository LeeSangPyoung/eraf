package com.eraf.gateway.circuitbreaker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Circuit Breaker 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.circuit-breaker")
public class CircuitBreakerProperties {

    /**
     * Circuit Breaker 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 기본 실패 임계값 (이 횟수 이상 실패하면 OPEN)
     */
    private int defaultFailureThreshold = 5;

    /**
     * 기본 성공 임계값 (HALF_OPEN 상태에서 이 횟수 이상 성공하면 CLOSED)
     */
    private int defaultSuccessThreshold = 2;

    /**
     * 기본 OPEN 타임아웃 (밀리초)
     * OPEN 상태에서 이 시간이 지나면 HALF_OPEN으로 전환 시도
     */
    private long defaultOpenTimeoutMs = 60000;

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * 실패로 간주할 HTTP 상태 코드 시작 값
     */
    private int failureStatusThreshold = 500;
}
