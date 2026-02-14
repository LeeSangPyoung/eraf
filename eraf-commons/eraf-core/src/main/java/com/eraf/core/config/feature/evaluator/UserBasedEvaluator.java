package com.eraf.core.config.feature.evaluator;

import com.eraf.core.config.feature.FeatureFlagEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * User-Based Feature Flag Evaluator
 *
 * <p>Evaluates USER_BASED type flags - enabled for specific users or user groups.
 *
 * <p>Targeting rules format:
 * <pre>
 * {
 *   "userIds": ["user1", "user2"],
 *   "userGroups": ["beta-testers", "internal-staff"]
 * }
 * </pre>
 *
 * <p>Context format:
 * <pre>
 * {
 *   "userId": "user1",
 *   "userGroups": ["beta-testers", "premium"]
 * }
 * </pre>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Component
public class UserBasedEvaluator implements FeatureFlagEvaluator {

    private static final Logger log = LoggerFactory.getLogger(UserBasedEvaluator.class);

    private final ObjectMapper objectMapper;

    public UserBasedEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(FeatureFlagEntity.FlagType flagType) {
        return flagType == FeatureFlagEntity.FlagType.USER_BASED;
    }

    @Override
    public boolean evaluate(FeatureFlagEntity flag, Map<String, Object> context) {
        // If flag is disabled, return false immediately
        if (!flag.getEnabled()) {
            log.trace("Flag '{}' is disabled", flag.getFlagKey());
            return false;
        }

        // Need context for user-based evaluation
        if (context == null || context.isEmpty()) {
            log.trace("No context provided for user-based flag '{}'", flag.getFlagKey());
            return false;
        }

        // Parse targeting rules
        Map<String, Object> rules = parseTargetingRules(flag.getTargetingRules());
        if (rules == null || rules.isEmpty()) {
            log.trace("No targeting rules for flag '{}'", flag.getFlagKey());
            return flag.getEnabled();
        }

        String userId = (String) context.get("userId");
        if (userId == null) {
            log.trace("No userId in context for flag '{}'", flag.getFlagKey());
            return false;
        }

        // Check user IDs
        @SuppressWarnings("unchecked")
        List<String> allowedUserIds = (List<String>) rules.get("userIds");
        if (allowedUserIds != null && allowedUserIds.contains(userId)) {
            log.debug("User '{}' matched for flag '{}' via userIds", userId, flag.getFlagKey());
            return true;
        }

        // Check user groups
        @SuppressWarnings("unchecked")
        List<String> allowedGroups = (List<String>) rules.get("userGroups");
        @SuppressWarnings("unchecked")
        List<String> userGroups = (List<String>) context.get("userGroups");

        if (allowedGroups != null && userGroups != null) {
            for (String group : userGroups) {
                if (allowedGroups.contains(group)) {
                    log.debug("User '{}' matched for flag '{}' via group '{}'",
                        userId, flag.getFlagKey(), group);
                    return true;
                }
            }
        }

        log.trace("User '{}' did not match for flag '{}'", userId, flag.getFlagKey());
        return false;
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
