-- Feature Flags Table
CREATE TABLE feature_flags (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Identity
    flag_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    -- Status
    enabled BOOLEAN NOT NULL DEFAULT true,

    -- Type
    flag_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE',
    -- SIMPLE: Basic on/off toggle
    -- USER_BASED: Enabled for specific users or groups
    -- PERCENTAGE: Gradual rollout (e.g., 25% of users)
    -- TIME_WINDOW: Enabled during specific time periods
    -- CUSTOM: Custom evaluation logic

    -- Advanced Targeting (JSONB for PostgreSQL, JSON for MySQL/H2)
    targeting_rules TEXT,
    -- JSON structure:
    -- {
    --   "userIds": ["user1", "user2"],
    --   "userGroups": ["beta-testers", "internal-staff"],
    --   "percentage": 25,
    --   "timeWindows": [
    --     {"start": "2026-02-15T00:00:00Z", "end": "2026-02-20T23:59:59Z"}
    --   ],
    --   "attributes": {"country": ["US", "CA"], "role": ["ADMIN"]}
    -- }

    -- Fallback Value (SpEL expression, optional)
    fallback_value TEXT,

    -- Metadata (JSON)
    tags TEXT,
    metadata TEXT,

    -- Statistics (usage tracking)
    check_count BIGINT DEFAULT 0,
    last_checked_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),

    -- Constraints
    CONSTRAINT chk_flag_type CHECK (flag_type IN ('SIMPLE', 'USER_BASED', 'PERCENTAGE', 'TIME_WINDOW', 'CUSTOM'))
);

-- Indexes for performance
CREATE INDEX idx_feature_flags_key ON feature_flags(flag_key);
CREATE INDEX idx_feature_flags_enabled ON feature_flags(enabled);
CREATE INDEX idx_feature_flags_type ON feature_flags(flag_type);
CREATE INDEX idx_feature_flags_created_at ON feature_flags(created_at);

-- Comments
COMMENT ON TABLE feature_flags IS 'Production-ready feature flag system for gradual rollouts, A/B testing, and feature toggling';
COMMENT ON COLUMN feature_flags.flag_key IS 'Unique identifier for the feature flag (e.g., new-ui, experimental-api)';
COMMENT ON COLUMN feature_flags.flag_type IS 'Type of feature flag: SIMPLE (on/off), USER_BASED (specific users), PERCENTAGE (gradual rollout), TIME_WINDOW (time-based)';
COMMENT ON COLUMN feature_flags.targeting_rules IS 'JSON rules for advanced targeting (userIds, percentage, time windows, etc.)';
COMMENT ON COLUMN feature_flags.check_count IS 'Number of times this flag has been checked (for statistics)';
