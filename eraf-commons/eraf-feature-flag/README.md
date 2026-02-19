# ERAF Feature Flag

Enterprise Feature Toggle System with A/B Testing, Canary Release, and User-based Targeting.

## 개요

ERAF Feature Flag는 프로덕션 환경에서 기능을 동적으로 제어할 수 있는 Feature Toggle 시스템입니다.

### 주요 기능

- **@FeatureToggle 어노테이션** - 메서드 레벨 기능 제어
- **다양한 평가 전략**:
  - Simple: ON/OFF 단순 제어
  - Percentage: 카나리/A/B 테스트 (0-100%)
  - TimeWindow: 시간 기반 활성화
  - UserBased: 사용자 타게팅
- **2-Level 캐싱** - Local Cache + Redis (optional)
- **관리 API** - Feature Flag CRUD REST API
- **JPA 영속화** - Database 저장 (optional)

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-feature-flag</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
eraf:
  feature-flag:
    enabled: true
    cache:
      local-ttl: 60s          # L1 캐시 TTL
      redis-enabled: false    # L2 Redis 캐시 (optional)
    admin-api-enabled: true   # 관리 API 활성화
```

### 2. 기본 사용법

#### Simple Feature Toggle

```java
@Service
public class OrderService {

    @FeatureToggle(name = "new-checkout", fallbackMethod = "oldCheckout")
    public OrderResult checkout(Order order) {
        // 새로운 체크아웃 로직
        return newCheckoutLogic(order);
    }

    private OrderResult oldCheckout(Order order) {
        // 기존 체크아웃 로직 (fallback)
        return legacyCheckoutLogic(order);
    }
}
```

#### Programmatic Check

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final FeatureFlagService featureFlagService;

    public void processPayment(Payment payment) {
        if (featureFlagService.isEnabled("new-payment-gateway")) {
            newPaymentGateway.process(payment);
        } else {
            legacyPaymentGateway.process(payment);
        }
    }
}
```

### 3. 고급 사용법

#### Percentage-based Rollout (Canary Release)

```java
// 10%의 사용자에게만 활성화
@FeatureToggle(
    name = "recommendation-engine-v2",
    strategy = "PERCENTAGE",
    percentage = 10
)
public List<Product> getRecommendations(String userId) {
    return newRecommendationEngine.recommend(userId);
}
```

#### User-based Targeting

```java
// 특정 사용자에게만 활성화
@FeatureToggle(
    name = "beta-dashboard",
    strategy = "USER_BASED",
    targetUsers = {"user-123", "user-456"}
)
public Dashboard getBetaDashboard() {
    return newDashboard();
}
```

#### Time Window

```java
// 특정 시간대에만 활성화
@FeatureToggle(
    name = "flash-sale",
    strategy = "TIME_WINDOW",
    startTime = "2024-12-25T00:00:00",
    endTime = "2024-12-25T23:59:59"
)
public List<Product> getFlashSaleProducts() {
    return flashSaleRepository.findAll();
}
```

## 관리 API

Feature Flag 관리를 위한 REST API가 자동으로 제공됩니다.

### 전체 Feature Flag 조회

```bash
GET /api/feature-flags

Response:
[
  {
    "name": "new-checkout",
    "enabled": true,
    "strategy": "SIMPLE",
    "description": "New checkout flow",
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

### Feature Flag 생성

```bash
POST /api/feature-flags
Content-Type: application/json

{
  "name": "dark-mode",
  "enabled": false,
  "strategy": "PERCENTAGE",
  "percentage": 0,
  "description": "Dark mode feature"
}
```

### Feature Flag 수정

```bash
PUT /api/feature-flags/{name}
Content-Type: application/json

{
  "enabled": true,
  "percentage": 50
}
```

### Feature Flag 삭제

```bash
DELETE /api/feature-flags/{name}
```

### 통계 조회

```bash
GET /api/feature-flags/stats

Response:
{
  "total": 15,
  "enabled": 8,
  "disabled": 7,
  "byStrategy": {
    "SIMPLE": 10,
    "PERCENTAGE": 3,
    "USER_BASED": 2
  }
}
```

## 설정 옵션

```yaml
eraf:
  feature-flag:
    enabled: true                     # Feature Flag 시스템 활성화

    cache:
      local-ttl: 60s                  # L1 로컬 캐시 TTL
      redis-enabled: false            # L2 Redis 캐시 활성화
      redis-ttl: 300s                 # L2 Redis 캐시 TTL

    admin-api-enabled: true           # 관리 API 활성화
    admin-api-path: /api/feature-flags # 관리 API 경로

    default-loader-enabled: true      # 시작 시 기본 Feature Flag 로드
    default-flags:                    # 기본 Feature Flag 정의
      - name: example-flag
        enabled: false
        strategy: SIMPLE
        description: Example feature flag
```

## 평가 전략 (Evaluator)

### 1. SIMPLE

단순 ON/OFF 제어:
```java
@FeatureToggle(name = "new-feature", strategy = "SIMPLE")
```

### 2. PERCENTAGE

카나리 배포, A/B 테스트:
```java
@FeatureToggle(
    name = "new-algorithm",
    strategy = "PERCENTAGE",
    percentage = 25  // 25% 사용자에게 활성화
)
```

### 3. USER_BASED

특정 사용자 타게팅:
```java
@FeatureToggle(
    name = "premium-feature",
    strategy = "USER_BASED",
    targetUsers = {"premium-user-1", "premium-user-2"}
)
```

### 4. TIME_WINDOW

시간 기반 활성화:
```java
@FeatureToggle(
    name = "black-friday-sale",
    strategy = "TIME_WINDOW",
    startTime = "2024-11-29T00:00:00",
    endTime = "2024-11-29T23:59:59"
)
```

## 캐싱 전략

### L1: Local Cache (기본)
- In-memory 캐시
- TTL 기반 자동 갱신
- 서버별 독립적

### L2: Redis Cache (선택)
- 분산 환경 캐시 공유
- 실시간 변경사항 반영
- 설정 필요: `eraf.feature-flag.cache.redis-enabled=true`

## 데이터베이스 영속화

JPA를 사용하여 Feature Flag를 데이터베이스에 저장할 수 있습니다:

```xml
<!-- JPA 의존성 추가 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
```

## 마이그레이션 가이드 (eraf-core v1.0 → v1.1)

### Before (v1.0)

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- Feature Flag 자동 포함 -->
```

### After (v1.1)

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- Feature Flag 사용 시 추가 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-feature-flag</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Import 변경

```java
// Before
import com.eraf.core.config.FeatureToggle;
import com.eraf.core.config.Feature;
import com.eraf.core.config.feature.FeatureFlagService;

// After
import com.eraf.featureflag.FeatureToggle;
import com.eraf.featureflag.Feature;
import com.eraf.featureflag.feature.FeatureFlagService;
```

## 라이선스

Copyright 2024 ERAF Platform
