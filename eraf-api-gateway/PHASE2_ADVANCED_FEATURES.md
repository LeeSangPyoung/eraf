# ERAF API Gateway - Phase 2: Kong-Level Advanced Features

## 🎯 Phase 2 목표

**"Kong이나 상용 API Gateway 수준의 고급 기능 추가"**

Phase 1의 빌드 타임 기능 선택 아키텍처를 기반으로, 엔터프라이즈급 고급 기능 5개를 추가했습니다.

---

## ✅ 완성된 Phase 2 고급 기능

### 1. OAuth2 인증 (eraf-gateway-feature-oauth2)

**RFC 6749 표준 준수 OAuth2 구현**

- **Authorization Code Flow**: 3-legged OAuth 지원
- **Token Management**: Access Token, Refresh Token 발급/검증/갱신
- **Token Introspection**: RFC 7662 준수
- **Multiple Grant Types**: authorization_code, refresh_token, client_credentials
- **Scope 기반 권한 관리**

**주요 클래스**:
- `OAuth2Service`: 토큰 생성, 검증, 갱신
- `OAuth2Filter`: Bearer 토큰 검증 (Order: HIGHEST + 32)
- `OAuth2Token`, `OAuth2Client`, `OAuth2AuthorizationCode`: Domain 모델

**Profile 사용**:
```bash
mvn clean package -P minimal,oauth2
```

**설정 예시**:
```yaml
eraf:
  gateway:
    oauth2:
      enabled: true
      access-token-ttl-seconds: 3600
      refresh-token-ttl-seconds: 2592000
```

---

### 2. 고급 Rate Limiting (eraf-gateway-feature-rate-limit-advanced)

**4가지 알고리즘 + Redis 분산 저장소**

#### 알고리즘

1. **Token Bucket**: Kong 기본 알고리즘, 버스트 트래픽 허용
2. **Leaky Bucket**: 일정한 속도로 요청 처리
3. **Sliding Window**: 정확한 시간 기반 제한
4. **Fixed Window**: 고성능, 간단한 구현

#### Redis 통합

- **Lua Script**: 원자적 연산 보장
- **Redis Cluster**: 수평 확장 지원
- **Key Expiration**: 자동 메모리 관리

**주요 클래스**:
- `TokenBucketRateLimiter`: 버스트 지원, 토큰 재충전
- `LeakyBucketRateLimiter`: 일정한 흐름 제어
- `RedisRateLimitRepository`: Lua 스크립트 기반 Redis 저장소

**Profile 사용**:
```bash
mvn clean package -P minimal,rate-limit-advanced
```

**설정 예시**:
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      enabled: true
      algorithm: TOKEN_BUCKET  # TOKEN_BUCKET, LEAKY_BUCKET, SLIDING_WINDOW, FIXED_WINDOW
      default-limit-per-second: 100
      default-burst-size: 200  # Token Bucket용
      storage: REDIS
      redis:
        host: localhost
        port: 6379
```

---

### 3. Request Validation (eraf-gateway-feature-validation)

**JSON Schema & OpenAPI 3.0 기반 요청 검증**

#### 검증 유형

1. **JSON Schema Validator**: Draft-07 지원
2. **OpenAPI Validator**: OpenAPI 3.0 스펙 기반
3. **Request Size Validator**: 최대 크기 제한
4. **Content-Type Validator**: MIME 타입 검증

**주요 클래스**:
- `JsonSchemaValidator`: networknt/json-schema-validator 사용
- `OpenApiValidator`: swagger-parser 통합
- `ValidationFilter`: 요청 전처리 (Order: HIGHEST + 15)

**Profile 사용**:
```bash
mvn clean package -P minimal,validation
```

**설정 예시**:
```yaml
eraf:
  gateway:
    validation:
      enabled: true
      json-schema:
        enabled: true
        schema-path: /schemas
      openapi:
        enabled: true
        spec-path: /openapi.yaml
      request-size:
        max-size-bytes: 10485760  # 10MB
