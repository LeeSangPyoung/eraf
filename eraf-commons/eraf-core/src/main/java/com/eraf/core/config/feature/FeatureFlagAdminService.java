package com.eraf.core.config.feature;

import com.eraf.core.config.feature.dto.FeatureFlagRequest;
import com.eraf.core.config.feature.dto.FeatureFlagResponse;
import com.eraf.core.config.feature.dto.FeatureFlagStats;
import com.eraf.exception.BusinessException;
import com.eraf.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feature Flag Admin Service
 *
 * <p>Handles CRUD operations and admin-level management of feature flags.
 *
 * @author ERAF Team
 * @since Phase 2
 */
@Service
@Transactional
public class FeatureFlagAdminService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagAdminService.class);

    private final FeatureFlagRepository repository;
    private final FeatureFlagService service;
    private final FeatureFlagMapper mapper;

    public FeatureFlagAdminService(
            FeatureFlagRepository repository,
            FeatureFlagService service,
            FeatureFlagMapper mapper) {
        this.repository = repository;
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Create a new feature flag
     *
     * @param request the feature flag request
     * @return the created feature flag
     * @throws BusinessException if flag key already exists
     */
    public FeatureFlagResponse create(FeatureFlagRequest request) {
        log.info("Creating feature flag: {}", request.getFlagKey());

        // Check for duplicate
        if (repository.existsByFlagKey(request.getFlagKey())) {
            throw new BusinessException(CommonErrorCode.FEATURE_FLAG_ALREADY_EXISTS, request.getFlagKey());
        }

        // Validate targeting rules if provided
        validateTargetingRules(request.getTargetingRules());

        // Create entity
        FeatureFlagEntity entity = mapper.toEntity(request);
        FeatureFlagEntity saved = repository.save(entity);

        log.info("Created feature flag: {} (id={})", saved.getFlagKey(), saved.getId());

        // Evict cache for the new flag key
        service.evictCache(saved.getFlagKey());

        return mapper.toResponse(saved);
    }

    /**
     * Update an existing feature flag
     *
     * @param id the feature flag ID
     * @param request the feature flag request
     * @return the updated feature flag
     * @throws BusinessException if flag not found
     */
    public FeatureFlagResponse update(Long id, FeatureFlagRequest request) {
        log.info("Updating feature flag: id={}", id);

        // Find existing flag
        FeatureFlagEntity entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.FEATURE_FLAG_NOT_FOUND, String.valueOf(id)));

        // Validate targeting rules if provided
        validateTargetingRules(request.getTargetingRules());

        // Update entity
        mapper.updateEntity(entity, request);
        FeatureFlagEntity updated = repository.save(entity);

        log.info("Updated feature flag: {} (id={})", updated.getFlagKey(), updated.getId());

        // Evict cache
        service.evictCache(updated.getFlagKey());

        return mapper.toResponse(updated);
    }

    /**
     * Delete a feature flag
     *
     * @param id the feature flag ID
     * @throws BusinessException if flag not found
     */
    public void delete(Long id) {
        log.info("Deleting feature flag: id={}", id);

        FeatureFlagEntity entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.FEATURE_FLAG_NOT_FOUND, String.valueOf(id)));

        String flagKey = entity.getFlagKey();
        repository.deleteById(id);

        log.info("Deleted feature flag: {} (id={})", flagKey, id);

        // Evict cache
        service.evictCache(flagKey);
    }

    /**
     * Get feature flag by ID
     *
     * @param id the feature flag ID
     * @return the feature flag
     * @throws BusinessException if flag not found
     */
    @Transactional(readOnly = true)
    public FeatureFlagResponse getById(Long id) {
        FeatureFlagEntity entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.FEATURE_FLAG_NOT_FOUND, String.valueOf(id)));
        return mapper.toResponse(entity);
    }

    /**
     * Get feature flag by key
     *
     * @param flagKey the feature flag key
     * @return the feature flag
     * @throws BusinessException if flag not found
     */
    @Transactional(readOnly = true)
    public FeatureFlagResponse getByKey(String flagKey) {
        FeatureFlagEntity entity = repository.findByFlagKey(flagKey)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.FEATURE_FLAG_NOT_FOUND, flagKey));
        return mapper.toResponse(entity);
    }

    /**
     * Get all feature flags
     *
     * @return list of all feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getAll() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Toggle feature flag enabled status
     *
     * @param id the feature flag ID
     * @return the updated feature flag
     * @throws BusinessException if flag not found
     */
    public FeatureFlagResponse toggle(Long id) {
        log.info("Toggling feature flag: id={}", id);

        FeatureFlagEntity entity = repository.findById(id)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.FEATURE_FLAG_NOT_FOUND, String.valueOf(id)));

        entity.setEnabled(!entity.getEnabled());
        FeatureFlagEntity updated = repository.save(entity);

        log.info("Toggled feature flag: {} (id={}) to enabled={}",
            updated.getFlagKey(), updated.getId(), updated.getEnabled());

        // Evict cache
        service.evictCache(updated.getFlagKey());

        return mapper.toResponse(updated);
    }

    /**
     * Get feature flag statistics
     *
     * @return feature flag statistics
     */
    @Transactional(readOnly = true)
    public FeatureFlagStats getStats() {
        FeatureFlagStats stats = new FeatureFlagStats();

        // Total counts
        stats.setTotalCount(repository.count());
        stats.setEnabledCount(repository.countByEnabledTrue());
        stats.setDisabledCount(stats.getTotalCount() - stats.getEnabledCount());

        // Type counts
        stats.setSimpleCount(repository.countByFlagType(FeatureFlagEntity.FlagType.SIMPLE));
        stats.setUserBasedCount(repository.countByFlagType(FeatureFlagEntity.FlagType.USER_BASED));
        stats.setPercentageCount(repository.countByFlagType(FeatureFlagEntity.FlagType.PERCENTAGE));
        stats.setTimeWindowCount(repository.countByFlagType(FeatureFlagEntity.FlagType.TIME_WINDOW));
        stats.setCustomCount(repository.countByFlagType(FeatureFlagEntity.FlagType.CUSTOM));

        // Total checks across all flags
        stats.setTotalChecks(repository.findAll().stream()
            .mapToLong(FeatureFlagEntity::getCheckCount)
            .sum());

        return stats;
    }

    /**
     * Reload all caches (evict all)
     */
    public void reloadCache() {
        log.info("Reloading all feature flag caches");
        service.evictAllCaches();
        log.info("All feature flag caches reloaded");
    }

    /**
     * Test evaluation of a feature flag
     *
     * @param flagKey the feature flag key
     * @param context evaluation context (optional)
     * @return evaluation result
     */
    @Transactional(readOnly = true)
    public Map<String, Object> evaluate(String flagKey, Map<String, Object> context) {
        boolean enabled = service.isEnabled(flagKey, context);

        FeatureFlagEntity entity = repository.findByFlagKey(flagKey)
            .orElse(null);

        return Map.of(
            "flagKey", flagKey,
            "enabled", enabled,
            "exists", entity != null,
            "context", context != null ? context : Map.of()
        );
    }

    /**
     * Validate targeting rules JSON format
     *
     * @param targetingRules the targeting rules JSON
     * @throws BusinessException if invalid JSON
     */
    private void validateTargetingRules(String targetingRules) {
        if (targetingRules == null || targetingRules.trim().isEmpty()) {
            return;
        }

        try {
            // Basic JSON validation
            if (!targetingRules.trim().startsWith("{") || !targetingRules.trim().endsWith("}")) {
                throw new BusinessException(CommonErrorCode.INVALID_TARGETING_RULES);
            }
            // Additional validation could be added here (e.g., parse with Jackson)
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.INVALID_TARGETING_RULES);
        }
    }
}
