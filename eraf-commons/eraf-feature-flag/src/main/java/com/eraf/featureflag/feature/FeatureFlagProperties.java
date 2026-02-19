package com.eraf.featureflag.feature;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Feature Flag system
 *
 * <p>Usage in application.yml:
 * <pre>{@code
 * eraf:
 *   feature-flag:
 *     enabled: true
 *     admin-enabled: true
 *     cache:
 *       l1-ttl: 5s
 *       l2-ttl: 60s
 *       redis-enabled: true
 *     statistics:
 *       enabled: true
 *       async: true
 *     defaults:
 *       - key: experimental-api
 *         name: "Experimental API Features"
 *         enabled: false
 *         type: SIMPLE
 * }</pre>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@ConfigurationProperties(prefix = "eraf.feature-flag")
public class FeatureFlagProperties {

    /**
     * Enable feature flag system
     */
    private boolean enabled = true;

    /**
     * Enable admin API for feature flag management
     */
    private boolean adminEnabled = true;

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    /**
     * Statistics configuration
     */
    private Statistics statistics = new Statistics();

    /**
     * Default feature flags (loaded on startup)
     */
    private List<DefaultFlag> defaults = new ArrayList<>();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }

    public void setAdminEnabled(boolean adminEnabled) {
        this.adminEnabled = adminEnabled;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public List<DefaultFlag> getDefaults() {
        return defaults;
    }

    public void setDefaults(List<DefaultFlag> defaults) {
        this.defaults = defaults;
    }

    /**
     * Cache configuration for feature flags
     */
    public static class Cache {
        /**
         * L1 (in-memory) cache TTL
         */
        private Duration l1Ttl = Duration.ofSeconds(5);

        /**
         * L2 (Redis) cache TTL
         */
        private Duration l2Ttl = Duration.ofSeconds(60);

        /**
         * Statistics cache TTL
         */
        private Duration statisticsTtl = Duration.ofMinutes(5);

        /**
         * Enable Redis cache (L2)
         */
        private boolean redisEnabled = true;

        /**
         * Cache key prefix for Redis
         */
        private String keyPrefix = "eraf:feature-flag:";

        // Getters and Setters

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

        public Duration getStatisticsTtl() {
            return statisticsTtl;
        }

        public void setStatisticsTtl(Duration statisticsTtl) {
            this.statisticsTtl = statisticsTtl;
        }

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }

    /**
     * Statistics configuration for feature flags
     */
    public static class Statistics {
        /**
         * Enable check count tracking
         */
        private boolean enabled = true;

        /**
         * Update check count asynchronously (recommended for performance)
         */
        private boolean async = true;

        /**
         * Batch size for async updates
         */
        private int batchSize = 100;

        /**
         * Flush interval for async updates (milliseconds)
         */
        private long flushIntervalMs = 5000;

        // Getters and Setters

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAsync() {
            return async;
        }

        public void setAsync(boolean async) {
            this.async = async;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getFlushIntervalMs() {
            return flushIntervalMs;
        }

        public void setFlushIntervalMs(long flushIntervalMs) {
            this.flushIntervalMs = flushIntervalMs;
        }
    }

    /**
     * Default feature flag specification (loaded on startup)
     */
    public static class DefaultFlag {
        /**
         * Feature flag key (unique)
         */
        private String key;

        /**
         * Human-readable name
         */
        private String name;

        /**
         * Description
         */
        private String description;

        /**
         * Initial enabled state
         */
        private Boolean enabled = true;

        /**
         * Flag type
         */
        private FeatureFlagEntity.FlagType type = FeatureFlagEntity.FlagType.SIMPLE;

        /**
         * Targeting rules (JSON string)
         */
        private String targetingRules;

        /**
         * Fallback value (SpEL expression)
         */
        private String fallbackValue;

        /**
         * Tags (for categorization)
         */
        private Map<String, String> tags = new HashMap<>();

        /**
         * Metadata
         */
        private Map<String, Object> metadata = new HashMap<>();

        // Getters and Setters

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public FeatureFlagEntity.FlagType getType() {
            return type;
        }

        public void setType(FeatureFlagEntity.FlagType type) {
            this.type = type;
        }

        public String getTargetingRules() {
            return targetingRules;
        }

        public void setTargetingRules(String targetingRules) {
            this.targetingRules = targetingRules;
        }

        public String getFallbackValue() {
            return fallbackValue;
        }

        public void setFallbackValue(String fallbackValue) {
            this.fallbackValue = fallbackValue;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
