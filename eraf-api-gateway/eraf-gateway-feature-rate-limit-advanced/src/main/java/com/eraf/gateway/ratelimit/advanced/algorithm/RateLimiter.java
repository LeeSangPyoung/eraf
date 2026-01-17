package com.eraf.gateway.ratelimit.advanced.algorithm;

/**
 * Rate Limiter 인터페이스
 * 다양한 알고리즘 구현을 위한 공통 인터페이스
 */
public interface RateLimiter {

    /**
     * 요청 허용 여부 확인
     *
     * @param key 제한 키 (IP, API Key 등)
     * @return 허용 여부
     */
    boolean allowRequest(String key);

    /**
     * 남은 요청 수 조회
     *
     * @param key 제한 키
     * @return 남은 요청 수
     */
    long getRemainingRequests(String key);

    /**
     * 리셋 시간 조회 (초)
     *
     * @param key 제한 키
     * @return 리셋까지 남은 시간 (초)
     */
    long getResetTimeSeconds(String key);

    /**
     * 특정 키의 제한 초기화
     *
     * @param key 제한 키
     */
    void reset(String key);

    /**
     * 모든 제한 초기화
     */
    void resetAll();
}
