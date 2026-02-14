-- ======================================================
-- Migration: Add Advanced Features to gateway_consumers
-- Version: V1.1.0
-- Description:
--   - API Key 만료 관리 (expires_at, expiry_notification_sent)
--   - IP 화이트리스트 (allowed_ip_ranges)
--   - 사용 통계 (last_used_at, usage_count, last_used_ip)
--   - 스코프/권한 (scopes)
--   - 키 버전 관리 (key_version)
-- ======================================================

-- 1. 만료 관리 컬럼 추가
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS expiry_notification_sent BOOLEAN DEFAULT FALSE;

-- 2. IP 화이트리스트 컬럼 추가
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS allowed_ip_ranges JSONB DEFAULT '[]'::jsonb;

-- 3. 사용 통계 컬럼 추가
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS usage_count BIGINT DEFAULT 0;
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS last_used_ip VARCHAR(45);

-- 4. 스코프/권한 컬럼 추가
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS scopes JSONB DEFAULT '[]'::jsonb;

-- 5. 키 버전 관리 컬럼 추가
ALTER TABLE gateway_consumers ADD COLUMN IF NOT EXISTS key_version INTEGER DEFAULT 1;

-- 6. 인덱스 추가 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_consumer_expires_at ON gateway_consumers(expires_at);
CREATE INDEX IF NOT EXISTS idx_consumer_last_used_at ON gateway_consumers(last_used_at);
CREATE INDEX IF NOT EXISTS idx_consumer_key_version ON gateway_consumers(key_version);

-- 7. 기존 데이터 마이그레이션 (key_version NULL → 1)
UPDATE gateway_consumers SET key_version = 1 WHERE key_version IS NULL;

-- 8. 코멘트 추가
COMMENT ON COLUMN gateway_consumers.expires_at IS 'API Key 만료 일시 (NULL = 무제한)';
COMMENT ON COLUMN gateway_consumers.allowed_ip_ranges IS '허용된 IP 범위 (CIDR 표기법, 빈 배열 = 모든 IP 허용)';
COMMENT ON COLUMN gateway_consumers.last_used_at IS '마지막 API Key 사용 일시';
COMMENT ON COLUMN gateway_consumers.usage_count IS '총 API Key 사용 횟수';
COMMENT ON COLUMN gateway_consumers.scopes IS '허용된 스코프 목록 (예: ["read:api", "write:api"])';
COMMENT ON COLUMN gateway_consumers.key_version IS 'API Key 버전 (재생성 시 증가)';
