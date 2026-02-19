package com.eraf.featureflag.feature.evaluator;

import com.eraf.featureflag.feature.FeatureFlagEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Time Window Feature Flag Evaluator
 *
 * <p>Evaluates TIME_WINDOW type flags - enabled during specific time periods.
 *
 * <p>Targeting rules format:
 * <pre>
 * {
 *   "timeWindows": [
 *     {
 *       "start": "2026-02-15T00:00:00Z",
 *       "end": "2026-02-20T23:59:59Z"
 *     }
 *   ]
 * }
 * </pre>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Component
public class TimeWindowEvaluator implements FeatureFlagEvaluator {

    private static final Logger log = LoggerFactory.getLogger(TimeWindowEvaluator.class);

    private final ObjectMapper objectMapper;

    public TimeWindowEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(FeatureFlagEntity.FlagType flagType) {
        return flagType == FeatureFlagEntity.FlagType.TIME_WINDOW;
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

        @SuppressWarnings("unchecked")
        List<Map<String, String>> windows = (List<Map<String, String>>) rules.get("timeWindows");

        if (windows == null || windows.isEmpty()) {
            log.warn("No time windows specified for TIME_WINDOW flag '{}'", flag.getFlagKey());
            return false;
        }

        Instant now = Instant.now();
        log.trace("Evaluating time window for flag '{}' at {}", flag.getFlagKey(), now);

        for (Map<String, String> window : windows) {
            try {
                String startStr = window.get("start");
                String endStr = window.get("end");

                if (startStr == null || endStr == null) {
                    log.warn("Invalid time window format for flag '{}': start or end missing", flag.getFlagKey());
                    continue;
                }

                Instant start = Instant.parse(startStr);
                Instant end = Instant.parse(endStr);

                if (now.isAfter(start) && now.isBefore(end)) {
                    log.debug("Flag '{}' is within time window: {} to {}", flag.getFlagKey(), start, end);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Invalid time window format for flag '{}': {}", flag.getFlagKey(), window, e);
            }
        }

        log.trace("Flag '{}' is not within any time window at {}", flag.getFlagKey(), now);
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
