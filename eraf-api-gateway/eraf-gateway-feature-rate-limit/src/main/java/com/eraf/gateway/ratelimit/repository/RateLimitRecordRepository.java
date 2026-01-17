package com.eraf.gateway.ratelimit.repository;

import com.eraf.gateway.ratelimit.domain.RateLimitRecord;

import java.util.Optional;

/**
 * Rate Limit Record Repository 인터페이스
 * Rate Limit 요청 기록 저장용 (슬라이딩 윈도우)
 * 구현체: InMemory, Redis 등 (JPA는 성능 이슈로 비권장)
 */
public interface RateLimitRecordRepository {

    /**
     * 키로 레코드 조회
     */
    Optional<RateLimitRecord> findByKey(String key);

    /**
     * 레코드 저장/수정
     */
    RateLimitRecord save(RateLimitRecord record);

    /**
     * 레코드 삭제
     */
    void deleteByKey(String key);

    /**
     * 만료된 레코드 정리
     */
    void cleanupExpired();

    /**
     * 요청 수 증가 및 허용 여부 확인 (atomic operation)
     * @param key 식별 키
     * @param windowSeconds 윈도우 크기 (초)
     * @param maxRequests 최대 요청 수
     * @return 요청이 허용되면 true, 제한에 걸리면 false
     */
    boolean incrementAndCheck(String key, int windowSeconds, int maxRequests);

    /**
     * 현재 요청 수 조회
     */
    int getCurrentCount(String key);

    /**
     * 남은 요청 수 조회
     */
    int getRemainingRequests(String key, int maxRequests);

    /**
     * 윈도우 리셋까지 남은 시간 (초)
     */
    long getResetTimeSeconds(String key, int windowSeconds);
}
