package com.eraf.core.config.feature;

import com.eraf.core.config.FeatureToggle;
import com.eraf.core.config.FeatureToggleAspect;
import com.eraf.core.config.feature.evaluator.FeatureFlagEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Auto-configuration for Production-ready Feature Flag system
 *
 * <p>Conditional activation:
 * <ul>
 *   <li>Requires {@code eraf.feature-flag.enabled=true} (default: true)</li>
 *   <li>Redis cache is optional (falls back to 2-tier if unavailable)</li>
 *   <li>JPA repository is required for DB persistence</li>
 * </ul>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@AutoConfiguration
@ConditionalOnProperty(name = "eraf.feature-flag.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FeatureFlagProperties.class)
@EnableAsync
@ComponentScan(basePackages = "com.eraf.core.config.feature.evaluator")
public class FeatureFlagAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagAutoConfiguration.class);

    @PostConstruct
    public void init() {
        log.info("Production-ready Feature Flag system initialized");
    }

    /**
     * Feature Flag Cache Manager (L2 - Redis)
     *
     * <p>Redis is optional - if unavailable, falls back to 2-tier (memory + DB).
     */
    @Bean
    @ConditionalOnMissingBean
    public FeatureFlagCacheManager featureFlagCacheManager(
            FeatureFlagProperties properties,
            @Autowired(required = false) RedisTemplate<String, Boolean> redisTemplate) {

        if (redisTemplate != null && properties.getCache().isRedisEnabled()) {
            log.info("FeatureFlagCacheManager initialized with Redis (L2 TTL: {})", properties.getCache().getL2Ttl());
        } else {
            log.info("FeatureFlagCacheManager initialized without Redis (2-tier: memory + DB)");
        }
        return new FeatureFlagCacheManager(redisTemplate, properties);
    }

    /**
     * Feature Flag Service (core business logic with pluggable evaluators)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(FeatureFlagRepository.class)
    public FeatureFlagService featureFlagService(
            FeatureFlagRepository repository,
            FeatureFlagCacheManager cacheManager,
            FeatureFlagProperties properties,
            List<FeatureFlagEvaluator> evaluators) {

        log.info("FeatureFlagService registered with 3-tier caching and {} evaluators", evaluators.size());
        return new FeatureFlagService(repository, cacheManager, properties, evaluators);
    }

    /**
     * FeatureToggle facade (backward compatible)
     *
     * <p>If FeatureFlagService is available, delegates to it.
     * Otherwise, operates in memory-only mode.
     */
    @Bean
    @ConditionalOnMissingBean
    public FeatureToggle featureToggle(
            @Autowired(required = false) FeatureFlagService featureFlagService) {

        if (featureFlagService != null) {
            return new FeatureToggle(featureFlagService);
        }
        return new FeatureToggle();
    }

    /**
     * FeatureToggleAspect (@Feature 어노테이션 처리)
     */
    @Bean
    @ConditionalOnMissingBean
    public FeatureToggleAspect featureToggleAspect(
            FeatureToggle featureToggle,
            @Autowired(required = false) org.springframework.beans.factory.BeanFactory beanFactory) {
        return new FeatureToggleAspect(featureToggle, beanFactory);
    }

    /**
     * Feature Flag Default Loader (loads default flags from application.yml on startup)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(FeatureFlagRepository.class)
    @ConditionalOnProperty(name = "eraf.feature-flag.defaults[0].key")
    public FeatureFlagDefaultLoader featureFlagDefaultLoader(
            FeatureFlagRepository repository,
            FeatureFlagProperties properties) {

        log.info("FeatureFlagDefaultLoader registered - will load {} default flags",
            properties.getDefaults().size());
        return new FeatureFlagDefaultLoader(repository, properties);
    }
}
