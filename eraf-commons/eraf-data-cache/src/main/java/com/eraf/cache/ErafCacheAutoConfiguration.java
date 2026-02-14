package com.eraf.cache;

import com.eraf.cache.caffeine.CaffeineCacheManagerBuilder;
import com.eraf.cache.statistics.CacheStatisticsCollector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ERAF Cache Auto Configuration
 *
 * <p>캐시 타입별 자동 설정:</p>
 * <ul>
 *     <li>SIMPLE: ConcurrentMapCacheManager (기본)</li>
 *     <li>CAFFEINE: Caffeine 고성능 로컬 캐시</li>
 *     <li>REDIS: Redis 분산 캐시 (eraf-data-redis 필요)</li>
 *     <li>MULTI_LEVEL: Caffeine(L1) + Redis(L2) 2단계 캐싱</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(CacheManager.class)
@EnableConfigurationProperties(ErafCacheProperties.class)
@EnableCaching
public class ErafCacheAutoConfiguration {

    /**
     * Simple Cache Configuration (기본)
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "eraf.cache.type", havingValue = "simple", matchIfMissing = true)
    static class SimpleCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(ErafCacheProperties properties) {
            ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
            cacheManager.setAllowNullValues(true);
            cacheManager.setStoreByValue(false);

            // Set named caches
            if (!properties.getCaches().isEmpty()) {
                cacheManager.setCacheNames(properties.getCaches().keySet());
            }

            return cacheManager;
        }
    }

    /**
     * Caffeine Cache Configuration
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Caffeine")
    @ConditionalOnProperty(name = "eraf.cache.type", havingValue = "caffeine")
    static class CaffeineCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(ErafCacheProperties properties) {
            CaffeineCacheManagerBuilder builder = new CaffeineCacheManagerBuilder(properties);
            return builder.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public CaffeineCacheManagerBuilder caffeineCacheManagerBuilder(ErafCacheProperties properties) {
            return new CaffeineCacheManagerBuilder(properties);
        }
    }

    /**
     * Redis Cache Configuration (eraf-data-redis 모듈 필요)
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheManager")
    @ConditionalOnProperty(name = "eraf.cache.type", havingValue = "redis")
    static class RedisCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(
                ErafCacheProperties properties,
                org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory) {

            // Redis CacheConfiguration
            org.springframework.data.redis.cache.RedisCacheConfiguration config =
                    org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(properties.getRedis().getTtl());

            // Disable null values caching if not enabled
            if (!properties.getRedis().isEnableNullValues()) {
                config = config.disableCachingNullValues();
            }

            if (properties.getRedis().isUseKeyPrefix()) {
                config = config.prefixCacheNameWith(properties.getKeyPrefix());
            }

            // Named cache configurations
            java.util.Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigs = new java.util.HashMap<>();

            for (java.util.Map.Entry<String, ErafCacheProperties.CacheSpec> entry : properties.getCaches().entrySet()) {
                String cacheName = entry.getKey();
                ErafCacheProperties.CacheSpec spec = entry.getValue();

                org.springframework.data.redis.cache.RedisCacheConfiguration namedConfig = config;
                if (spec.getTtl() != null) {
                    namedConfig = namedConfig.entryTtl(spec.getTtl());
                }
                cacheConfigs.put(cacheName, namedConfig);
            }

            return org.springframework.data.redis.cache.RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(config)
                    .withInitialCacheConfigurations(cacheConfigs)
                    .build();
        }
    }

    /**
     * Multi-Level Cache Configuration: Caffeine(L1) + Redis(L2)
     * L1 캐시 미스 시 L2 캐시에서 조회하는 2단계 캐싱 구성
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {
            "com.github.benmanes.caffeine.cache.Caffeine",
            "org.springframework.data.redis.cache.RedisCacheManager"
    })
    @ConditionalOnProperty(name = "eraf.cache.type", havingValue = "multi_level")
    static class MultiLevelCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheManager cacheManager(
                ErafCacheProperties properties,
                org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory) {

            ErafCacheProperties.MultiLevel mlProps = properties.getMultiLevel();

            // L1: Caffeine local cache
            CaffeineCacheManager l1CacheManager = new CaffeineCacheManager();
            l1CacheManager.setCaffeine(
                    com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                            .maximumSize(mlProps.getL1MaxSize())
                            .expireAfterWrite(mlProps.getL1Ttl().toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            );
            l1CacheManager.setAllowNullValues(true);

            // L2: Redis distributed cache
            org.springframework.data.redis.cache.RedisCacheConfiguration redisConfig =
                    org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(mlProps.getL2Ttl());

            if (properties.getRedis().isUseKeyPrefix()) {
                redisConfig = redisConfig.prefixCacheNameWith(properties.getKeyPrefix());
            }

            CacheManager l2CacheManager = org.springframework.data.redis.cache.RedisCacheManager
                    .builder(redisConnectionFactory)
                    .cacheDefaults(redisConfig)
                    .build();

            // Composite: L1 first, then L2
            org.springframework.cache.support.CompositeCacheManager compositeCacheManager =
                    new org.springframework.cache.support.CompositeCacheManager(l1CacheManager, l2CacheManager);
            compositeCacheManager.setFallbackToNoOpCache(false);

            return compositeCacheManager;
        }
    }

    /**
     * Cache Statistics Collector
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.cache.statistics-enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public CacheStatisticsCollector cacheStatisticsCollector(CacheManager cacheManager) {
        return new CacheStatisticsCollector(cacheManager);
    }
}
