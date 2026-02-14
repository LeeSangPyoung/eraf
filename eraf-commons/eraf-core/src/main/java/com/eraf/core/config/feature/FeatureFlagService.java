package com.eraf.core.config.feature;

import com.eraf.core.config.feature.evaluator.FeatureFlagEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature Flag Service
 *
 * <p>Core business logic for feature flag evaluation with 3-tier caching:
 * <ul>
 *   <li>L1: In-memory cache (5s TTL)</li>
 *   <li>L2: Redis cache (60s TTL)</li>
 *   <li>L3: Database (source of truth)</li>
 * </ul>
 *
 * @author ERAF Team
 * @since Phase 3
 */
public class FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);

    private final FeatureFlagRepository repository;
    private final FeatureFlagCacheManager cacheManager;
    private final FeatureFlagProperties properties;
    private final List<FeatureFlagEvaluator> evaluators;

    // L1 cache: In-memory with TTL
    private final Map<String, CachedFlag> memoryCache = new ConcurrentHashMap<>();

    public FeatureFlagService(
            FeatureFlagRepository repository,
            FeatureFlagCacheManager cacheManager,
            FeatureFlagProperties properties,
            List<FeatureFlagEvaluator> evaluators) {
        this.repository = repository;
        this.cacheManager = cacheManager;
        this.properties = properties;
        this.evaluators = evaluators;
    }

    /**
     * Check if a feature flag is enabled (simple evaluation)
     *
     * @param flagKey the feature flag key
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String flagKey) {
        return isEnabled(flagKey, null);
    }

    /**
     * Check if a feature flag is enabled (with context for advanced targeting)
     *
     * @param flagKey the feature flag key
     * @param context context map for evaluation (userId, userGroups, etc.)
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String flagKey, Map<String, Object> context) {
        try {
            // Check L1 cache first
            CachedFlag cached = memoryCache.get(flagKey);
            if (cached != null && !cached.isExpired(properties.getCache().getL1Ttl())) {
                log.debug("Feature flag '{}' found in L1 cache: {}", flagKey, cached.enabled);
                incrementCheckCountAsync(flagKey);
                return cached.enabled;
            }

            // Check L2 cache (Redis) via CacheManager
            Boolean enabled = cacheManager.getEnabled(flagKey);
            if (enabled != null) {
                log.debug("Feature flag '{}' found in L2 cache: {}", flagKey, enabled);
                // Populate L1 cache
                memoryCache.put(flagKey, new CachedFlag(enabled, Instant.now()));
                incrementCheckCountAsync(flagKey);
                return enabled;
            }

            // Check L3 (Database)
            Optional<FeatureFlagEntity> flagOpt = repository.findByFlagKey(flagKey);
            if (flagOpt.isPresent()) {
                FeatureFlagEntity flag = flagOpt.get();

                // Evaluate based on flag type and context
                boolean result = evaluateFlag(flag, context);

                log.debug("Feature flag '{}' found in DB: enabled={}, evaluated={}",
                    flagKey, flag.getEnabled(), result);

                // Populate L2 and L1 caches
                cacheManager.putEnabled(flagKey, result);
                memoryCache.put(flagKey, new CachedFlag(result, Instant.now()));

                incrementCheckCountAsync(flagKey);
                return result;
            }

            // Flag not found - default to false
            log.debug("Feature flag '{}' not found, defaulting to false", flagKey);
            return false;

        } catch (Exception e) {
            log.error("Error checking feature flag '{}': {}", flagKey, e.getMessage(), e);
            return false; // Fail closed (disable feature on error)
        }
    }

    /**
     * Evaluate feature flag based on type and context using pluggable evaluators
     *
     * @param flag the feature flag entity
     * @param context evaluation context
     * @return true if flag should be enabled for this context
     */
    private boolean evaluateFlag(FeatureFlagEntity flag, Map<String, Object> context) {
        // Find the appropriate evaluator for this flag type
        for (FeatureFlagEvaluator evaluator : evaluators) {
            if (evaluator.supports(flag.getFlagType())) {
                return evaluator.evaluate(flag, context);
            }
        }

        // No evaluator found - fall back to enabled status
        log.warn("No evaluator found for flag type {} (flag: {}), returning enabled status",
            flag.getFlagType(), flag.getFlagKey());
        return flag.getEnabled();
    }

    /**
     * Increment check count asynchronously (for statistics)
     */
    @Async
    protected void incrementCheckCountAsync(String flagKey) {
        if (!properties.getStatistics().isEnabled()) {
            return;
        }

        try {
            repository.incrementCheckCount(flagKey, Instant.now());
        } catch (Exception e) {
            log.debug("Failed to increment check count for flag '{}': {}", flagKey, e.getMessage());
            // Ignore errors - statistics are not critical
        }
    }

    /**
     * Find feature flag by key
     *
     * @param flagKey the feature flag key
     * @return Optional containing the flag if found
     */
    @Transactional(readOnly = true)
    public Optional<FeatureFlagEntity> findByKey(String flagKey) {
        return repository.findByFlagKey(flagKey);
    }

    /**
     * Find all feature flags
     *
     * @return list of all feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlagEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Evict flag from cache
     *
     * @param flagKey the feature flag key
     */
    public void evictCache(String flagKey) {
        memoryCache.remove(flagKey);
        cacheManager.evict(flagKey);
        log.debug("Evicted cache for feature flag '{}'", flagKey);
    }

    /**
     * Evict all flags from cache
     */
    public void evictAllCaches() {
        memoryCache.clear();
        cacheManager.evictAll();
        log.info("Evicted all feature flag caches");
    }

    /**
     * Cached flag entry with TTL
     */
    private static class CachedFlag {
        final boolean enabled;
        final Instant cachedAt;

        CachedFlag(boolean enabled, Instant cachedAt) {
            this.enabled = enabled;
            this.cachedAt = cachedAt;
        }

        boolean isExpired(java.time.Duration ttl) {
            return Instant.now().isAfter(cachedAt.plus(ttl));
        }
    }
}
