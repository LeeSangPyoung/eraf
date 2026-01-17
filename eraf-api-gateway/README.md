# ERAF API Gateway

ERAF Commons 기반의 Kong-style API Gateway 기능 모듈입니다.

> **빌드 가이드**: 전체 빌드 가이드는 [BUILD_GUIDE.md](../BUILD_GUIDE.md)를 참조하세요.

## 주요 기능

### Phase 1: Core Features
- **Rate Limiting**: IP 기반 요청 제한
- **IP Restriction**: IP 화이트리스트/블랙리스트
- **API Key Authentication**: API Key 기반 인증
- **JWT Validation**: JWT 토큰 검증
- **Circuit Breaker**: 서킷 브레이커 패턴
- **Analytics**: API 호출 분석/통계
- **Cache**: 응답 캐싱
- **Bot Detection**: 봇 탐지

### Phase 2: Advanced Features
- **OAuth2**: OAuth2 인증 서버
- **Advanced Rate Limit**: Token Bucket, Sliding Window 알고리즘 (Redis 필요)
- **Request Validation**: JSON Schema/OpenAPI 기반 검증
- **Load Balancer**: 업스트림 로드 밸런싱
- **Advanced Analytics**: 상세 메트릭, 외부 익스포트

## 모듈 구조

```
eraf-api-gateway/
├── eraf-gateway-common/              # 공통 도메인, 필터 기본 클래스
├── eraf-gateway-builder/             # 실행 가능 JAR 빌더 (Maven Profile)
├── eraf-gateway-store-memory/        # In-Memory 스토리지
├── eraf-gateway-store-jpa/           # JPA 스토리지
│
├── eraf-gateway-feature-rate-limit/       # Rate Limit 기능
├── eraf-gateway-feature-api-key/          # API Key 인증
├── eraf-gateway-feature-ip-restriction/   # IP 제한
├── eraf-gateway-feature-jwt/              # JWT 검증
├── eraf-gateway-feature-circuit-breaker/  # 서킷 브레이커
├── eraf-gateway-feature-analytics/        # 분석/통계
├── eraf-gateway-feature-cache/            # 응답 캐시
├── eraf-gateway-feature-bot-detection/    # 봇 탐지
├── eraf-gateway-feature-oauth2/           # OAuth2
└── ...
```

## 빠른 시작

### 1. 빌드

```bash
# 전체 기능 빌드
cd eraf-api-gateway/eraf-gateway-builder
mvn clean package -P full -DskipTests

# 특정 기능만 빌드
mvn clean package -P rate-limit,jwt,api-key -DskipTests
```

### 2. 실행

```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 3. 확인

```
http://localhost:8080/actuator/health
```

## 의존성 추가

### Maven

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- JPA 저장소 사용 시 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-store-jpa</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 설정

### 기본 설정 (In-Memory)

```yaml
eraf:
  gateway:
    enabled: true
    store-type: memory  # memory | jpa | redis

    rate-limit:
      enabled: true
      exclude-patterns:
        - /actuator/**
        - /health/**

    ip-restriction:
      enabled: true
      exclude-patterns:
        - /actuator/**
        - /health/**

    api-key:
      enabled: false
      header-name: X-API-Key
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /public/**
```

### JPA 저장소 설정

```yaml
eraf:
  gateway:
    store-type: jpa

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gateway
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

## 사용법

### Rate Limiting

Rate Limiting은 기본적으로 활성화되어 있습니다. 규칙을 추가하려면:

```java
@Autowired
private RateLimitService rateLimitService;

// Rate Limit 규칙 추가
RateLimitRule rule = RateLimitRule.builder()
    .name("API Rate Limit")
    .pathPattern("/api/**")
    .type(RateLimitRule.RateLimitType.IP)
    .windowSeconds(60)
    .maxRequests(100)
    .enabled(true)
    .priority(0)
    .build();

rateLimitService.createRule(rule);
```

### IP Restriction

```java
@Autowired
private IpRestrictionService ipRestrictionService;

// IP 블랙리스트 추가
ipRestrictionService.blockIp("192.168.1.100", "Blocked for abuse");

// IP 화이트리스트 추가 (특정 경로)
ipRestrictionService.allowIp("10.0.0.1", "/admin/**", "Admin access");
```

### API Key Authentication

```java
@Autowired
private ApiKeyService apiKeyService;

// API Key 생성
ApiKey apiKey = apiKeyService.createApiKey(
    "My API Key",                    // 이름
    "API key for mobile app",        // 설명
    Set.of("/api/**"),               // 허용 경로
    Set.of("192.168.1.0/24"),        // 허용 IP (CIDR)
    100,                              // Rate Limit (초당)
    LocalDateTime.now().plusYears(1)  // 만료일
);

System.out.println("API Key: " + apiKey.getApiKey());
```

## 응답 헤더

### Rate Limiting

```
X-RateLimit-Limit: 100      # 최대 요청 수
X-RateLimit-Remaining: 95   # 남은 요청 수
X-RateLimit-Reset: 45       # 리셋까지 남은 시간(초)
Retry-After: 45             # 429 응답 시
```

## 에러 응답

### Rate Limit 초과 (429)

```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded. Try again later."
  },
  "retryAfter": 45
}
```

### IP 차단 (403)

```json
{
  "success": false,
  "error": {
    "code": "IP_BLOCKED",
    "message": "Access denied from your IP address."
  }
}
```

### API Key 인증 실패 (401)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_API_KEY",
    "message": "Invalid or missing API key."
  }
}
```

## 데이터베이스 스키마

JPA 모드 사용 시 자동으로 테이블이 생성됩니다. 수동 생성이 필요한 경우 `schema.sql` 참조.

## 확장

### 커스텀 Repository 구현

```java
@Configuration
public class CustomGatewayConfig {

    @Bean
    public ApiKeyRepository apiKeyRepository() {
        return new MyCustomApiKeyRepository();
    }
}
```

### Redis 저장소 구현 (예정)

```yaml
eraf:
  gateway:
    store-type: redis

spring:
  data:
    redis:
      host: localhost
      port: 6379
```
