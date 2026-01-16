package com.eraf.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Rate Limit 요청 기록 (슬라이딩 윈도우용)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRecord {

    /**
     * 식별 키 (IP, API Key, User ID 등)
     */
    private String key;

    /**
     * 규칙 ID
     */
    private String ruleId;

    /**
     * 현재 윈도우의 요청 수
     */
    private int requestCount;

    /**
     * 윈도우 시작 시간
     */
    private Instant windowStart;

    /**
     * 마지막 요청 시간
     */
    private Instant lastRequest;

    /**
     * 요청 수 증가
     */
    public void incrementCount() {
        this.requestCount++;
        this.lastRequest = Instant.now();
    }

    /**
     * 윈도우 리셋
     */
    public void resetWindow() {
        this.requestCount = 1;
        this.windowStart = Instant.now();
        this.lastRequest = Instant.now();
    }

    /**
     * 윈도우가 만료되었는지 확인
     */
    public boolean isWindowExpired(int windowSeconds) {
        return Instant.now().isAfter(windowStart.plusSeconds(windowSeconds));
    }

    /**
     * 복합 키 생성 (ruleId + identifier)
     */
    public static String generateKey(String ruleId, String identifier) {
        return ruleId + ":" + identifier;
    }
}
