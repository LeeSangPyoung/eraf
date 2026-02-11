# ERAF Gateway - Manual Test Examples

## 사전 준비

1. SQL 실행 완료
2. JWT 토큰 획득 (아래 `<YOUR_TOKEN>`에 실제 토큰 입력)

---

## 1. Consumers API Tests

### 1.1 모든 Consumer 조회

```bash
curl http://localhost:9000/admin/consumers \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:**
```json
[
  {
    "id": 1,
    "username": "mobile-app-ios",
    "apiKey": "ck_1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p",
    "description": "iOS Mobile Application Consumer",
    "rateLimit": 1000,
    "rateLimitWindowSeconds": 60,
    "enabled": true,
    "tags": {"platform": "iOS", "app_version": "2.1.0"},
    ...
  },
  ...
]
```

---

### 1.2 새로운 Consumer 생성

```bash
curl -X POST http://localhost:9000/admin/consumers \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "my-test-consumer",
    "description": "My Test Consumer",
    "rateLimit": 100,
    "rateLimitWindowSeconds": 60,
    "enabled": true
  }'
```

**예상 결과:**
```json
{
  "id": 6,
  "username": "my-test-consumer",
  "apiKey": "ck_<자동_생성된_UUID>",
  "description": "My Test Consumer",
  "rateLimit": 100,
  "rateLimitWindowSeconds": 60,
  "enabled": true,
  ...
}
```

---

### 1.3 Consumer 상세 조회

```bash
# ID로 조회
curl http://localhost:9000/admin/consumers/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"

# Username으로 조회
curl http://localhost:9000/admin/consumers/username/mobile-app-ios \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### 1.4 Consumer 수정

```bash
curl -X PUT http://localhost:9000/admin/consumers/6 \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "my-test-consumer-updated",
    "description": "Updated Description",
    "rateLimit": 200,
    "rateLimitWindowSeconds": 60,
    "enabled": true
  }'
```

---

### 1.5 API Key 재생성

```bash
curl -X POST http://localhost:9000/admin/consumers/6/regenerate-key \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:** 새로운 API Key가 생성됨

---

### 1.6 Consumer 활성화/비활성화 토글

```bash
curl -X PATCH http://localhost:9000/admin/consumers/6/toggle \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### 1.7 Consumer 통계

```bash
curl http://localhost:9000/admin/consumers/stats \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:**
```json
{
  "totalConsumers": 6,
  "enabledConsumers": 5,
  "disabledConsumers": 1
}
```

---

### 1.8 Consumer 삭제

```bash
curl -X DELETE http://localhost:9000/admin/consumers/6 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## 2. Health Checks API Tests

### 2.1 모든 Health Check 결과 조회

```bash
curl http://localhost:9000/admin/health-checks \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:**
```json
[
  {
    "targetId": 1,
    "serviceName": "backend-service",
    "host": "localhost",
    "port": 8080,
    "healthStatus": "HEALTHY",
    "consecutiveFailures": 0,
    "lastHealthCheckAt": "2026-02-07T13:40:00",
    "responseTimeMs": null,
    "errorMessage": null,
    "enabled": true
  },
  ...
]
```

---

### 2.2 Service별 Health Check 조회

```bash
# Service ID 1의 모든 Target Health Check
curl http://localhost:9000/admin/health-checks/service/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### 2.3 특정 Target 즉시 Health Check 수행

```bash
curl -X POST http://localhost:9000/admin/health-checks/check/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:**
```json
{
  "targetId": 1,
  "serviceName": "backend-service",
  "host": "localhost",
  "port": 8080,
  "healthStatus": "HEALTHY",
  "consecutiveFailures": 0,
  "lastHealthCheckAt": "2026-02-07T13:45:23.123",
  "responseTimeMs": 45,
  "errorMessage": null,
  "enabled": true
}
```

---

### 2.4 Health Check 통계

```bash
curl http://localhost:9000/admin/health-checks/stats \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**예상 결과:**
```json
{
  "totalTargets": 2,
  "healthyTargets": 2,
  "unhealthyTargets": 0,
  "unknownTargets": 0,
  "healthyPercentage": 100.0
}
```

---

## 3. Health Check Scheduler 확인

**자동 Health Check 로그 확인:**

Health Check Scheduler는 Gateway 시작 10초 후부터 30초마다 자동 실행됩니다.

Gateway 로그에서 다음 로그를 확인:
```
2026-02-07 13:40:00.123 [scheduling-1] DEBUG c.e.o.h.HealthCheckScheduler - Scheduled health check started
2026-02-07 13:40:00.456 [scheduling-1] INFO  c.e.o.h.HealthCheckService - Performing health checks on 2 targets
2026-02-07 13:40:00.789 [scheduling-1] DEBUG c.e.o.h.HealthCheckService - Target 1 is HEALTHY
2026-02-07 13:40:01.012 [scheduling-1] DEBUG c.e.o.h.HealthCheckService - Target 2 is HEALTHY
```

**Health Status가 자동 업데이트되는지 확인:**

30초 대기 후 다시 stats 조회:
```bash
# 첫 번째 조회
curl http://localhost:9000/admin/health-checks/stats \
  -H "Authorization: Bearer <YOUR_TOKEN>"

# 35초 대기
sleep 35

# 두 번째 조회 (lastHealthCheckAt이 업데이트됨)
curl http://localhost:9000/admin/health-checks \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## 4. 통합 테스트 시나리오

### 시나리오 1: Consumer 생애주기

1. Consumer 생성
2. API Key로 인증 (향후 API Key Auth Filter 구현 시)
3. Rate Limit 초과 테스트
4. Consumer 비활성화
5. 비활성화된 Consumer 접근 차단 확인
6. Consumer 삭제

### 시나리오 2: Health Check 장애 시뮬레이션

1. Target Service 중지 (sample-biz 종료)
2. Health Check Scheduler 대기 (30초)
3. Health Status가 UNHEALTHY로 변경되는지 확인
4. consecutiveFailures 증가 확인
5. Target Service 재시작
6. Health Status가 HEALTHY로 복구되는지 확인

---

## 5. Frontend UI 테스트 (TODO)

**Consumers Page** (http://localhost:3000/gateway/consumers)
- Consumer 목록 표시
- Create/Edit Modal
- API Key 복사
- Toggle 스위치
- Delete 확인

**Health Checks Page** (http://localhost:3000/gateway/health-checks)
- Target Health 상태 실시간 표시
- Service별 필터링
- Manual Health Check 버튼
- 통계 대시보드

---

## 검증 체크리스트

✅ SQL 실행 완료
✅ gateway_consumers 테이블 생성 확인
✅ 샘플 데이터 5개 INSERT 확인
✅ Admin API 등록 확인 (gateway_apis 테이블)
✅ Consumers API CRUD 동작
✅ Health Checks API 동작
✅ Health Check Scheduler 자동 실행 (30초마다)
✅ JWT 인증 정상 작동 (ROLE_ADMIN 체크)
✅ API Validation Filter 정상 작동
