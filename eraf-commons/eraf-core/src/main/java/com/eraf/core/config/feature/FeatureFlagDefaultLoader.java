package com.eraf.core.config.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;

/**
 * Loads default feature flags from application.yml on application startup
 *
 * @author ERAF Team
 * @since Phase 3
 */
public class FeatureFlagDefaultLoader {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagDefaultLoader.class);

    private final FeatureFlagRepository repository;
    private final FeatureFlagProperties properties;

    public FeatureFlagDefaultLoader(FeatureFlagRepository repository, FeatureFlagProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * Load default flags when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadDefaultFlags() {
        if (properties.getDefaults().isEmpty()) {
            log.debug("No default feature flags to load");
            return;
        }

        int loaded = 0;
        int skipped = 0;

        for (FeatureFlagProperties.DefaultFlag defaultFlag : properties.getDefaults()) {
            try {
                if (repository.existsByFlagKey(defaultFlag.getKey())) {
                    log.debug("Feature flag '{}' already exists, skipping", defaultFlag.getKey());
                    skipped++;
                    continue;
                }

                FeatureFlagEntity entity = new FeatureFlagEntity();
                entity.setFlagKey(defaultFlag.getKey());
                entity.setName(defaultFlag.getName());
                entity.setDescription(defaultFlag.getDescription());
                entity.setEnabled(defaultFlag.getEnabled());
                entity.setFlagType(defaultFlag.getType());
                entity.setTargetingRules(defaultFlag.getTargetingRules());
                entity.setFallbackValue(defaultFlag.getFallbackValue());
                entity.setCreatedBy("system");
                entity.setCreatedAt(Instant.now());

                repository.save(entity);
                loaded++;

                log.info("Loaded default feature flag: '{}' (enabled={})",
                    defaultFlag.getKey(), defaultFlag.getEnabled());

            } catch (Exception e) {
                log.error("Failed to load default feature flag '{}': {}",
                    defaultFlag.getKey(), e.getMessage(), e);
            }
        }

        log.info("Feature flag default loader completed: {} loaded, {} skipped", loaded, skipped);
    }
}
