package com.eraf.core.config.feature.evaluator;

import com.eraf.core.config.feature.FeatureFlagEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simple Feature Flag Evaluator
 *
 * <p>Evaluates SIMPLE type flags - just returns the enabled status.
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Component
public class SimpleEvaluator implements FeatureFlagEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SimpleEvaluator.class);

    @Override
    public boolean supports(FeatureFlagEntity.FlagType flagType) {
        return flagType == FeatureFlagEntity.FlagType.SIMPLE;
    }

    @Override
    public boolean evaluate(FeatureFlagEntity flag, Map<String, Object> context) {
        log.trace("Evaluating SIMPLE flag '{}': enabled={}", flag.getFlagKey(), flag.getEnabled());
        return flag.getEnabled();
    }
}
