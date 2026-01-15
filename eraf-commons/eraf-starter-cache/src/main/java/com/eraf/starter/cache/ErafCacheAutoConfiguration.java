package com.eraf.starter.cache;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

/**
 * ERAF Cache Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(CacheManager.class)
@EnableConfigurationProperties(ErafCacheProperties.class)
@EnableCaching
public class ErafCacheAutoConfiguration {

}
