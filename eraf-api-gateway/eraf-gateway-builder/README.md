# ERAF Gateway Builder

빌드 시 원하는 기능만 선택하여 단일 JAR로 생성하는 API Gateway Builder 모듈입니다.

## 🎯 핵심 개념

**"체크박스처럼 기능을 선택하면, 선택된 기능만 포함된 경량화 JAR가 생성됩니다"**

## 🔧 빌드 명령어

### 1. 전체 기능 (기본값)

```bash
mvn clean package
# 또는
mvn clean package -P full
```

**포함 기능**: Rate Limit, API Key, IP Restriction, JWT, Circuit Breaker, Analytics, Cache, Bot Detection + Memory Store

### 2. 최소 구성

```bash
mvn clean package -P minimal
```

**포함 기능**: Common + Memory Store만 (필터 없음)

### 3. 개별 기능 선택 조합

```bash
# Rate Limit + API Key만
mvn clean package -P minimal,rate-limit,api-key

# Rate Limit + IP Restriction + JWT
mvn clean package -P minimal,rate-limit,ip-restriction,jwt

# Rate Limit + Circuit Breaker + Cache (성능 최적화)
mvn clean package -P minimal,rate-limit,circuit-breaker,cache
```

### 4. 프리셋 조합

#### 보안 중심 (Security)
```bash
mvn clean package -P security
```
**포함**: Rate Limit, API Key, IP Restriction, JWT, Bot Detection

#### 성능 중심 (Performance)
```bash
mvn clean package -P performance
```
**포함**: Rate Limit, Circuit Breaker, Cache

### 5. Storage 선택

```bash
# JPA Store 사용 (기본은 Memory)
mvn clean package -P full,store-jpa

# Memory Store 명시적 사용
mvn clean package -P minimal,rate-limit,store-memory
```

## 📦 사용 가능한 Profile

### 개별 기능 Profile

#### Phase 1: Core Features

| Profile | 기능 | 설명 |
|---------|------|------|
| `rate-limit` | Rate Limiting | IP/API Key/User 기반 요청 제한 |
| `api-key` | API Key 인증 | API Key 기반 인증 및 관리 |
| `ip-restriction` | IP 제한 | IP 화이트리스트/블랙리스트, CIDR 지원 |
| `jwt` | JWT 검증 | JWT 토큰 검증 (JJWT 사용) |
| `circuit-breaker` | Circuit Breaker | 장애 전파 방지 |
| `analytics` | Analytics | API 호출 메트릭 수집 |
| `cache` | Response Cache | 응답 캐싱 (TTL 기반) |
| `bot-detection` | Bot Detection | User-Agent 기반 봇 탐지 |

#### Phase 2: Advanced Features (Kong-level)

| Profile | 기능 | 설명 |
|---------|------|------|
| `oauth2` | OAuth2 | RFC 6749 준수, 토큰 발급/검증/갱신 |
| `rate-limit-advanced` | 고급 Rate Limiting | Token Bucket, Leaky Bucket, Redis 기반 |
| `validation` | Request Validation | JSON Schema, OpenAPI 3.0 검증 |
| `load-balancer` | Load Balancing | Health Check, Canary Deployment |
| `analytics-advanced` | 고급 Analytics | Percentile, Prometheus, Datadog |

### Storage Profile

| Profile | 설명 |
|---------|------|
| `store-memory` | In-Memory 저장소 (기본값) |
| `store-jpa` | JPA/Database 저장소 |

### 프리셋 Profile

| Profile | 포함 기능 |
|---------|-----------|
| `full` | 모든 Phase 1 기능 (기본값) |
| `minimal` | Common + Storage만 |
| `security` | Rate Limit + API Key + IP Restriction + JWT + Bot Detection |
| `performance` | Rate Limit + Circuit Breaker + Cache |
| `enterprise` | 모든 Phase 1 + Phase 2 기능 (Kong-level) |

## 🚀 빌드 결과물

빌드 후 생성되는 JAR:

```
target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### JAR 크기 비교 (예상)

| 구성 | 예상 크기 | 포함 기능 |
|------|----------|-----------|
| Full | ~25-30MB | 모든 기능 + 모든 의존성 |
| Minimal | ~5-8MB | Common만 |
| Security | ~15-20MB | 보안 관련 기능 |
| Performance | ~12-18MB | 성능 관련 기능 |
| Custom | 가변 | 선택한 기능만 |

## 🎯 실행 방법

```bash
# JAR 실행
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar

# 프로파일 지정
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod

# 포트 변경
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar --server.port=9090
```

## 📝 설정 파일

`src/main/resources/application.yml`에서 각 기능을 설정할 수 있습니다.

```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      default-limit-per-second: 100

    api-key:
      enabled: true
      header-name: X-API-Key

    jwt:
      enabled: true
      secret-key: your-secret-key