```

---

### 4. Load Balancer (eraf-gateway-feature-load-balancer)

**5가지 알고리즘 + Health Check + Canary Deployment**

#### Load Balancing 알고리즘

1. **Round Robin**: 순차 분배
2. **Weighted Round Robin**: 가중치 기반 분배
3. **Least Connections**: 최소 연결 수 기반
4. **Random**: 랜덤 선택
5. **IP Hash**: Sticky Session 지원

#### Health Check

- **Active Health Check**: 주기적 Ping (HTTP/TCP)
- **Passive Health Check**: 실제 트래픽 기반 모니터링
- **Circuit Breaking**: 장애 노드 자동 격리

#### Canary Deployment

- **Percentage-Based Routing**: 트래픽 비율 조절 (e.g., 10% → new version)
- **Header-Based Routing**: 특정 헤더로 카나리 그룹 선택

**주요 클래스**:
- `LoadBalancerService`: 백엔드 선택 및 요청 전달
- `HealthChecker`: Active/Passive 헬스 체크
- `CanaryRouter`: 카나리 배포 라우팅
- `HttpProxyClient`: WebClient 기반 프록시

**Profile 사용**:
```bash
mvn clean package -P minimal,load-balancer
```

**설정 예시**:
```yaml
eraf:
  gateway:
    load-balancer:
      enabled: true
      algorithm: WEIGHTED_ROUND_ROBIN
      backends:
        - id: backend-1
          url: http://localhost:8081
          weight: 70
        - id: backend-2
          url: http://localhost:8082
          weight: 30
      health-check:
        enabled: true
        interval-seconds: 10
        timeout-seconds: 5
      canary:
        enabled: true
        percentage: 10  # 10% to canary group
```

---

### 5. Advanced Analytics (eraf-gateway-feature-analytics-advanced)

**Percentile Metrics + Exporter + Real-time Dashboard**

#### 메트릭

1. **Latency Percentiles**: p50, p75, p95, p99, p999
2. **Error Rate Metrics**: 4xx/5xx 비율
3. **Throughput Metrics**: RPS, RPM, MB/sec
4. **Top-N Metrics**: Top Consumers, APIs, Errors

#### Exporters

1. **Prometheus**: Micrometer 기반, pull 방식
2. **Datadog**: StatsD 프로토콜, push 방식
3. **Elasticsearch**: 선택적, 로그 기반 분석

#### Time Series 지원

- **Aggregation Windows**: 1min, 5min, 1hour, 1day
- **Retention Policy**: 설정 가능한 데이터 보관 기간
- **In-Memory TSDB**: 고성능 시계열 데이터베이스

**주요 클래스**:
- `LatencyPercentiles`: HdrHistogram 기반 백분위수 계산
- `PrometheusExporter`: Micrometer Registry 연동
- `DatadogExporter`: StatsD 클라이언트
- `AnalyticsDashboardController`: REST API (9 endpoints)

**Profile 사용**:
```bash
mvn clean package -P minimal,analytics-advanced
```

**설정 예시**:
```yaml
eraf:
  gateway:
    analytics-advanced:
      enabled: true
      percentiles:
        enabled: true
        values: [0.5, 0.75, 0.95, 0.99, 0.999]
      exporters:
        prometheus:
          enabled: true
          port: 9090
        datadog:
          enabled: true
          host: localhost
          port: 8125
          prefix: eraf.gateway
      time-series:
        retention-days: 30
        aggregation-intervals: [1m, 5m, 1h, 1d]
```

**Dashboard API 예시**:
```bash
# 전체 메트릭 요약
curl http://localhost:8080/analytics/summary

# Latency Percentiles
curl http://localhost:8080/analytics/latency/percentiles

# Top Consumers
curl http://localhost:8080/analytics/top/consumers?limit=10
```

---

## 🚀 Phase 2 빌드 방법

### 개별 기능 선택

```bash
# OAuth2 + Advanced Rate Limit
mvn clean package -P minimal,oauth2,rate-limit-advanced

# Load Balancer + Validation
mvn clean package -P minimal,load-balancer,validation

# Advanced Analytics만
mvn clean package -P minimal,analytics-advanced
```

### Enterprise 프리셋 (모든 기능)

```bash
mvn clean package -P enterprise
```

**포함 내용**:
- Phase 1: Rate Limit, API Key, IP Restriction, JWT, Circuit Breaker, Analytics, Cache, Bot Detection
- Phase 2: OAuth2, Advanced Rate Limit, Validation, Load Balancer, Advanced Analytics

---

## 📊 빌드 결과 비교

| 구성 | JAR 크기 (예상) | 포함 기능 |
|------|----------------|-----------|
| Minimal | ~5-8MB | Common만 |
| Phase 1 Full | ~25-30MB | Phase 1 모든 기능 |
| Enterprise | ~40-50MB | Phase 1 + Phase 2 모든 기능 |
| Custom (OAuth2 + Load Balancer) | ~15-20MB | 선택한 기능만 |

---

## 🏗️ 확장된 필터 실행 순서

Phase 2 기능이 추가된 전체 필터 체인:

```
Request
  ↓
