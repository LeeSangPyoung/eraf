package com.eraf.redis.admin;

import com.eraf.response.ApiResponse;
import com.eraf.redis.invalidation.RedisCacheInvalidator;
import com.eraf.redis.statistics.RedisCacheStatistics;
import com.eraf.redis.warming.CacheWarmer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Redis 캐시 모니터링 및 관리 API
 *
 * <p>캐시 통계 조회, 수동 워밍, 무효화 등의 관리 기능 제공</p>
 */
@RestController
@RequestMapping("/admin/cache")
@ConditionalOnProperty(name = "eraf.redis.statistics.enabled", havingValue = "true", matchIfMissing = true)
public class CacheMonitoringController {

    private final RedisCacheStatistics statistics;
    private final CacheWarmer cacheWarmer;
    private final RedisCacheInvalidator cacheInvalidator;

    public CacheMonitoringController(
            RedisCacheStatistics statistics,
            CacheWarmer cacheWarmer,
            RedisCacheInvalidator cacheInvalidator) {
        this.statistics = statistics;
        this.cacheWarmer = cacheWarmer;
        this.cacheInvalidator = cacheInvalidator;
    }

    /**
     * 전역 캐시 통계 조회
     *
     * @return 전역 통계 (히트율, 미스율, 총 요청 수 등)
     */
    @GetMapping("/statistics/global")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGlobalStatistics() {
        Map<String, Object> stats = statistics.getGlobalStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 특정 캐시 통계 조회
     *
     * @param cacheName 캐시 이름
     * @return 캐시별 통계
     */
    @GetMapping("/statistics/{cacheName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStatistics(
            @PathVariable String cacheName) {
        Map<String, Object> stats = statistics.getCacheStatistics(cacheName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 모든 캐시 통계 조회
     *
     * @return 전체 캐시별 통계
     */
    @GetMapping("/statistics/all")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getAllCacheStatistics() {
        Map<String, Map<String, Object>> allStats = statistics.getAllCacheStatistics();
        return ResponseEntity.ok(ApiResponse.success(allStats));
    }

    /**
     * 캐시 통계 초기화
     *
     * @return 성공 메시지
     */
    @DeleteMapping("/statistics")
    public ResponseEntity<ApiResponse<String>> resetStatistics() {
        statistics.reset();
        return ResponseEntity.ok(ApiResponse.success("Cache statistics reset successfully"));
    }

    /**
     * 특정 캐시 통계 초기화
     *
     * @param cacheName 캐시 이름
     * @return 성공 메시지
     */
    @DeleteMapping("/statistics/{cacheName}")
    public ResponseEntity<ApiResponse<String>> resetCacheStatistics(@PathVariable String cacheName) {
        statistics.resetCache(cacheName);
        return ResponseEntity.ok(ApiResponse.success("Cache statistics for " + cacheName + " reset successfully"));
    }

    /**
     * 모든 캐시 수동 워밍
     *
     * @return 워밍 결과
     */
    @PostMapping("/warm")
    public ResponseEntity<ApiResponse<CacheWarmer.WarmupResult>> warmAllCaches() {
        CacheWarmer.WarmupResult result = cacheWarmer.warmAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 특정 캐시 수동 워밍
     *
     * @param cacheName 캐시 이름
     * @return 성공 메시지
     */
    @PostMapping("/warm/{cacheName}")
    public ResponseEntity<ApiResponse<String>> warmCache(@PathVariable String cacheName) {
        cacheWarmer.warmCache(cacheName);
        return ResponseEntity.ok(ApiResponse.success("Cache " + cacheName + " warmed successfully"));
    }

    /**
     * 특정 캐시의 특정 키 무효화
     *
     * @param cacheName 캐시 이름
     * @param key 키
     * @return 성공 메시지
     */
    @DeleteMapping("/invalidate/{cacheName}/{key}")
    public ResponseEntity<ApiResponse<String>> invalidateCacheKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        cacheInvalidator.invalidate(cacheName, key);
        return ResponseEntity.ok(ApiResponse.success("Cache key invalidated: " + cacheName + ":" + key));
    }

    /**
     * 특정 캐시 전체 무효화
     *
     * @param cacheName 캐시 이름
     * @return 성공 메시지
     */
    @DeleteMapping("/invalidate/{cacheName}")
    public ResponseEntity<ApiResponse<String>> invalidateCache(@PathVariable String cacheName) {
        cacheInvalidator.invalidateAll(cacheName);
        return ResponseEntity.ok(ApiResponse.success("Cache invalidated: " + cacheName));
    }

    /**
     * 패턴 기반 캐시 무효화
     *
     * @param cacheName 캐시 이름
     * @param keyPattern 키 패턴 (예: "user:*")
     * @return 성공 메시지
     */
    @DeleteMapping("/invalidate/{cacheName}/pattern")
    public ResponseEntity<ApiResponse<String>> invalidateCachePattern(
            @PathVariable String cacheName,
            @RequestParam String keyPattern) {
        cacheInvalidator.invalidatePattern(cacheName, keyPattern);
        return ResponseEntity.ok(ApiResponse.success("Cache invalidated by pattern: " + cacheName + " - " + keyPattern));
    }

    /**
     * 캐시 상태 헬스 체크
     *
     * @return 캐시 시스템 상태
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "statistics", statistics.getGlobalStatistics(),
                "warmer", Map.of("available", cacheWarmer != null),
                "invalidator", Map.of("available", cacheInvalidator != null)
        );
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
