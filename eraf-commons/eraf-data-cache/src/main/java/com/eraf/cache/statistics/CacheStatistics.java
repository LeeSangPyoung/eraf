package com.eraf.cache.statistics;

/**
 * Cache Statistics
 *
 * <p>캐시 통계 정보를 담는 클래스</p>
 */
public class CacheStatistics {

    private final String cacheName;
    private long hitCount = 0;
    private long missCount = 0;
    private long loadSuccessCount = 0;
    private long loadFailureCount = 0;
    private long evictionCount = 0;
    private long size = 0;

    public CacheStatistics(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public long getLoadSuccessCount() {
        return loadSuccessCount;
    }

    public void setLoadSuccessCount(long loadSuccessCount) {
        this.loadSuccessCount = loadSuccessCount;
    }

    public long getLoadFailureCount() {
        return loadFailureCount;
    }

    public void setLoadFailureCount(long loadFailureCount) {
        this.loadFailureCount = loadFailureCount;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    public void setEvictionCount(long evictionCount) {
        this.evictionCount = evictionCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Calculate hit rate (0.0 ~ 1.0)
     */
    public double getHitRate() {
        long totalRequests = hitCount + missCount;
        return totalRequests == 0 ? 0.0 : (double) hitCount / totalRequests;
    }

    /**
     * Calculate miss rate (0.0 ~ 1.0)
     */
    public double getMissRate() {
        return 1.0 - getHitRate();
    }

    /**
     * Get total request count
     */
    public long getTotalRequests() {
        return hitCount + missCount;
    }

    @Override
    public String toString() {
        return String.format(
                "CacheStatistics{cacheName='%s', hitCount=%d, missCount=%d, hitRate=%.2f%%, size=%d}",
                cacheName, hitCount, missCount, getHitRate() * 100, size
        );
    }
}
