package com.eraf.redis.statistics;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 캐시 통계 추적기
 *
 * <p>주요 기능:</p>
 * <ul>
 *     <li>캐시 히트율 추적 (Hit Rate)</li>
 *     <li>캐시 미스율 추적 (Miss Rate)</li>
 *     <li>캐시 크기 모니터링</li>
 *     <li>평균 응답 시간 추적</li>
 *     <li>캐시별 통계 분리</li>
 * </ul>
 */
@Component
public class RedisCacheStatistics {

    private final StringRedisTemplate redisTemplate;

    // 캐시별 통계 저장
    private final Map<String, CacheStats> statsMap = new ConcurrentHashMap<>();

    // 전역 통계
    private final AtomicLong globalHits = new AtomicLong(0);
    private final AtomicLong globalMisses = new AtomicLong(0);
    private final AtomicLong globalPuts = new AtomicLong(0);
    private final AtomicLong globalEvictions = new AtomicLong(0);

    private final Instant startTime = Instant.now();

    public RedisCacheStatistics(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 캐시 히트 기록
     */
    public void recordHit(String cacheName) {
        globalHits.incrementAndGet();
        getOrCreateStats(cacheName).recordHit();
    }

    /**
     * 캐시 미스 기록
     */
    public void recordMiss(String cacheName) {
        globalMisses.incrementAndGet();
        getOrCreateStats(cacheName).recordMiss();
    }

    /**
     * 캐시 저장 기록
     */
    public void recordPut(String cacheName, long sizeBytes) {
        globalPuts.incrementAndGet();
        getOrCreateStats(cacheName).recordPut(sizeBytes);
    }

    /**
     * 캐시 제거 기록
     */
    public void recordEviction(String cacheName) {
        globalEvictions.incrementAndGet();
        getOrCreateStats(cacheName).recordEviction();
    }

    /**
     * 캐시 응답 시간 기록 (밀리초)
     */
    public void recordLatency(String cacheName, long latencyMs) {
        getOrCreateStats(cacheName).recordLatency(latencyMs);
    }

    /**
     * 전역 통계 조회
     */
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long hits = globalHits.get();
        long misses = globalMisses.get();
        long total = hits + misses;

        stats.put("totalHits", hits);
        stats.put("totalMisses", misses);
        stats.put("totalRequests", total);
        stats.put("hitRate", total > 0 ? (double) hits / total * 100 : 0.0);
        stats.put("missRate", total > 0 ? (double) misses / total * 100 : 0.0);
        stats.put("totalPuts", globalPuts.get());
        stats.put("totalEvictions", globalEvictions.get());
        stats.put("uptimeSeconds", Duration.between(startTime, Instant.now()).getSeconds());
        stats.put("cacheCount", statsMap.size());

        // Redis 서버 정보
        try {
            stats.put("redisConnected", redisTemplate.getConnectionFactory().getConnection().ping() != null);
        } catch (Exception e) {
            stats.put("redisConnected", false);
        }

        return stats;
    }

    /**
     * 특정 캐시 통계 조회
     */
    public Map<String, Object> getCacheStatistics(String cacheName) {
        CacheStats stats = statsMap.get(cacheName);
        if (stats == null) {
            return Map.of("exists", false);
        }
        return stats.toMap();
    }

    /**
     * 모든 캐시 통계 조회
     */
    public Map<String, Map<String, Object>> getAllCacheStatistics() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();
        statsMap.forEach((name, stats) -> allStats.put(name, stats.toMap()));
        return allStats;
    }

    /**
     * 통계 초기화
     */
    public void reset() {
        globalHits.set(0);
        globalMisses.set(0);
        globalPuts.set(0);
        globalEvictions.set(0);
        statsMap.clear();
    }

    /**
     * 캐시별 통계 초기화
     */
    public void resetCache(String cacheName) {
        statsMap.remove(cacheName);
    }

    private CacheStats getOrCreateStats(String cacheName) {
        return statsMap.computeIfAbsent(cacheName, k -> new CacheStats(cacheName));
    }

    /**
     * 캐시별 통계 클래스
     */
    private static class CacheStats {
        private final String cacheName;
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong puts = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);
        private final AtomicLong totalSizeBytes = new AtomicLong(0);
        private final AtomicLong totalLatencyMs = new AtomicLong(0);
        private final AtomicLong latencyCount = new AtomicLong(0);
        private final Instant createdAt = Instant.now();

        public CacheStats(String cacheName) {
            this.cacheName = cacheName;
        }

        public void recordHit() {
            hits.incrementAndGet();
        }

        public void recordMiss() {
            misses.incrementAndGet();
        }

        public void recordPut(long sizeBytes) {
            puts.incrementAndGet();
            totalSizeBytes.addAndGet(sizeBytes);
        }

        public void recordEviction() {
            evictions.incrementAndGet();
        }

        public void recordLatency(long latencyMs) {
            totalLatencyMs.addAndGet(latencyMs);
            latencyCount.incrementAndGet();
        }

        public Map<String, Object> toMap() {
            long hitCount = hits.get();
            long missCount = misses.get();
            long total = hitCount + missCount;
            long latCount = latencyCount.get();

            Map<String, Object> map = new HashMap<>();
            map.put("cacheName", cacheName);
            map.put("hits", hitCount);
            map.put("misses", missCount);
            map.put("totalRequests", total);
            map.put("hitRate", total > 0 ? (double) hitCount / total * 100 : 0.0);
            map.put("missRate", total > 0 ? (double) missCount / total * 100 : 0.0);
            map.put("puts", puts.get());
            map.put("evictions", evictions.get());
            map.put("totalSizeBytes", totalSizeBytes.get());
            map.put("averageLatencyMs", latCount > 0 ? (double) totalLatencyMs.get() / latCount : 0.0);
            map.put("ageSeconds", Duration.between(createdAt, Instant.now()).getSeconds());

            return map;
        }
    }
}
