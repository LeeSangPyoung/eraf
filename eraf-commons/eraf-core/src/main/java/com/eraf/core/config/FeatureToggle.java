package com.eraf.core.config;

import com.eraf.core.config.feature.FeatureFlagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature Toggle Facade (Backward Compatible)
 *
 * <p>This class maintains backward compatibility with existing code while delegating
 * to the production-ready {@link FeatureFlagService} with DB persistence and Redis cache.
 *
 * <p><strong>Priority:</strong>
 * <ol>
 *   <li>Memory overrides (set via enable/disable methods)</li>
 *   <li>Database + Redis cache (via FeatureFlagService)</li>
 *   <li>Default: false</li>
 * </ol>
 *
 * <p><strong>Usage:</strong> Existing code using @Feature annotation or FeatureToggle.isEnabled()
 * will automatically benefit from the new production-ready system.
 *
 * @author ERAF Team
 * @since Phase 1 (updated in Phase 3 for production-readiness)
 */
public class FeatureToggle {

    private static final Logger log = LoggerFactory.getLogger(FeatureToggle.class);

    /**
     * Memory overrides (for runtime enable/disable without DB)
     * These take priority over DB/Redis cache
     */
    private final Map<String, Boolean> memoryOverrides = new ConcurrentHashMap<>();

    /**
     * Production-ready feature flag service (optional, injected if available)
     */
    private final FeatureFlagService featureFlagService;

    /**
     * Constructor with FeatureFlagService (production mode)
     *
     * @param featureFlagService feature flag service with DB + Redis support
     */
    public FeatureToggle(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
        log.info("FeatureToggle initialized with production-ready backend (DB + Redis)");
    }

    /**
     * Constructor without FeatureFlagService (memory-only mode, backward compatible)
     */
    public FeatureToggle() {
        this.featureFlagService = null;
        log.info("FeatureToggle initialized in memory-only mode (no DB/Redis)");
    }

    /**
     * Check if feature is enabled
     *
     * <p>Priority:
     * <ol>
     *   <li>Memory override (if set via enable/disable)</li>
     *   <li>FeatureFlagService (DB + Redis)</li>
     *   <li>Default: false</li>
     * </ol>
     *
     * @param featureName the feature name
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String featureName) {
        // Priority 1: Memory override
        if (memoryOverrides.containsKey(featureName)) {
            boolean enabled = memoryOverrides.get(featureName);
            log.trace("Feature '{}' resolved from memory override: {}", featureName, enabled);
            return enabled;
        }

        // Priority 2: FeatureFlagService (DB + Redis)
        if (featureFlagService != null) {
            boolean enabled = featureFlagService.isEnabled(featureName);
            log.trace("Feature '{}' resolved from FeatureFlagService: {}", featureName, enabled);
            return enabled;
        }

        // Priority 3: Default false
        log.trace("Feature '{}' not found, defaulting to false", featureName);
        return false;
    }

    /**
     * Enable feature (memory override)
     *
     * <p>This sets a runtime memory override that takes priority over DB/Redis.
     * Use this for temporary runtime toggles during debugging or testing.
     *
     * @param featureName the feature name
     */
    public void enable(String featureName) {
        memoryOverrides.put(featureName, true);
        log.debug("Feature '{}' enabled (memory override)", featureName);
    }

    /**
     * Disable feature (memory override)
     *
     * <p>This sets a runtime memory override that takes priority over DB/Redis.
     * Use this for temporary runtime toggles during debugging or testing.
     *
     * @param featureName the feature name
     */
    public void disable(String featureName) {
        memoryOverrides.put(featureName, false);
        log.debug("Feature '{}' disabled (memory override)", featureName);
    }

    /**
     * Toggle feature (memory override)
     *
     * @param featureName the feature name
     */
    public void toggle(String featureName) {
        boolean currentValue = isEnabled(featureName);
        memoryOverrides.put(featureName, !currentValue);
        log.debug("Feature '{}' toggled to {} (memory override)", featureName, !currentValue);
    }

    /**
     * Set feature enabled/disabled (memory override)
     *
     * @param featureName the feature name
     * @param enabled the enabled status
     */
    public void set(String featureName, boolean enabled) {
        memoryOverrides.put(featureName, enabled);
        log.debug("Feature '{}' set to {} (memory override)", featureName, enabled);
    }

    /**
     * Get all feature flags (memory overrides only)
     *
     * <p>Note: This returns only memory overrides, not flags from DB/Redis.
     * For a complete list, use FeatureFlagService.findAll().
     *
     * @return map of feature names to enabled status (memory overrides only)
     */
    public Map<String, Boolean> getAll() {
        return Map.copyOf(memoryOverrides);
    }

    /**
     * Clear all memory overrides
     *
     * <p>Note: This only clears memory overrides. Flags in DB/Redis are unaffected.
     */
    public void clear() {
        memoryOverrides.clear();
        log.debug("All memory overrides cleared");
    }

    /**
     * Clear specific memory override
     *
     * <p>After clearing, the feature will fall back to DB/Redis or default false.
     *
     * @param featureName the feature name
     */
    public void clearOverride(String featureName) {
        memoryOverrides.remove(featureName);
        log.debug("Memory override cleared for feature '{}'", featureName);
    }

    /**
     * Get all feature flags including DB/Redis (if available)
     *
     * @return map of all feature names to enabled status
     */
    public Map<String, Boolean> getAllIncludingService() {
        Map<String, Boolean> allFlags = new HashMap<>();

        // Add flags from FeatureFlagService
        if (featureFlagService != null) {
            featureFlagService.findAll().forEach(flag ->
                allFlags.put(flag.getFlagKey(), flag.getEnabled())
            );
        }

        // Override with memory overrides (higher priority)
        allFlags.putAll(memoryOverrides);

        return allFlags;
    }
}
