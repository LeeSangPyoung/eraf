package com.eraf.core.config.feature.dto;

import com.eraf.core.config.feature.FeatureFlagEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Feature Flag Request DTO
 *
 * @author ERAF Team
 * @since Phase 2
 */
public class FeatureFlagRequest {

    @NotBlank(message = "Flag key is required")
    @Size(max = 100, message = "Flag key must not exceed 100 characters")
    private String flagKey;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;

    @NotNull(message = "Flag type is required")
    private FeatureFlagEntity.FlagType flagType;

    private String targetingRules;

    private String fallbackValue;

    private String tags;

    private String metadata;

    // Constructors
    public FeatureFlagRequest() {
    }

    public FeatureFlagRequest(String flagKey, String name, String description, Boolean enabled,
                              FeatureFlagEntity.FlagType flagType) {
        this.flagKey = flagKey;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.flagType = flagType;
    }

    // Getters and Setters
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
}
