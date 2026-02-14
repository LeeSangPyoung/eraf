package com.eraf.core.config.feature.dto;

/**
 * Feature Flag Statistics DTO
 *
 * @author ERAF Team
 * @since Phase 2
 */
public class FeatureFlagStats {

    private long totalCount;
    private long enabledCount;
    private long disabledCount;
    private long simpleCount;
    private long userBasedCount;
    private long percentageCount;
    private long timeWindowCount;
    private long customCount;
    private long totalChecks;

    // Constructors
    public FeatureFlagStats() {
    }

    public FeatureFlagStats(long totalCount, long enabledCount, long disabledCount) {
        this.totalCount = totalCount;
        this.enabledCount = enabledCount;
        this.disabledCount = disabledCount;
    }

    // Getters and Setters
    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getEnabledCount() {
        return enabledCount;
    }

    public void setEnabledCount(long enabledCount) {
        this.enabledCount = enabledCount;
    }

    public long getDisabledCount() {
        return disabledCount;
    }

    public void setDisabledCount(long disabledCount) {
        this.disabledCount = disabledCount;
    }

    public long getSimpleCount() {
        return simpleCount;
    }

    public void setSimpleCount(long simpleCount) {
        this.simpleCount = simpleCount;
    }

    public long getUserBasedCount() {
        return userBasedCount;
    }

    public void setUserBasedCount(long userBasedCount) {
        this.userBasedCount = userBasedCount;
    }

    public long getPercentageCount() {
        return percentageCount;
    }

    public void setPercentageCount(long percentageCount) {
        this.percentageCount = percentageCount;
    }

    public long getTimeWindowCount() {
        return timeWindowCount;
    }

    public void setTimeWindowCount(long timeWindowCount) {
        this.timeWindowCount = timeWindowCount;
    }

    public long getCustomCount() {
        return customCount;
    }

    public void setCustomCount(long customCount) {
        this.customCount = customCount;
    }

    public long getTotalChecks() {
        return totalChecks;
    }

    public void setTotalChecks(long totalChecks) {
        this.totalChecks = totalChecks;
    }
}
