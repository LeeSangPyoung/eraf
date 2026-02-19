package com.eraf.featureflag.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Feature Flag Cache Manager (L2 - Redis)
 *
 * <p>Manages Redis cache for feature flags with configurable TTL.
 * Falls back gracefully if Redis is unavailable.
 *
 * <p>Cache key format: {@code eraf:feature-flag:{flagKey}:enabled}
 *
 * @author ERAF Team
 * @since Phase 3
 */
public class FeatureFlagCacheManager {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagCacheManager.class);

    private final RedisTemplate<String, Boolean> redisTemplate;
    private final FeatureFlagProperties properties;
    private final boolean redisAvailable;

    /**
     * Constructor with Redis support
     *
     * @param redisTemplate Redis template (nullable if Redis not available)
     * @param properties feature flag properties
     */
    public FeatureFlagCacheManager(RedisTemplate<String, Boolean> redisTemplate, FeatureFlagProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.redisAvailable = redisTemplate != null && properties.getCache().isRedisEnabled();

        if (redisAvailable) {
            log.info("FeatureFlagCacheManager initialized with Redis support (TTL: {})",
                properties.getCache().getL2Ttl());
        } else {
            log.info("FeatureFlagCacheManager initialized without Redis (fallback to DB)");
        }
    }

    /**
     * Get feature flag enabled status from Redis cache
     *
     * @param flagKey the feature flag key
     * @return Boolean value if cached, null if not found or Redis unavailable
     */
    public Boolean getEnabled(String flagKey) {
        if (!redisAvailable) {
            return null;
        }

        try {
            String cacheKey = buildCacheKey(flagKey);
            Boolean enabled = redisTemplate.opsForValue().get(cacheKey);

            if (enabled != null) {
                log.trace("Cache HIT for flag '{}': {}", flagKey, enabled);
            } else {
                log.trace("Cache MISS for flag '{}'", flagKey);
            }

            return enabled;
        } catch (Exception e) {
            log.warn("Failed to get flag '{}' from Redis cache: {}", flagKey, e.getMessage());
            return null; // Fallback to DB
        }
    }

    /**
     * Put feature flag enabled status into Redis cache
     *
     * @param flagKey the feature flag key
     * @param enabled the enabled status
     */
    public void putEnabled(String flagKey, boolean enabled) {
        if (!redisAvailable) {
            return;
        }

        try {
            String cacheKey = buildCacheKey(flagKey);
            Duration ttl = properties.getCache().getL2Ttl();

            redisTemplate.opsForValue().set(cacheKey, enabled, ttl.toMillis(), TimeUnit.MILLISECONDS);

            log.trace("Cached flag '{}' = {} with TTL {}", flagKey, enabled, ttl);
        } catch (Exception e) {
            log.warn("Failed to cache flag '{}': {}", flagKey, e.getMessage());
            // Continue without caching - not critical
        }
    }

    /**
     * Evict specific feature flag from cache
     *
     * @param flagKey the feature flag key
     */
    public void evict(String flagKey) {
        if (!redisAvailable) {
            return;
        }

        try {
            String cacheKey = buildCacheKey(flagKey);
            redisTemplate.delete(cacheKey);

            log.debug("Evicted cache for flag '{}'", flagKey);
        } catch (Exception e) {
            log.warn("Failed to evict cache for flag '{}': {}", flagKey, e.getMessage());
        }
    }

    /**
     * Evict all feature flags from cache
     */
    public void evictAll() {
        if (!redisAvailable) {
            return;
        }

        try {
            String pattern = properties.getCache().getKeyPrefix() + "*";
            var keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Evicted {} feature flag cache entries", keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to evict all caches: {}", e.getMessage());
        }
    }

    /**
     * Build Redis cache key
     *
     * @param flagKey the feature flag key
     * @return full cache key with prefix
     */
    private String buildCacheKey(String flagKey) {
        return properties.getCache().getKeyPrefix() + flagKey + ":enabled";
    }

    /**
     * Check if Redis is available
     *
     * @return true if Redis is available and enabled
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }
}
