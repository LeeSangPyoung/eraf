#!/bin/bash

GATEWAY_ADMIN="http://localhost:9000/admin"

echo "📋 Registering sample-biz service to OpenAPI Gateway..."

# 1. Create Service
echo "1️⃣ Creating service..."
SERVICE_RESPONSE=$(curl -s -X POST "${GATEWAY_ADMIN}/services" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "sample-biz",
    "description": "ERAF Sample Business Service",
    "protocol": "http",
    "host": "localhost",
    "port": 8080,
    "enabled": true,
    "connectTimeout": 5000,
    "readTimeout": 30000,
    "writeTimeout": 30000,
    "retries": 3,
    "loadBalancingAlgorithm": "ROUND_ROBIN",
    "healthCheckEnabled": true,
    "healthCheckPath": "/actuator/health",
    "healthCheckInterval": 30000,
    "healthCheckTimeout": 5000,
    "healthyThreshold": 2,
    "unhealthyThreshold": 3
  }')

SERVICE_ID=$(echo $SERVICE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ Service created with ID: $SERVICE_ID"

# 2. Create Target
echo "2️⃣ Creating target..."
curl -s -X POST "${GATEWAY_ADMIN}/targets" \
  -H "Content-Type: application/json" \
  -d "{
    \"serviceId\": $SERVICE_ID,
    \"host\": \"localhost\",
    \"port\": 8080,
    \"weight\": 100,
    \"enabled\": true
  }"
echo ""
echo "✅ Target created"

# 3. Create Routes
echo "3️⃣ Creating routes..."

# Auth API Route
curl -s -X POST "${GATEWAY_ADMIN}/routes" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"auth-api\",
    \"path\": \"/api/auth/**\",
    \"serviceId\": $SERVICE_ID,
    \"enabled\": true,
    \"stripPathPrefix\": false,
    \"priority\": 100,
    \"description\": \"Authentication API route\"
  }"
echo "  ✅ auth-api route created"

# Document API Route
curl -s -X POST "${GATEWAY_ADMIN}/routes" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"document-api\",
    \"path\": \"/api/document/**\",
    \"serviceId\": $SERVICE_ID,
    \"enabled\": true,
    \"stripPathPrefix\": false,
    \"priority\": 90,
    \"description\": \"Document API route\"
  }"
echo "  ✅ document-api route created"

# Cache API Route
curl -s -X POST "${GATEWAY_ADMIN}/routes" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"cache-api\",
    \"path\": \"/api/cache/**\",
    \"serviceId\": $SERVICE_ID,
    \"enabled\": true,
    \"stripPathPrefix\": false,
    \"priority\": 80,
    \"description\": \"Cache API route\"
  }"
echo "  ✅ cache-api route created"

# Security API Route
curl -s -X POST "${GATEWAY_ADMIN}/routes" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"security-api\",
    \"path\": \"/api/security/**\",
    \"serviceId\": $SERVICE_ID,
    \"enabled\": true,
    \"stripPathPrefix\": false,
    \"priority\": 70,
    \"description\": \"Security API route\"
  }"
echo "  ✅ security-api route created"

# Monitoring API Route
curl -s -X POST "${GATEWAY_ADMIN}/routes" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"monitoring-api\",
    \"path\": \"/api/monitoring/**\",
    \"serviceId\": $SERVICE_ID,
    \"enabled\": true,
    \"stripPathPrefix\": false,
    \"priority\": 60,
    \"description\": \"Monitoring API route\"
  }"
echo "  ✅ monitoring-api route created"

echo ""
echo "🎉 Sample-biz service registration completed!"
echo "📊 Service ID: $SERVICE_ID"
echo "🔗 Test URL: http://localhost:9000/api/auth/login"
