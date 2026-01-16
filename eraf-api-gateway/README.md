# ERAF API Gateway

ERAF Commons 기반의 API Gateway 기능 모듈입니다.

## 주요 기능

- **Rate Limiting**: IP 기반 요청 제한
- **IP Restriction**: IP 화이트리스트/블랙리스트
- **API Key Authentication**: API Key 기반 인증

## 모듈 구조

```
eraf-api-gateway/
├── eraf-gateway-core/          # 핵심 도메인, 인터페이스
├── eraf-gateway-store-memory/  # In-Memory 구현체
├── eraf-gateway-store-jpa/     # JPA 구현체
└── eraf-gateway-starter/       # Spring Boot Auto Configuration
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
