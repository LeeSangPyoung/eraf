package com.eraf.gateway.cache;

import java.util.Optional;

/**
 * 응답 캐시 저장소 인터페이스
 */
public interface ResponseCacheRepository {

    /**
     * 캐시 조회
     */
    Optional<CachedResponse> get(String key);

    /**
     * 캐시 저장
     */
    void put(String key, CachedResponse response);

    /**
     * 캐시 삭제
     */
    void evict(String key);

    /**
     * 패턴 기반 캐시 삭제
     */
    void evictByPattern(String pattern);

    /**
     * 전체 캐시 삭제
     */
    void clear();

    /**
     * 만료된 캐시 정리
     */
    void cleanupExpired();
}
