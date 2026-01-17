package com.eraf.gateway.ratelimit.advanced.domain;

import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 고급 Rate Limit 규칙
 * 기본 RateLimitRule을 확장하여 다양한 알고리즘과 분산 처리 지원
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedRateLimitRule {

    private String id;

    /**
     * 규칙 이름
     */
    private String name;

    /**
     * 적용 경로 패턴
     */
    private String pathPattern;

    /**
     * Rate Limit 타입
     */
    private RateLimitRule.RateLimitType type;

    /**
     * 알고리즘 타입
     */
    @Builder.Default
    private RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;

    /**
     * 시간 윈도우 (초)
     */
    private int windowSeconds;

    /**
     * 윈도우당 최대 요청 수
     */
    private int maxRequests;

    /**
     * 버스트 크기 (Token Bucket에서 사용)
     * 토큰 버킷의 최대 용량
     */
    private int burstSize;

    /**
     * 리필 속도 (초당 토큰 수)
     * Token Bucket과 Leaky Bucket에서 사용
     */
    private double refillRate;

    /**
     * 분산 모드 활성화 (Redis 사용)
     */
    private boolean distributedMode;

    /**
     * Consumer별 제한 (user_id, api_key 등)
     */
    @Builder.Default
    private Map<String, ConsumerLimit> consumerLimits = new HashMap<>();

    /**
     * 헤더 기반 제한 (User-Agent, Custom headers 등)
     */
    @Builder.Default
    private Map<String, Integer> headerBasedLimits = new HashMap<>();

    /**
     * 활성화 여부
     */
    private boolean enabled;

    /**
     * 우선순위 (낮을수록 먼저 적용)
     */
    private int priority;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * Consumer별 제한 설정
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerLimit {
        /**
         * Consumer ID (user_id, api_key 등)
         */
        private String consumerId;

        /**
         * 최대 요청 수
         */
        private int maxRequests;

        /**
         * 버스트 크기
         */
        private int burstSize;

        /**
         * 리필 속도
         */
        private double refillRate;
    }

    /**
     * 규칙이 유효한지 확인
     */
    public boolean isValid() {
        return enabled && windowSeconds > 0 && maxRequests > 0;
    }

    /**
     * 기본 RateLimitRule로 변환 (하위 호환성)
     */
    public RateLimitRule toBasicRule() {
        return RateLimitRule.builder()
                .id(id)
                .name(name)
                .pathPattern(pathPattern)
                .type(type)
                .windowSeconds(windowSeconds)
                .maxRequests(maxRequests)
                .burstAllowed(burstSize > maxRequests)
                .burstMaxRequests(burstSize)
                .enabled(enabled)
                .priority(priority)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 특정 Consumer에 대한 제한 조회
     */
    public ConsumerLimit getConsumerLimit(String consumerId) {
        return consumerLimits.get(consumerId);
    }

    /**
     * 특정 헤더에 대한 제한 조회
     */
    public Integer getHeaderBasedLimit(String headerValue) {
        return headerBasedLimits.get(headerValue);
    }
}
