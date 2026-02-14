package com.eraf.core.config.feature;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Feature Flag Entity
 *
 * <p>Production-ready feature flag system for:
 * <ul>
 *   <li>Gradual feature rollout (canary deployments)</li>
 *   <li>A/B testing with percentage-based targeting</li>
 *   <li>User/group-based feature toggles</li>
 *   <li>Time-window based activation</li>
 * </ul>
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Entity
@Table(name = "feature_flags", indexes = {
    @Index(name = "idx_feature_flags_key", columnList = "flag_key", unique = true),
    @Index(name = "idx_feature_flags_enabled", columnList = "enabled"),
    @Index(name = "idx_feature_flags_type", columnList = "flag_type"),
    @Index(name = "idx_feature_flags_created_at", columnList = "created_at")
})
public class FeatureFlagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique feature flag key (e.g., "new-ui", "experimental-api")
     */
    @Column(name = "flag_key", nullable = false, unique = true, length = 100)
    private String flagKey;

    /**
     * Human-readable name
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the feature
     */
    @Column(length = 1000)
    private String description;

    /**
     * Whether the feature is enabled
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Feature flag type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, length = 20)
    private FlagType flagType = FlagType.SIMPLE;

    /**
     * Advanced targeting rules (JSON)
     *
     * <p>Example:
     * <pre>{@code
     * {
     *   "userIds": ["user1", "user2"],
     *   "userGroups": ["beta-testers"],
     *   "percentage": 25,
     *   "timeWindows": [
     *     {"start": "2026-02-15T00:00:00Z", "end": "2026-02-20T23:59:59Z"}
     *   ]
     * }
     * }</pre>
     */
    @Column(name = "targeting_rules", columnDefinition = "TEXT")
    private String targetingRules;

    /**
     * Fallback value (SpEL expression, optional)
     */
    @Column(name = "fallback_value", columnDefinition = "TEXT")
    private String fallbackValue;

    /**
     * Tags for categorization (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String tags;

    /**
     * Additional metadata (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Statistics

    /**
     * Number of times this flag has been checked
     */
    @Column(name = "check_count")
    private Long checkCount = 0L;

    /**
     * Last time this flag was checked
     */
    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    // Audit Fields

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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

    public FlagType getFlagType() {
        return flagType;
    }

    public void setFlagType(FlagType flagType) {
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

    /**
     * Feature Flag Type
     */
    public enum FlagType {
        /**
         * Simple on/off toggle
         */
        SIMPLE,

        /**
         * Enabled for specific users or user groups
         */
        USER_BASED,

        /**
         * Gradual rollout based on percentage (e.g., 25% of users)
         */
        PERCENTAGE,

        /**
         * Enabled during specific time windows
         */
        TIME_WINDOW,

        /**
         * Custom evaluation logic
         */
        CUSTOM
    }
}