1. Bot Detection (HIGHEST + 5)
  ↓
2. Rate Limit (HIGHEST + 10)
  ↓
3. Request Validation (HIGHEST + 15)  ← Phase 2 NEW
  ↓
4. IP Restriction (HIGHEST + 20)
  ↓
5. API Key Auth (HIGHEST + 30)
  ↓
6. OAuth2 (HIGHEST + 32)  ← Phase 2 NEW
  ↓
7. JWT Validation (HIGHEST + 35)
  ↓
8. Circuit Breaker (HIGHEST + 40)
  ↓
9. Load Balancer (HIGHEST + 45)  ← Phase 2 NEW
  ↓
10. Response Cache (HIGHEST + 50)
  ↓
  [비즈니스 로직]
  ↓
11. Advanced Analytics (LOWEST - 10)  ← Phase 2 NEW
  ↓
12. Analytics (LOWEST - 10)
  ↓
Response
```

---

## 📦 모듈 구조 (Phase 2 추가)

```
eraf-api-gateway/
│
├── eraf-gateway-common/                    # 공통 인프라
│
├── Phase 1: Core Features
│   ├── eraf-gateway-feature-rate-limit/
│   ├── eraf-gateway-feature-api-key/
│   ├── eraf-gateway-feature-ip-restriction/
│   ├── eraf-gateway-feature-jwt/
│   ├── eraf-gateway-feature-circuit-breaker/
│   ├── eraf-gateway-feature-analytics/
│   ├── eraf-gateway-feature-cache/
│   └── eraf-gateway-feature-bot-detection/
│
├── Phase 2: Advanced Features (Kong-level)
│   ├── eraf-gateway-feature-oauth2/              ← NEW
│   ├── eraf-gateway-feature-rate-limit-advanced/ ← NEW
│   ├── eraf-gateway-feature-validation/          ← NEW
│   ├── eraf-gateway-feature-load-balancer/       ← NEW
│   └── eraf-gateway-feature-analytics-advanced/  ← NEW
│
├── Storage Modules
│   ├── eraf-gateway-store-memory/
│   └── eraf-gateway-store-jpa/
│
└── eraf-gateway-builder/                   # 빌드 조합 모듈
    ├── pom.xml (Phase 2 Profiles 포함)
    └── GatewayApplication.java (Phase 2 감지 포함)
```

---

## 🎯 실전 사용 시나리오

### 시나리오 1: OAuth2 기반 외부 API Gateway

```bash
mvn clean package -P minimal,oauth2,rate-limit-advanced,validation,store-jpa
```

**포함 기능**:
- OAuth2 인증 (Authorization Code Flow)
- Token Bucket Rate Limiting (버스트 허용)
- Request Validation (JSON Schema)
- JPA Store (토큰 영속성)

**적합한 환경**: 외부 파트너 API, 공개 API

---

### 시나리오 2: MSA Load Balancer Gateway

```bash
mvn clean package -P minimal,api-key,load-balancer,circuit-breaker,analytics-advanced,store-memory
```

**포함 기능**:
- API Key 인증 (서비스 간)
- Load Balancing (Weighted Round Robin)
- Health Check (Active/Passive)
- Circuit Breaker (장애 격리)
- Advanced Analytics (Percentiles)

**적합한 환경**: 마이크로서비스 내부 Gateway, 트래픽 분산

---

### 시나리오 3: High-Performance Caching Gateway

```bash
mvn clean package -P minimal,rate-limit-advanced,cache,validation,store-memory
```

**포함 기능**:
- Advanced Rate Limit (Sliding Window)
- Response Cache (TTL)
- Request Validation (크기 제한)
- Memory Store (빠른 액세스)

**적합한 환경**: 읽기 중심 API, CDN 앞단

---

### 시나리오 4: Enterprise Full-Stack Gateway

```bash
mvn clean package -P enterprise,store-jpa
```

**포함 기능**:
- Phase 1 + Phase 2 모든 기능
- JPA Store (영속성)

**적합한 환경**: 대규모 엔터프라이즈, 모든 기능 필요

---

## 🔍 Phase 2 기능 확인 방법

### 애플리케이션 로그

```
==================================================
ERAF API Gateway Started Successfully
==================================================
Loaded Features:
--- Phase 1: Core Features ---
  ✓ Rate Limit
  ✓ API Key
  ✗ IP Restriction (not included)
  ✗ JWT (not included)
  ✗ Circuit Breaker (not included)
  ✗ Analytics (not included)
  ✗ Cache (not included)
  ✗ Bot Detection (not included)
