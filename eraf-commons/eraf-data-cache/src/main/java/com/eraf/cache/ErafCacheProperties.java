package com.eraf.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ERAF Cache Configuration Properties
 *
 * <p>설정 예시:</p>
 * <pre>
 * eraf:
 *   cache:
 *     type: caffeine  # simple, caffeine, redis, multi-level
 *     default-ttl: 30m
 *     statistics-enabled: true
 *     caffeine:
 *       max-size: 10000
 *       expire-after-write: 1h
 *     caches:
 *       userCache:
 *         ttl: 5m
 *         max-size: 1000
 *       productCache:
 *         ttl: 1h
 *         max-size: 5000
 * </pre>
 */
@ConfigurationProperties(prefix = "eraf.cache")
public class ErafCacheProperties {

    /**
     * Cache type: simple, caffeine, redis, multi-level
     */
    private CacheType type = CacheType.SIMPLE;

    /**
     * Default TTL for all caches
     */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /**
     * Enable cache statistics
     */
    private boolean statisticsEnabled = false;

    /**
     * Cache key prefix
     */
    private String keyPrefix = "eraf:cache:";

    /**
     * Caffeine cache configuration
     */
    private Caffeine caffeine = new Caffeine();

    /**
     * Redis cache configuration
     */
    private Redis redis = new Redis();

    /**
     * Multi-level cache configuration
     */
    private MultiLevel multiLevel = new MultiLevel();

    /**
     * Named cache configurations
     */
    private Map<String, CacheSpec> caches = new HashMap<>();

    // Getters and Setters

    public CacheType getType() {
        return type;
    }

    public void setType(CacheType type) {
        this.type = type;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Caffeine getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(Caffeine caffeine) {
        this.caffeine = caffeine;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public MultiLevel getMultiLevel() {
        return multiLevel;
    }

    public void setMultiLevel(MultiLevel multiLevel) {
        this.multiLevel = multiLevel;
    }

    public Map<String, CacheSpec> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheSpec> caches) {
        this.caches = caches;
    }

    /**
     * Cache Type Enum
     */
    public enum CacheType {
        SIMPLE,
        CAFFEINE,
        REDIS,
        MULTI_LEVEL
    }

    /**
     * Caffeine Cache Configuration
     */
    public static class Caffeine {
        /**
         * Maximum number of entries
         */
        private int maxSize = 10000;

        /**
         * Expire after write
         */
        private Duration expireAfterWrite = Duration.ofHours(1);

        /**
         * Expire after access
         */
        private Duration expireAfterAccess;

        /**
         * Initial capacity
         */
        private int initialCapacity = 100;

        /**
         * Record statistics
         */
        private boolean recordStats = false;

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Duration getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Duration expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }

        public int getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public boolean isRecordStats() {
            return recordStats;
        }

        public void setRecordStats(boolean recordStats) {
            this.recordStats = recordStats;
        }
    }

    /**
     * Redis Cache Configuration
     */
    public static class Redis {
        /**
         * Enable null values
         */
        private boolean enableNullValues = true;

        /**
         * Use key prefix
         */
        private boolean useKeyPrefix = true;

        /**
         * Time to live
         */
        private Duration ttl = Duration.ofMinutes(30);

        public boolean isEnableNullValues() {
            return enableNullValues;
        }

        public void setEnableNullValues(boolean enableNullValues) {
            this.enableNullValues = enableNullValues;
        }

        public boolean isUseKeyPrefix() {
            return useKeyPrefix;
        }

        public void setUseKeyPrefix(boolean useKeyPrefix) {
            this.useKeyPrefix = useKeyPrefix;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    /**
     * Multi-Level Cache Configuration
     */
    public static class MultiLevel {
        /**
         * L1 cache type (usually Caffeine)
         */
        private String l1CacheType = "caffeine";

        /**
         * L2 cache type (usually Redis)
         */
        private String l2CacheType = "redis";

        /**
         * L1 max size
         */
        private int l1MaxSize = 1000;

        /**
         * L1 TTL
         */
        private Duration l1Ttl = Duration.ofMinutes(5);

        /**
         * L2 TTL
         */
        private Duration l2Ttl = Duration.ofMinutes(30);

        public String getL1CacheType() {
            return l1CacheType;
        }

        public void setL1CacheType(String l1CacheType) {
            this.l1CacheType = l1CacheType;
        }

        public String getL2CacheType() {
            return l2CacheType;
        }

        public void setL2CacheType(String l2CacheType) {
            this.l2CacheType = l2CacheType;
        }

        public int getL1MaxSize() {
            return l1MaxSize;
        }

        public void setL1MaxSize(int l1MaxSize) {
            this.l1MaxSize = l1MaxSize;
        }

        public Duration getL1Ttl() {
            return l1Ttl;
        }

        public void setL1Ttl(Duration l1Ttl) {
            this.l1Ttl = l1Ttl;
        }

        public Duration getL2Ttl() {
            return l2Ttl;
        }

        public void setL2Ttl(Duration l2Ttl) {
            this.l2Ttl = l2Ttl;
        }
    }

    /**
     * Named Cache Specification
     */
    public static class CacheSpec {
        /**
         * TTL for this cache
         */
        private Duration ttl;

        /**
         * Max size (for Caffeine)
         */
        private Integer maxSize;

        /**
         * Expire after write
         */
        private Duration expireAfterWrite;

        /**
         * Expire after access
         */
        private Duration expireAfterAccess;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Duration getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Duration expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }
    }
}
