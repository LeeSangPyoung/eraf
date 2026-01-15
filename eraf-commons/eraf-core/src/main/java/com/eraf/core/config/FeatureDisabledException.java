package com.eraf.core.config;

import com.eraf.core.exception.BusinessException;
import com.eraf.core.exception.CommonErrorCode;

/**
 * 기능 비활성화 예외
 */
public class FeatureDisabledException extends BusinessException {

    private final String featureName;

    public FeatureDisabledException(String featureName) {
        super(CommonErrorCode.FEATURE_DISABLED, featureName);
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