```

## 🔍 기능 확인

애플리케이션 시작 시 로그에서 포함된 기능을 확인할 수 있습니다:

```
==================================================
ERAF API Gateway Started Successfully
==================================================
Loaded Features:
  ✓ Rate Limit
  ✓ API Key
  ✗ IP Restriction (not included)
  ✓ JWT
  ✗ Circuit Breaker (not included)
  ✗ Analytics (not included)
  ✗ Cache (not included)
  ✗ Bot Detection (not included)
==================================================
```

## 💡 사용 예시

### 예시 1: 마이크로서비스별 최적화

#### 공개 API Gateway
```bash
mvn clean package -P minimal,rate-limit,bot-detection,cache
```
- 불필요한 인증 기능 제외
- Rate Limit으로 남용 방지
- Bot Detection으로 악의적 봇 차단
- Cache로 응답 속도 향상

#### 내부 API Gateway
```bash
mvn clean package -P minimal,api-key,circuit-breaker,analytics
```
- API Key로 내부 서비스 인증
- Circuit Breaker로 장애 전파 방지
- Analytics로 API 사용 현황 추적

#### 외부 파트너 API Gateway
```bash
mvn clean package -P security
```
- 모든 보안 기능 활성화
- Rate Limit, API Key, IP Restriction, JWT, Bot Detection

### 예시 2: 환경별 배포

#### 개발 환경
```bash
mvn clean package -P minimal,rate-limit,analytics,store-memory
```
- 빠른 재시작을 위한 Memory Store
- 기본 Rate Limit + Analytics

#### 프로덕션 환경
```bash
mvn clean package -P full,store-jpa
```
- 모든 기능 활성화
- 영속성을 위한 JPA Store

### 예시 3: Phase 2 고급 기능 사용

#### OAuth2 + Advanced Rate Limiting
```bash
mvn clean package -P minimal,oauth2,rate-limit-advanced,store-memory
```
- OAuth2 인증
- Token Bucket 알고리즘 기반 Rate Limiting

#### Load Balancer + Health Check
```bash
mvn clean package -P minimal,load-balancer,validation,store-memory
```
- 로드 밸런싱 (5가지 알고리즘)
- Active/Passive Health Check
- Request Validation

#### Enterprise 전체 기능
```bash
mvn clean package -P enterprise,store-jpa
```
- Phase 1 + Phase 2 모든 기능
- Kong-level 엔터프라이즈 API Gateway

## 🏗️ 아키텍처

```
eraf-gateway-builder (조합 모듈)
  │
  ├── eraf-gateway-common (항상 포함)
  │     └── 공통 인프라, 베이스 클래스
  │
  ├── Feature Modules (Profile로 선택)
  │     ├── eraf-gateway-feature-rate-limit
  │     ├── eraf-gateway-feature-api-key
  │     ├── eraf-gateway-feature-ip-restriction
  │     ├── eraf-gateway-feature-jwt
  │     ├── eraf-gateway-feature-circuit-breaker
  │     ├── eraf-gateway-feature-analytics
  │     ├── eraf-gateway-feature-cache
  │     └── eraf-gateway-feature-bot-detection
  │
  └── Storage Module (Profile로 선택)
        ├── eraf-gateway-store-memory (기본)
        └── eraf-gateway-store-jpa (선택)
```

## 🎨 커스터마이징

### 새로운 프리셋 Profile 추가

`pom.xml`에 새로운 profile을 추가할 수 있습니다:

```xml
<profile>
    <id>my-custom</id>
    <dependencies>
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-gateway-feature-rate-limit</artifactId>
        </dependency>
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-gateway-feature-jwt</artifactId>
        </dependency>
        <!-- 원하는 기능들 추가 -->
    </dependencies>
</profile>
```

사용:
```bash
mvn clean package -P my-custom
```

## 📊 빌드 시간 비교

| 구성 | 빌드 시간 (예상) |
|------|------------------|
| Full | ~2-3분 |
| Minimal | ~30초-1분 |
| Custom (3-4 features) | ~1-2분 |

## ⚠️ 주의사항

1. **최소한 하나의 Storage는 필수**: `store-memory` 또는 `store-jpa` 중 하나는 반드시 포함되어야 합니다.
2. **Profile 중복**: 같은 기능을 여러 번 선택해도 한 번만 포함됩니다.
3. **의존성 자동 해결**: 선택한 기능이 의존하는 common 모듈은 자동으로 포함됩니다.
4. **JWT 사용 시**: JWT profile을 선택하면 JJWT 라이브러리가 자동으로 포함됩니다.

## 🐛 트러블슈팅

### ClassNotFoundException 발생 시
- 필요한 기능의 Profile이 빌드 시 포함되었는지 확인
- `mvn dependency:tree`로 의존성 확인

### 기능이 활성화되지 않을 때
- `application.yml`에서 해당 기능의 `enabled: true` 확인
- 애플리케이션 로그에서 "Loaded Features" 확인

## 📚 더 보기

- [REFACTORING_PLAN.md](../REFACTORING_PLAN.md) - 전체 리팩토링 계획
- 각 기능 모듈의 README.md - 개별 기능 상세 문서
