package com.eraf.featureflag.feature.dto;

import com.eraf.featureflag.feature.FeatureFlagEntity;

import java.time.Instant;

/**
 * Feature Flag Response DTO
 *
 * @author ERAF Team
 * @since Phase 2
 */
public class FeatureFlagResponse {

    private Long id;
    private String flagKey;
    private String name;
    private String description;
    private Boolean enabled;
    private FeatureFlagEntity.FlagType flagType;
    private String targetingRules;
    private String fallbackValue;
    private String tags;
    private String metadata;
    private Long checkCount;
    private Instant lastCheckedAt;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    // Constructors
    public FeatureFlagResponse() {
    }

    public FeatureFlagResponse(Long id, String flagKey, String name, Boolean enabled,
                               FeatureFlagEntity.FlagType flagType) {
        this.id = id;
        this.flagKey = flagKey;
        this.name = name;
        this.enabled = enabled;
        this.flagType = flagType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public void setFlagKey(String flagKey) {
        this.flagKey = flagKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public FeatureFlagEntity.FlagType getFlagType() {
        return flagType;
    }

    public void setFlagType(FeatureFlagEntity.FlagType flagType) {
        this.flagType = flagType;
    }

    public String getTargetingRules() {
        return targetingRules;
    }

    public void setTargetingRules(String targetingRules) {
        this.targetingRules = targetingRules;
    }

    public String getFallbackValue() {
        return fallbackValue;
    }

    public void setFallbackValue(String fallbackValue) {
        this.fallbackValue = fallbackValue;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(Long checkCount) {
        this.checkCount = checkCount;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
