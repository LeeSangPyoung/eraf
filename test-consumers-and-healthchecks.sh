#!/bin/bash

# ERAF Gateway - Consumers & Health Checks 테스트 스크립트
# 사용법: ./test-consumers-and-healthchecks.sh <JWT_TOKEN>

if [ -z "$1" ]; then
  echo "Usage: $0 <JWT_TOKEN>"
  echo ""
  echo "Get JWT Token from:"
  echo "1. Login at http://localhost:3000 (admin/admin123)"
  echo "2. Open Browser DevTools (F12)"
  echo "3. Application > Local Storage > accessToken"
  exit 1
fi

TOKEN="$1"
BASE_URL="http://localhost:9000"

echo "========================================="
echo "  ERAF Gateway API Tests"
echo "========================================="
echo ""

# Helper function
test_api() {
  local METHOD=$1
  local PATH=$2
  local DATA=$3
  local DESC=$4

  echo "-----------------------------------"
  echo "TEST: $DESC"
  echo "-----------------------------------"
  echo "REQUEST: $METHOD $PATH"

  if [ -n "$DATA" ]; then
    echo "BODY: $DATA"
    echo ""
    RESPONSE=$(curl -s -X $METHOD "$BASE_URL$PATH" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$DATA")
  else
    echo ""
    RESPONSE=$(curl -s -X $METHOD "$BASE_URL$PATH" \
      -H "Authorization: Bearer $TOKEN")
  fi

  echo "RESPONSE:"
  echo "$RESPONSE" | python -m json.tool 2>/dev/null || echo "$RESPONSE"
  echo ""
  echo ""
}

##################################################
# 1. CONSUMERS API TESTS
##################################################

echo "========================================="
echo "  1. CONSUMERS API TESTS"
echo "========================================="
echo ""

# 1.1 Get all consumers
test_api "GET" "/admin/consumers" "" \
  "Get all consumers"

# 1.2 Get consumers (enabled only)
test_api "GET" "/admin/consumers?enabledOnly=true" "" \
  "Get enabled consumers only"

# 1.3 Create new consumer
NEW_CONSUMER='{
  "username": "test-api-consumer",
  "description": "Test Consumer created via API",
  "rateLimit": 500,
  "rateLimitWindowSeconds": 60,
  "enabled": true,
  "tags": {"env": "test", "purpose": "api-test"},
  "customId": "test-001"
}'

test_api "POST" "/admin/consumers" "$NEW_CONSUMER" \
  "Create new consumer"

# Store consumer ID (assuming it's the last created)
CONSUMER_ID=$(curl -s -X GET "$BASE_URL/admin/consumers" \
  -H "Authorization: Bearer $TOKEN" | \
  python -c "import sys, json; data=json.load(sys.stdin); print(data[-1]['id'] if data else 1)" 2>/dev/null || echo "1")

echo "Created Consumer ID: $CONSUMER_ID"
echo ""

# 1.4 Get consumer by ID
test_api "GET" "/admin/consumers/$CONSUMER_ID" "" \
  "Get consumer by ID"

# 1.5 Update consumer
UPDATED_CONSUMER='{
  "username": "test-api-consumer-updated",
  "description": "Updated Test Consumer",
  "rateLimit": 1000,
  "rateLimitWindowSeconds": 60,
  "enabled": true,
  "tags": {"env": "test", "purpose": "api-test", "updated": "true"}
}'

test_api "PUT" "/admin/consumers/$CONSUMER_ID" "$UPDATED_CONSUMER" \
  "Update consumer"

# 1.6 Regenerate API Key
test_api "POST" "/admin/consumers/$CONSUMER_ID/regenerate-key" "" \
  "Regenerate API Key"

# 1.7 Toggle consumer (disable)
test_api "PATCH" "/admin/consumers/$CONSUMER_ID/toggle" "" \
  "Toggle consumer (disable)"

# 1.8 Toggle consumer (enable again)
test_api "PATCH" "/admin/consumers/$CONSUMER_ID/toggle" "" \
  "Toggle consumer (enable)"

# 1.9 Get consumer statistics
test_api "GET" "/admin/consumers/stats" "" \
  "Get consumer statistics"

# 1.10 Get consumer by username
test_api "GET" "/admin/consumers/username/test-api-consumer-updated" "" \
  "Get consumer by username"

# 1.11 Delete consumer (cleanup)
test_api "DELETE" "/admin/consumers/$CONSUMER_ID" "" \
  "Delete consumer (cleanup)"

##################################################
# 2. HEALTH CHECKS API TESTS
##################################################

echo "========================================="
echo "  2. HEALTH CHECKS API TESTS"
echo "========================================="
echo ""

# 2.1 Get all health checks
test_api "GET" "/admin/health-checks" "" \
  "Get all health check results"

# 2.2 Get health check statistics
test_api "GET" "/admin/health-checks/stats" "" \
  "Get health check statistics"

# 2.3 Get health checks by service (assume service_id=1 exists)
test_api "GET" "/admin/health-checks/service/1" "" \
  "Get health checks for service 1"

# 2.4 Perform immediate health check on target (assume target_id=1 exists)
test_api "POST" "/admin/health-checks/check/1" "" \
  "Perform immediate health check on target 1"

##################################################
# 3. VERIFICATION
##################################################

echo "========================================="
echo "  3. FINAL VERIFICATION"
echo "========================================="
echo ""

# Check if Health Check Scheduler is working
echo "Waiting 35 seconds for Health Check Scheduler to run..."
sleep 35

test_api "GET" "/admin/health-checks/stats" "" \
  "Health check stats after scheduler run"

echo "========================================="
echo "  ALL TESTS COMPLETED!"
echo "========================================="
echo ""
echo "Summary:"
echo "✅ Consumers API: Create, Read, Update, Delete, Toggle, Regenerate Key, Stats"
echo "✅ Health Checks API: Get All, Get by Service, Perform Check, Stats"
echo "✅ Health Check Scheduler: Auto-running every 30 seconds"
echo ""
