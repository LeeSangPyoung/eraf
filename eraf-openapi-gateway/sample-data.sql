-- ERAF OpenAPI Gateway Sample Data
-- This script provides sample data to get started quickly

-- ============================================================
-- 1. Sample Services
-- ============================================================

-- Backend Service
INSERT INTO gateway_services (name, protocol, host, port, base_path, connect_timeout, read_timeout, write_timeout, retries, load_balancing_algorithm, health_check_enabled, health_check_path, health_check_interval, health_check_timeout, healthy_threshold, unhealthy_threshold, description, enabled, created_at, updated_at)
VALUES ('backend-service', 'http', 'localhost', 8080, '/api', 5000, 30000, 30000, 3, 'ROUND_ROBIN', true, '/actuator/health', 30000, 5000, 2, 3, 'Main backend service', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- External API Service
INSERT INTO gateway_services (name, protocol, host, port, base_path, connect_timeout, read_timeout, write_timeout, retries, load_balancing_algorithm, health_check_enabled, health_check_path, health_check_interval, health_check_timeout, healthy_threshold, unhealthy_threshold, description, enabled, created_at, updated_at)
VALUES ('external-api', 'https', 'api.example.com', 443, '', 5000, 30000, 30000, 3, 'ROUND_ROBIN', true, '/health', 60000, 10000, 2, 3, 'External API service', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 2. Sample Targets (Load Balancing)
-- ============================================================

-- Backend Service Targets
INSERT INTO gateway_targets (service_id, host, port, weight, health_status, consecutive_failures, description, enabled, created_at, updated_at)
VALUES (1, 'localhost', 8080, 100, 'HEALTHY', 0, 'Backend instance 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO gateway_targets (service_id, host, port, weight, health_status, consecutive_failures, description, enabled, created_at, updated_at)
VALUES (1, 'localhost', 8081, 100, 'HEALTHY', 0, 'Backend instance 2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 3. Sample Routes
-- ============================================================

-- API Routes
INSERT INTO gateway_routes (name, paths, methods, hosts, service_id, priority, strip_path, path_prefix, description, enabled, created_at, updated_at)
VALUES ('api-routes', '["/api/**"]'::jsonb, '["GET", "POST", "PUT", "DELETE"]'::jsonb, null, 1, 0, true, null, 'Main API routes', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Public Routes (no auth)
INSERT INTO gateway_routes (name, paths, methods, hosts, service_id, priority, strip_path, path_prefix, description, enabled, created_at, updated_at)
VALUES ('public-routes', '["/public/**"]'::jsonb, '["GET"]'::jsonb, null, 1, 10, true, null, 'Public routes (no authentication)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- External API Routes
INSERT INTO gateway_routes (name, paths, methods, hosts, service_id, priority, strip_path, path_prefix, description, enabled, created_at, updated_at)
VALUES ('external-routes', '["/external/**"]'::jsonb, '["GET", "POST"]'::jsonb, null, 2, 0, true, null, 'External API routes', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 4. Global Plugins
-- ============================================================

-- CORS Plugin (Global)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('cors', 'GLOBAL', null, null, '{
  "allowed_origins": ["*"],
  "allowed_methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
  "allowed_headers": ["*"],
  "exposed_headers": ["X-Request-Id"],
  "allow_credentials": true,
  "max_age_seconds": 3600
}'::jsonb, 0, 'Global CORS configuration', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Analytics Plugin (Global)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('analytics', 'GLOBAL', null, null, '{
  "collect_headers": false,
  "collect_body": false
}'::jsonb, 1000, 'Global request/response analytics', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Bot Detection Plugin (Global)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('bot-detection', 'GLOBAL', null, null, '{
  "action": "block",
  "allow_good_bots": true,
  "custom_patterns": []
}'::jsonb, 150, 'Global bot detection', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 5. Service-Level Plugins
-- ============================================================

-- Rate Limit Plugin (Backend Service)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('rate-limit', 'SERVICE', null, 1, '{
  "limit": 100,
  "window_seconds": 60,
  "key_prefix": "rate_limit_backend"
}'::jsonb, 100, 'Rate limiting for backend service', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Circuit Breaker Plugin (Backend Service)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('circuit-breaker', 'SERVICE', null, 1, '{
  "name": "backend-service-cb",
  "failure_rate_threshold": 50,
  "slow_call_rate_threshold": 100,
  "slow_call_duration_seconds": 5,
  "minimum_number_of_calls": 10,
  "wait_duration_in_open_state_seconds": 60
}'::jsonb, 500, 'Circuit breaker for backend service', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 6. Route-Level Plugins
-- ============================================================

-- JWT Auth Plugin (API Routes only)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('jwt-auth', 'ROUTE', 1, null, '{
  "secret_key": "your-secret-key-change-in-production",
  "exclude_paths": ["/api/login", "/api/register"],
  "required": true
}'::jsonb, 200, 'JWT authentication for API routes', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cache Plugin (Public Routes)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('cache', 'ROUTE', 2, null, '{
  "ttl_seconds": 300,
  "methods": ["GET"],
  "exclude_paths": []
}'::jsonb, 300, 'Response caching for public routes', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- IP Restriction Plugin (External Routes)
INSERT INTO gateway_plugins (name, scope, route_id, service_id, config, priority, description, enabled, created_at, updated_at)
VALUES ('ip-restriction', 'ROUTE', 3, null, '{
  "policy": "whitelist",
  "ip_list": ["127.0.0.1", "192.168.*.*"]
}'::jsonb, 50, 'IP whitelist for external routes', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- Sample Redis Data (API Keys)
-- Run these commands in redis-cli or Redis Commander
-- ============================================================

-- SETEX api_key:test-api-key-123 86400 "client-id-123"
-- SETEX api_key:test-api-key-456 86400 "client-id-456"
