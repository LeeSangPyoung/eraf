package com.eraf.starter.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Cache Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.cache")
public class ErafCacheProperties {

    /**
     * Cache type (simple, caffeine, redis)
     */
    private String type = "simple";

    /**
     * Default TTL
     */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /**
     * Maximum cache size (for local caches)
     */
    private int maxSize = 1000;

    /**
     * Cache key prefix
     */
    private String keyPrefix = "eraf:cache:";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
