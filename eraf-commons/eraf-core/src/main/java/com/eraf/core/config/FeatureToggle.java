package com.eraf.core.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 기능 토글 관리
 */
public class FeatureToggle {

    private final Map<String, Boolean> features = new ConcurrentHashMap<>();

    /**
     * 기능 활성화 여부 확인
     */
    public boolean isEnabled(String featureName) {
        return features.getOrDefault(featureName, false);
    }

    /**
     * 기능 활성화
     */
    public void enable(String featureName) {
        features.put(featureName, true);
    }

    /**
     * 기능 비활성화
     */
    public void disable(String featureName) {
        features.put(featureName, false);
    }

    /**
     * 기능 토글
     */
    public void toggle(String featureName) {
        features.compute(featureName, (k, v) -> v == null || !v);
    }

    /**
     * 기능 설정
     */
    public void set(String featureName, boolean enabled) {
        features.put(featureName, enabled);
    }

    /**
     * 모든 기능 설정 조회
     */
    public Map<String, Boolean> getAll() {
        return Map.copyOf(features);
    }

    /**
     * 모든 기능 초기화
     */
    public void clear() {
        features.clear();
    }
}
