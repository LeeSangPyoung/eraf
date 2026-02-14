package com.eraf.cache.statistics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.*;

/**
 * Cache Statistics Collector
 *
 * <p>캐시 통계를 수집하고 조회하는 유틸리티</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @Autowired}
 * private CacheStatisticsCollector statsCollector;
 *
 * // 모든 캐시 통계 조회
 * Collection&lt;CacheStatistics&gt; allStats = statsCollector.getAllStatistics();
 *
 * // 특정 캐시 통계 조회
 * CacheStatistics userCacheStats = statsCollector.getStatistics("userCache");
 * </pre>
 */
public class CacheStatisticsCollector {

    private final CacheManager cacheManager;

    public CacheStatisticsCollector(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get statistics for specific cache
     */
    public CacheStatistics getStatistics(String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }

        CacheStatistics stats = new CacheStatistics(cacheName);

        // If Caffeine cache, extract detailed statistics
        if (cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) cache;
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

            CacheStats cacheStats = nativeCache.stats();
            stats.setHitCount(cacheStats.hitCount());
            stats.setMissCount(cacheStats.missCount());
            stats.setLoadSuccessCount(cacheStats.loadSuccessCount());
            stats.setLoadFailureCount(cacheStats.loadFailureCount());
            stats.setEvictionCount(cacheStats.evictionCount());
            stats.setSize(nativeCache.estimatedSize());
        }

        return stats;
    }

    /**
     * Get statistics for all caches
     */
    public Collection<CacheStatistics> getAllStatistics() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        List<CacheStatistics> allStats = new ArrayList<>();

        for (String cacheName : cacheNames) {
            CacheStatistics stats = getStatistics(cacheName);
            if (stats != null) {
                allStats.add(stats);
            }
        }

        return allStats;
    }

    /**
     * Get statistics map (cacheName -> CacheStatistics)
     */
    public Map<String, CacheStatistics> getStatisticsMap() {
        Map<String, CacheStatistics> statsMap = new HashMap<>();

        for (CacheStatistics stats : getAllStatistics()) {
            statsMap.put(stats.getCacheName(), stats);
        }

        return statsMap;
    }

    /**
     * Clear all caches
     */
    public void clearAll() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    /**
     * Clear specific cache
     */
    public void clear(String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Get cache manager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
