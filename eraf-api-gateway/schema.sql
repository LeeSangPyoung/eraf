-- ERAF API Gateway Database Schema
-- MySQL / MariaDB

-- API Key 테이블
CREATE TABLE IF NOT EXISTS gateway_api_key (
    id VARCHAR(36) PRIMARY KEY,
    api_key VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    rate_limit_per_second INT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_api_key (api_key),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- API Key 허용 경로 테이블
CREATE TABLE IF NOT EXISTS gateway_api_key_paths (
    api_key_id VARCHAR(36) NOT NULL,
    path_pattern VARCHAR(200) NOT NULL,
    PRIMARY KEY (api_key_id, path_pattern),
    FOREIGN KEY (api_key_id) REFERENCES gateway_api_key(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- API Key 허용 IP 테이블
CREATE TABLE IF NOT EXISTS gateway_api_key_ips (
    api_key_id VARCHAR(36) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    PRIMARY KEY (api_key_id, ip_address),
    FOREIGN KEY (api_key_id) REFERENCES gateway_api_key(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IP 제한 테이블
CREATE TABLE IF NOT EXISTS gateway_ip_restriction (
    id VARCHAR(36) PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    path_pattern VARCHAR(200),
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ip_address (ip_address),
    INDEX idx_type (type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rate Limit 규칙 테이블
CREATE TABLE IF NOT EXISTS gateway_rate_limit_rule (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    path_pattern VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    window_seconds INT NOT NULL,
    max_requests INT NOT NULL,
    burst_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    burst_max_requests INT DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_enabled (enabled),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 샘플 데이터 (선택적)
-- 기본 Rate Limit 규칙
INSERT INTO gateway_rate_limit_rule (id, name, path_pattern, type, window_seconds, max_requests, enabled, priority)
VALUES
    (UUID(), 'Default API Rate Limit', '/api/**', 'IP', 60, 100, TRUE, 100),
    (UUID(), 'Login Rate Limit', '/api/auth/login', 'IP', 60, 10, TRUE, 10);
