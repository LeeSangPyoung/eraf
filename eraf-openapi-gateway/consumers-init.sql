-- =====================================================
-- Gateway Consumers Table Creation & Sample Data
-- =====================================================
-- This script creates the gateway_consumers table and inserts sample data

-- ===== Create Table =====
CREATE TABLE IF NOT EXISTS gateway_consumers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,

    -- Rate Limit
    rate_limit INTEGER,
    rate_limit_window_seconds INTEGER DEFAULT 60,

    -- Status
    enabled BOOLEAN DEFAULT true NOT NULL,

    -- Metadata
    tags JSONB,
    custom_id VARCHAR(100),
    metadata JSONB,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- ===== Create Indexes =====
CREATE INDEX IF NOT EXISTS idx_consumer_username ON gateway_consumers(username);
CREATE INDEX IF NOT EXISTS idx_consumer_api_key ON gateway_consumers(api_key);
CREATE INDEX IF NOT EXISTS idx_consumer_enabled ON gateway_consumers(enabled);

-- ===== Sample Data =====

-- Sample Consumer 1: Mobile App
INSERT INTO gateway_consumers (username, api_key, description, rate_limit, rate_limit_window_seconds, enabled, tags, custom_id, created_at, updated_at)
VALUES (
    'mobile-app-ios',
    'ck_1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p',
    'iOS Mobile Application Consumer',
    1000,
    60,
    true,
    '{"platform": "iOS", "app_version": "2.1.0"}'::jsonb,
    'mobile-ios-001',
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Sample Consumer 2: Web Application
INSERT INTO gateway_consumers (username, api_key, description, rate_limit, rate_limit_window_seconds, enabled, tags, custom_id, created_at, updated_at)
VALUES (
    'web-app-frontend',
    'ck_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6',
    'Web Frontend Application Consumer',
    2000,
    60,
    true,
    '{"platform": "Web", "framework": "React"}'::jsonb,
    'web-frontend-001',
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Sample Consumer 3: External Partner
INSERT INTO gateway_consumers (username, api_key, description, rate_limit, rate_limit_window_seconds, enabled, tags, custom_id, created_at, updated_at)
VALUES (
    'partner-acme-corp',
    'ck_x9y8z7w6v5u4t3s2r1q0p9o8n7m6l5k4',
    'ACME Corporation Partner API Consumer',
    500,
    60,
    true,
    '{"partner": "ACME", "tier": "premium"}'::jsonb,
    'partner-acme-001',
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Sample Consumer 4: Internal Service
INSERT INTO gateway_consumers (username, api_key, description, rate_limit, rate_limit_window_seconds, enabled, tags, custom_id, created_at, updated_at)
VALUES (
    'internal-analytics',
    'ck_k4j5h6g7f8d9s0a1p2o3i4u5y6t7r8e9',
    'Internal Analytics Service Consumer',
    5000,
    60,
    true,
    '{"type": "internal", "service": "analytics"}'::jsonb,
    'internal-analytics-001',
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Sample Consumer 5: Test Consumer (Disabled)
INSERT INTO gateway_consumers (username, api_key, description, rate_limit, rate_limit_window_seconds, enabled, tags, custom_id, created_at, updated_at)
VALUES (
    'test-consumer',
    'ck_t1e2s3t4c5o6n7s8u9m0e1r2k3e4y5a6',
    'Test Consumer for Development',
    100,
    60,
    false,
    '{"environment": "test", "purpose": "development"}'::jsonb,
    'test-consumer-001',
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Verify the inserted consumers
SELECT id, username, api_key, rate_limit, enabled, tags
FROM gateway_consumers
ORDER BY id;
