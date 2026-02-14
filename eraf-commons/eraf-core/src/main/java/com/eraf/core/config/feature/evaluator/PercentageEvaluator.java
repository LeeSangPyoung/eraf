package com.eraf.core.config.feature.evaluator;

import com.eraf.core.config.feature.FeatureFlagEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Percentage-Based Feature Flag Evaluator
 *
 * <p>Evaluates PERCENTAGE type flags - gradual rollout to a percentage of users.
 * Uses consistent hashing to ensure the same user always gets the same result.
 *
 * <p>Targeting rules format:
 * <pre>
 * {
 *   "percentage": 25
 * }
 * </pre>
 *
 * <p>Context format:
 * <pre>
 * {
 *   "userId": "user123"
 * }
 * </pre>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Component
public class PercentageEvaluator implements FeatureFlagEvaluator {

    private static final Logger log = LoggerFactory.getLogger(PercentageEvaluator.class);

    private final ObjectMapper objectMapper;

    public PercentageEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(FeatureFlagEntity.FlagType flagType) {
        return flagType == FeatureFlagEntity.FlagType.PERCENTAGE;
    }

    @Override
    public boolean evaluate(FeatureFlagEntity flag, Map<String, Object> context) {
        // If flag is disabled, return false immediately
        if (!flag.getEnabled()) {
            log.trace("Flag '{}' is disabled", flag.getFlagKey());
            return false;
        }

        // Parse targeting rules
        Map<String, Object> rules = parseTargetingRules(flag.getTargetingRules());
        if (rules == null || rules.isEmpty()) {
            log.trace("No targeting rules for flag '{}'", flag.getFlagKey());
            return flag.getEnabled();
        }

        Number percentageNum = (Number) rules.get("percentage");
        if (percentageNum == null) {
            log.warn("No percentage specified for PERCENTAGE flag '{}'", flag.getFlagKey());
            return false;
        }

        int percentage = percentageNum.intValue();
        if (percentage <= 0) {
            log.trace("Percentage is 0 for flag '{}'", flag.getFlagKey());
            return false;
        }
        if (percentage >= 100) {
            log.trace("Percentage is 100 for flag '{}'", flag.getFlagKey());
            return true;
        }

        // Get userId from context (or use "anonymous" as default)
        String userId = context != null ? (String) context.getOrDefault("userId", "anonymous") : "anonymous";

        // Consistent hash: same user always gets same result
        int hash = Math.abs((flag.getFlagKey() + ":" + userId).hashCode());
        int bucket = hash % 100;

        boolean result = bucket < percentage;
        log.trace("Percentage evaluation for flag '{}', user '{}': hash={}, bucket={}, percentage={}, result={}",
            flag.getFlagKey(), userId, hash, bucket, percentage, result);

        return result;
    }

    /**
     * Parse targeting rules JSON
     */
    private Map<String, Object> parseTargetingRules(String rulesJson) {
        if (rulesJson == null || rulesJson.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(rulesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse targeting rules: {}", rulesJson, e);
            return null;
        }
    }
}
