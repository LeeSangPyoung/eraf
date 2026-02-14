package com.eraf.core.config.feature.evaluator;

import com.eraf.core.config.feature.FeatureFlagEntity;

import java.util.Map;

/**
 * Feature Flag Evaluator Interface
 *
 * <p>Strategy interface for evaluating feature flags based on their type.
 * Implementations provide specific evaluation logic for different flag types.
 *
 * @author ERAF Team
 * @since Phase 3
 */
public interface FeatureFlagEvaluator {

    /**
     * Check if this evaluator supports the given flag type
     *
     * @param flagType the flag type
     * @return true if this evaluator can handle the flag type
     */
    boolean supports(FeatureFlagEntity.FlagType flagType);

    /**
     * Evaluate the feature flag with given context
     *
     * @param flag the feature flag entity
     * @param context evaluation context (userId, userGroups, etc.)
     * @return true if flag should be enabled for this context
     */
    boolean evaluate(FeatureFlagEntity flag, Map<String, Object> context);
}
