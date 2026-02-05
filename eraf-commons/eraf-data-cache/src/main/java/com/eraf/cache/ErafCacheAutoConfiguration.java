package com.eraf.cache;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Cache Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(CacheManager.class)
@EnableConfigurationProperties(ErafCacheProperties.class)
@EnableCaching
public class ErafCacheAutoConfiguration {

    /**
     * ConcurrentMapCacheManager 커스터마이저
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizer<ConcurrentMapCacheManager> erafCacheManagerCustomizer(ErafCacheProperties properties) {
        return cacheManager -> {
            cacheManager.setAllowNullValues(true);
            cacheManager.setStoreByValue(false);
        };
    }
}
