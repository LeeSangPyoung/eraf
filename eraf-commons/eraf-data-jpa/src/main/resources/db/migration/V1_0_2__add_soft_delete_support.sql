-- ERAF Commons: Soft Delete Support
-- 소프트 삭제 지원 컬럼 추가
-- Multi-DB Support: PostgreSQL, MySQL, H2

-- Add soft delete columns to common_code
ALTER TABLE common_code ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE common_code ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE common_code ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Create index for soft delete queries
CREATE INDEX IF NOT EXISTS idx_common_code_deleted ON common_code(deleted);

-- Add soft delete columns to audit_log
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Create index for soft delete queries
CREATE INDEX IF NOT EXISTS idx_audit_log_deleted ON audit_log(deleted);

-- Add comments
COMMENT ON COLUMN common_code.deleted IS '삭제 여부 (소프트 삭제)';
COMMENT ON COLUMN common_code.deleted_at IS '삭제 시각';
COMMENT ON COLUMN common_code.deleted_by IS '삭제자 ID';

COMMENT ON COLUMN audit_log.deleted IS '삭제 여부 (소프트 삭제)';
COMMENT ON COLUMN audit_log.deleted_at IS '삭제 시각';
COMMENT ON COLUMN audit_log.deleted_by IS '삭제자 ID';