--- Phase 2: Advanced Features ---
  ✓ OAuth2
  ✓ Advanced Rate Limit
  ✓ Request Validation
  ✓ Load Balancer
  ✗ Advanced Analytics (not included)
==================================================
```

### JAR 내용 확인

```bash
# Phase 2 클래스 확인
jar tf target/eraf-gateway-*.jar | grep "eraf/gateway/oauth2"
jar tf target/eraf-gateway-*.jar | grep "eraf/gateway/ratelimit/advanced"
jar tf target/eraf-gateway-*.jar | grep "eraf/gateway/loadbalancer"
```

---

## 📚 Phase 2 관련 문서

| 문서 | 위치 | 내용 |
|------|------|------|
| OAuth2 상세 | [eraf-gateway-feature-oauth2/README.md](eraf-gateway-feature-oauth2/README.md) | RFC 6749, 토큰 관리 |
| Advanced Rate Limit 상세 | [eraf-gateway-feature-rate-limit-advanced/README.md](eraf-gateway-feature-rate-limit-advanced/README.md) | 4가지 알고리즘, Redis |
| Validation 상세 | [eraf-gateway-feature-validation/README.md](eraf-gateway-feature-validation/README.md) | JSON Schema, OpenAPI |
| Load Balancer 상세 | [eraf-gateway-feature-load-balancer/README.md](eraf-gateway-feature-load-balancer/README.md) | 5가지 알고리즘, Health Check |
| Advanced Analytics 상세 | [eraf-gateway-feature-analytics-advanced/README.md](eraf-gateway-feature-analytics-advanced/README.md) | Percentiles, Exporters |

---

## 🆚 Phase 1 vs Phase 2 비교

| 특징 | Phase 1 | Phase 2 |
|------|---------|---------|
| 목표 | 기본 API Gateway 기능 | Kong-level 엔터프라이즈 기능 |
| Rate Limiting | 간단한 카운터 기반 | Token Bucket, Leaky Bucket, Redis |
| 인증 | API Key, JWT | OAuth2 (RFC 6749) |
| 검증 | 없음 | JSON Schema, OpenAPI 3.0 |
| 트래픽 분산 | 없음 | Load Balancer (5가지 알고리즘) |
| Analytics | 기본 카운트 | Percentiles, Prometheus, Datadog |
| Health Check | 없음 | Active/Passive Health Check |
| Canary Deployment | 없음 | Percentage-Based Routing |
| 적합한 환경 | 중소규모, 내부 API | 대규모, 엔터프라이즈, 외부 파트너 |

---

## 🎉 결론

**Phase 2 완성!**

- ✅ OAuth2 인증 (RFC 6749 준수)
- ✅ 고급 Rate Limiting (4가지 알고리즘 + Redis)
- ✅ Request Validation (JSON Schema + OpenAPI)
- ✅ Load Balancer (5가지 알고리즘 + Health Check + Canary)
- ✅ Advanced Analytics (Percentiles + Prometheus + Datadog)

**이제 ERAF API Gateway는**:
- ✅ Kong 수준의 기능을 제공합니다
- ✅ 빌드 타임에 필요한 기능만 선택할 수 있습니다
- ✅ 경량화된 JAR로 배포할 수 있습니다
- ✅ 엔터프라이즈급 프로덕션 환경에서 사용 가능합니다

```bash
# 예: OAuth2 + Load Balancer + Advanced Analytics만 선택
mvn clean package -P minimal,oauth2,load-balancer,analytics-advanced

# 결과: 필요한 기능만 포함된 최적화 JAR
# 크기: ~18-22MB (Full 50MB 대비 50% 절감)
```

🚀 **Happy Building with ERAF API Gateway!**
