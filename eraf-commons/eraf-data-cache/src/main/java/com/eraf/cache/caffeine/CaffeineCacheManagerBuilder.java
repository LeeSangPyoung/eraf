package com.eraf.cache.caffeine;

import com.eraf.cache.ErafCacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine CacheManager Builder
 *
 * <p>고급 Caffeine 캐시 설정을 지원하는 빌더</p>
 */
public class CaffeineCacheManagerBuilder {

    private final ErafCacheProperties properties;

    public CaffeineCacheManagerBuilder(ErafCacheProperties properties) {
        this.properties = properties;
    }

    /**
     * Build CaffeineCacheManager with ERAF properties
     */
    public CaffeineCacheManager build() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Set default cache configuration
        cacheManager.setCaffeine(buildDefaultCaffeineSpec());

        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(true);

        // Register per-cache configurations with individual Caffeine specs
        if (!properties.getCaches().isEmpty()) {
            for (Map.Entry<String, ErafCacheProperties.CacheSpec> entry : properties.getCaches().entrySet()) {
                String cacheName = entry.getKey();
                Caffeine<Object, Object> caffeineBuilder = buildForNamedCache(cacheName);
                cacheManager.registerCustomCache(cacheName, caffeineBuilder.build());
            }
        }

        return cacheManager;
    }

    /**
     * Build default Caffeine specification
     */
    private Caffeine<Object, Object> buildDefaultCaffeineSpec() {
        ErafCacheProperties.Caffeine caffeineProps = properties.getCaffeine();

        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .initialCapacity(caffeineProps.getInitialCapacity())
                .maximumSize(caffeineProps.getMaxSize());

        // Expire after write
        if (caffeineProps.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(
                    caffeineProps.getExpireAfterWrite().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        // Expire after access
        if (caffeineProps.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(
                    caffeineProps.getExpireAfterAccess().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        // Record statistics
        if (caffeineProps.isRecordStats() || properties.isStatisticsEnabled()) {
            builder.recordStats();
        }

        return builder;
    }

    /**
     * Build Caffeine for specific named cache
     */
    public Caffeine<Object, Object> buildForNamedCache(String cacheName) {
        ErafCacheProperties.CacheSpec spec = properties.getCaches().get(cacheName);
        if (spec == null) {
            return buildDefaultCaffeineSpec();
        }

        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // Initial capacity
        builder.initialCapacity(properties.getCaffeine().getInitialCapacity());

        // Max size
        if (spec.getMaxSize() != null) {
            builder.maximumSize(spec.getMaxSize());
        } else {
            builder.maximumSize(properties.getCaffeine().getMaxSize());
        }

        // Expire after write
        if (spec.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(
                    spec.getExpireAfterWrite().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } else if (properties.getCaffeine().getExpireAfterWrite() != null) {
            builder.expireAfterWrite(
                    properties.getCaffeine().getExpireAfterWrite().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        // Expire after access
        if (spec.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(
                    spec.getExpireAfterAccess().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } else if (properties.getCaffeine().getExpireAfterAccess() != null) {
            builder.expireAfterAccess(
                    properties.getCaffeine().getExpireAfterAccess().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        // Record statistics
        if (properties.getCaffeine().isRecordStats() || properties.isStatisticsEnabled()) {
            builder.recordStats();
        }

        return builder;
    }

    /**
     * Build map of Caffeine builders for all named caches
     */
    public Map<String, Caffeine<Object, Object>> buildNamedCaffeine() {
        Map<String, Caffeine<Object, Object>> caffeineMap = new java.util.HashMap<>();

        for (String cacheName : properties.getCaches().keySet()) {
            caffeineMap.put(cacheName, buildForNamedCache(cacheName));
        }

        return caffeineMap;
    }
}
