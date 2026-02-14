-- ERAF Commons: Multi-Tenancy Support
-- 멀티테넌시 지원 컬럼 추가
-- Multi-DB Support: PostgreSQL, MySQL, H2

-- Add tenant_id column to common_code
ALTER TABLE common_code ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_common_code_tenant_id ON common_code(tenant_id);

-- Add tenant_id column to audit_log
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant_id ON audit_log(tenant_id);

-- Add comments
COMMENT ON COLUMN common_code.tenant_id IS '테넌트 ID (멀티테넌시)';
COMMENT ON COLUMN audit_log.tenant_id IS '테넌트 ID (멀티테넌시)';
