-- =====================================================
-- Admin API Registration Script
-- =====================================================
-- This script registers all Admin API endpoints in gateway_apis table
-- with ROLE_ADMIN requirement for authentication

-- First, ensure we have a service for Admin APIs
-- If 'admin-service' doesn't exist, create it
INSERT INTO gateway_services (name, description, protocol, host, port, base_path, enabled, retries, created_at, updated_at)
VALUES (
    'admin-service',
    'Internal Admin API Service',
    'http',
    'localhost',
    9000,
    '/admin',
    true,
    3,
    NOW(),
    NOW()
)
ON CONFLICT (name) DO NOTHING;

-- Get the service_id for admin-service
DO $$
DECLARE
    v_service_id BIGINT;
BEGIN
    SELECT id INTO v_service_id FROM gateway_services WHERE name = 'admin-service';

    -- ===== Admin Root API =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('admin-root', '/admin', 'GET', v_service_id, 'Admin API 정보 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        service_id = EXCLUDED.service_id,
        description = EXCLUDED.description,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Dashboard APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('dashboard-overview', '/admin/dashboard/overview', 'GET', v_service_id, 'Dashboard 개요 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('dashboard-traffic', '/admin/dashboard/traffic', 'GET', v_service_id, 'Traffic 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('dashboard-top-apis', '/admin/dashboard/top-apis', 'GET', v_service_id, '최다 호출 API', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('dashboard-error-prone-apis', '/admin/dashboard/error-prone-apis', 'GET', v_service_id, '에러 다발 API', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('dashboard-api-stats', '/admin/dashboard/api-stats', 'GET', v_service_id, 'API 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Routes APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('routes-list', '/admin/routes', 'GET', v_service_id, 'Route 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-create', '/admin/routes', 'POST', v_service_id, 'Route 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-get', '/admin/routes/*', 'GET', v_service_id, 'Route 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-update', '/admin/routes/*', 'PUT', v_service_id, 'Route 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-delete', '/admin/routes/*', 'DELETE', v_service_id, 'Route 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-toggle', '/admin/routes/*/toggle', 'PATCH', v_service_id, 'Route 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('routes-by-service', '/admin/routes/service/*', 'GET', v_service_id, 'Service별 Route 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Services APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('services-list', '/admin/services', 'GET', v_service_id, 'Service 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-create', '/admin/services', 'POST', v_service_id, 'Service 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-get', '/admin/services/*', 'GET', v_service_id, 'Service 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-update', '/admin/services/*', 'PUT', v_service_id, 'Service 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-delete', '/admin/services/*', 'DELETE', v_service_id, 'Service 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-toggle', '/admin/services/*/toggle', 'PATCH', v_service_id, 'Service 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('services-by-name', '/admin/services/name/*', 'GET', v_service_id, '이름으로 Service 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Targets APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('targets-list', '/admin/targets', 'GET', v_service_id, 'Target 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-create', '/admin/targets', 'POST', v_service_id, 'Target 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-get', '/admin/targets/*', 'GET', v_service_id, 'Target 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-update', '/admin/targets/*', 'PUT', v_service_id, 'Target 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-delete', '/admin/targets/*', 'DELETE', v_service_id, 'Target 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-toggle', '/admin/targets/*/toggle', 'PATCH', v_service_id, 'Target 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('targets-by-service', '/admin/targets/service/*', 'GET', v_service_id, 'Service별 Target 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Plugins APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('plugins-list', '/admin/plugins', 'GET', v_service_id, 'Plugin 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-create', '/admin/plugins', 'POST', v_service_id, 'Plugin 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-get', '/admin/plugins/*', 'GET', v_service_id, 'Plugin 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-update', '/admin/plugins/*', 'PUT', v_service_id, 'Plugin 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-delete', '/admin/plugins/*', 'DELETE', v_service_id, 'Plugin 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-toggle', '/admin/plugins/*/toggle', 'PATCH', v_service_id, 'Plugin 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-by-route', '/admin/plugins/route/*', 'GET', v_service_id, 'Route별 Plugin 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-by-service', '/admin/plugins/service/*', 'GET', v_service_id, 'Service별 Plugin 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-global', '/admin/plugins/global', 'GET', v_service_id, 'Global Plugin 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('plugins-by-name', '/admin/plugins/name/*', 'GET', v_service_id, '이름으로 Plugin 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== APIs Management APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('apis-list', '/admin/apis', 'GET', v_service_id, 'API 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-create', '/admin/apis', 'POST', v_service_id, 'API 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-get', '/admin/apis/*', 'GET', v_service_id, 'API 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-update', '/admin/apis/*', 'PUT', v_service_id, 'API 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-delete', '/admin/apis/*', 'DELETE', v_service_id, 'API 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-toggle', '/admin/apis/*/toggle', 'PATCH', v_service_id, 'API 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-lookup', '/admin/apis/lookup', 'GET', v_service_id, 'API 조회 (경로/메서드)', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-stats', '/admin/apis/stats', 'GET', v_service_id, 'API 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-by-service', '/admin/apis/service/*', 'GET', v_service_id, 'Service별 API 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('apis-by-route', '/admin/apis/route/*', 'GET', v_service_id, 'Route별 API 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Request Logs APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('request-logs-list', '/admin/request-logs', 'GET', v_service_id, 'Request Log 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('request-logs-clear', '/admin/request-logs/clear', 'DELETE', v_service_id, 'Request Log 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('request-logs-stats', '/admin/request-logs/stats', 'GET', v_service_id, 'Request Log 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Consumers APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('consumers-list', '/admin/consumers', 'GET', v_service_id, 'Consumer 목록 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-create', '/admin/consumers', 'POST', v_service_id, 'Consumer 생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-get', '/admin/consumers/*', 'GET', v_service_id, 'Consumer 상세 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-update', '/admin/consumers/*', 'PUT', v_service_id, 'Consumer 수정', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-delete', '/admin/consumers/*', 'DELETE', v_service_id, 'Consumer 삭제', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-toggle', '/admin/consumers/*/toggle', 'PATCH', v_service_id, 'Consumer 활성화 토글', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-regenerate-key', '/admin/consumers/*/regenerate-key', 'POST', v_service_id, 'API Key 재생성', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-stats', '/admin/consumers/stats', 'GET', v_service_id, 'Consumer 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('consumers-by-username', '/admin/consumers/username/*', 'GET', v_service_id, 'Username으로 Consumer 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

    -- ===== Health Checks APIs =====
    INSERT INTO gateway_apis (name, path, method, service_id, description, auth_required, auth_type, required_roles, validation_enabled, enabled, priority, created_at, updated_at)
    VALUES
    ('health-checks-list', '/admin/health-checks', 'GET', v_service_id, 'Health Check 결과 조회', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('health-checks-by-service', '/admin/health-checks/service/*', 'GET', v_service_id, 'Service별 Health Check', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('health-checks-perform', '/admin/health-checks/check/*', 'POST', v_service_id, 'Health Check 즉시 수행', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW()),
    ('health-checks-stats', '/admin/health-checks/stats', 'GET', v_service_id, 'Health Check 통계', true, 'JWT', '["ROLE_ADMIN"]'::jsonb, false, true, 100, NOW(), NOW())
    ON CONFLICT (name) DO UPDATE SET
        path = EXCLUDED.path,
        method = EXCLUDED.method,
        required_roles = EXCLUDED.required_roles,
        updated_at = NOW();

END $$;

-- Verify the inserted Admin APIs
SELECT name, path, method, required_roles, enabled
FROM gateway_apis
WHERE path LIKE '/admin/%'
ORDER BY path, method;
