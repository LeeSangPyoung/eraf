package com.eraf.gateway.domain;

import com.eraf.core.utils.PathMatcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Rate Limit 규칙 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRule {

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
    private RateLimitType type;

    /**
     * 시간 윈도우 (초)
     */
    private int windowSeconds;

    /**
     * 윈도우당 최대 요청 수
     */
    private int maxRequests;

    /**
     * 버스트 허용 여부
     */
    private boolean burstAllowed;

    /**
     * 버스트 최대 요청 수 (burstAllowed가 true일 때)
     */
    private int burstMaxRequests;

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
     * Rate Limit 타입
     */
    public enum RateLimitType {
        /**
         * IP 기반 Rate Limit
         */
        IP,
        /**
         * API Key 기반 Rate Limit
         */
        API_KEY,
        /**
         * 사용자 기반 Rate Limit
         */
        USER,
        /**
         * 글로벌 Rate Limit (전체 요청에 적용)
         */
        GLOBAL
    }

    /**
     * 해당 Rate Limit 규칙이 유효한지 확인
     */
    public boolean isValid() {
        return enabled && windowSeconds > 0 && maxRequests > 0;
    }

    /**
     * 특정 경로가 이 규칙의 적용 대상인지 확인
     */
    public boolean matchesPath(String path) {
        return PathMatcher.matches(path, pathPattern);
    }
}
