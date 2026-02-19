# ERAF Gateway

Spring Cloud Gateway 기반의 API Gateway 모듈로, 라우팅, 인증, Rate Limiting, 서비스 디스커버리 통합 기능을 제공합니다.

## Features

- ✅ **동적 라우팅**: YAML 설정 기반 라우트 구성
- ✅ **JWT 인증**: Spring Security 통합 JWT 토큰 검증
- ✅ **Rate Limiting**: Token Bucket 알고리즘 기반 요청 제한
- ✅ **서비스 디스커버리**: Eureka, Consul 등과 통합
- ✅ **Auto-Configuration**: Spring Boot 자동 구성 지원
- ✅ **반응형 프로그래밍**: Spring WebFlux 기반

## Installation

### Maven

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 선택적 의존성

**서비스 디스커버리 (Eureka):**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Rate Limiting (Redis):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

## Configuration

### 기본 설정

```yaml
eraf:
  gateway:
    enabled: true
    routes:
      - id: user-service
        uri: http://localhost:8081
        predicates:
          - Path=/api/users/**
        filters:
          - name: StripPrefix
            args: "1"
        order: 1

      - id: order-service
        uri: http://localhost:8082
        predicates:
          - Path=/api/orders/**
        filters:
          - name: RewritePath
            args: "/api/orders/(?<segment>.*), /$\\{segment}"
        order: 2
```

### Rate Limiting 설정

```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      replenish-rate: 10        # 초당 토큰 재충전 수
      burst-capacity: 20        # 최대 버스트 용량
      requested-tokens: 1s      # 토큰 요청 간격
```

**Token Bucket 알고리즘 설명:**
- `replenishRate`: 초당 생성되는 토큰 수 (지속 가능한 요청 속도)
- `burstCapacity`: 버킷의 최대 토큰 수 (순간 트래픽 허용량)
- 예: `replenishRate=10, burstCapacity=20`인 경우
  - 평균 초당 10개 요청 허용
  - 순간적으로 최대 20개까지 허용 (버킷에 토큰이 쌓인 경우)

### 서비스 디스커버리 통합

```yaml
eraf:
  gateway:
    discovery-enabled: true
    discovery-server-url: http://localhost:8761/eureka
    routes:
      - id: user-service
        uri: lb://USER-SERVICE    # 로드 밸런싱 URI
        predicates:
          - Path=/api/users/**

eureka:
  client:
    service-url:
      defaultZone: ${eraf.gateway.discovery-server-url}
```

### JWT 인증 설정

게이트웨이는 `eraf-security` 모듈과 통합되어 JWT 인증을 지원합니다.

```yaml
eraf:
  security:
    jwt:
      secret: your-secret-key
      expiration: 86400000  # 24시간

  gateway:
    routes:
      - id: secure-service
        uri: http://localhost:8083
        predicates:
          - Path=/api/secure/**
        filters:
          - name: Authentication  # JWT 검증 필터 자동 적용
```

**보안 경고:**
- JWT secret은 환경 변수나 Vault를 통해 관리하세요
- 예: `secret: ${JWT_SECRET}` 또는 `secret: '{cipher}AQA...'`

## Usage

### 1. Spring Boot 애플리케이션에서 사용

```java
@SpringBootApplication
@EnableErafGateway  // 선택적: 명시적 활성화
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### 2. 프로그래밍 방식 라우트 구성

```java
@Configuration
public class CustomGatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("custom-route", r -> r
                .path("/api/custom/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Custom-Header", "Value"))
                .uri("http://localhost:8084"))
            .build();
    }
}
```

### 3. 커스텀 필터 추가

```java
@Component
public class CustomGatewayFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 요청 전처리
        log.info("Custom filter: {}", request.getPath());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 응답 후처리
            ServerHttpResponse response = exchange.getResponse();
            log.info("Response status: {}", response.getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return -1;  // 필터 실행 순서
    }
}
```

## Built-in Filters

### 1. AuthenticationGatewayFilter

JWT 토큰 검증 필터 (자동 적용)

```yaml
eraf:
  gateway:
    routes:
      - id: protected-route
        uri: http://localhost:8080
        predicates:
          - Path=/api/protected/**
        # Authentication 필터는 eraf.security.jwt.enabled=true일 때 자동 적용
```

요청 헤더 예시:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. RateLimitGatewayFilter

Token Bucket 알고리즘 기반 Rate Limiting 필터

```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      replenish-rate: 100      # 초당 100개 요청
      burst-capacity: 200      # 최대 200개 버스트
```

응답 헤더:
- `X-RateLimit-Remaining`: 남은 토큰 수
- `X-RateLimit-Replenish-Rate`: 토큰 재충전 속도
- `X-RateLimit-Burst-Capacity`: 버킷 용량

Rate limit 초과 시: `429 Too Many Requests`

## Advanced Configuration

### CORS 설정

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
            allowed-headers: "*"
            max-age: 3600
```

### Actuator 엔드포인트

```yaml
management:
  endpoints:
    web:
      exposure:
        include: gateway, health, metrics
```

게이트웨이 라우트 조회:
```bash
curl http://localhost:8080/actuator/gateway/routes
```

라우트 새로고침:
```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### 타임아웃 설정

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000    # 연결 타임아웃 (ms)
        response-timeout: 10s    # 응답 타임아웃
```

## Performance Tuning

### Netty 설정

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: ELASTIC
          max-connections: 1000
          max-idle-time: 30s
```

### 메모리 최적화

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        compression: true
      httpserver:
        max-initial-line-length: 4096
```

## Monitoring

### Prometheus Metrics

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

**주요 메트릭:**
- `spring.cloud.gateway.requests` - 요청 수
- `gateway.requests.duration` - 요청 처리 시간
- `rate.limit.rejected` - Rate limit 거부된 요청 수

### Logging

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.eraf.gateway: DEBUG
```

## Troubleshooting

### 1. 라우트가 작동하지 않음

**확인 사항:**
- `eraf.gateway.enabled=true` 설정 확인
- 라우트 predicates와 URI가 올바른지 확인
- Actuator `/gateway/routes` 엔드포인트로 라우트 목록 확인

### 2. JWT 인증 실패

**확인 사항:**
- `eraf-security` 모듈이 classpath에 있는지 확인
- JWT secret이 올바르게 설정되었는지 확인
- 토큰 만료 시간 확인

### 3. Rate Limiting이 작동하지 않음

**확인 사항:**
- `eraf.gateway.rate-limit.enabled=true` 설정 확인
- Redis 연결 확인 (Redis 기반 사용 시)
- 응답 헤더 `X-RateLimit-*` 확인

### 4. 서비스 디스커버리 연결 실패

**확인 사항:**
- Eureka/Consul 서버가 실행 중인지 확인
- `discovery-server-url`이 올바른지 확인
- 네트워크 연결 확인

## Examples

### Example 1: 마이크로서비스 게이트웨이

```yaml
eraf:
  gateway:
    enabled: true
    discovery-enabled: true
    discovery-server-url: http://eureka:8761/eureka

    rate-limit:
      enabled: true
      replenish-rate: 100
      burst-capacity: 200

    routes:
      # User Service
      - id: user-service
        uri: lb://USER-SERVICE
        predicates:
          - Path=/api/users/**
        order: 1

      # Order Service
      - id: order-service
        uri: lb://ORDER-SERVICE
        predicates:
          - Path=/api/orders/**
        order: 2

      # Payment Service (보안 강화)
      - id: payment-service
        uri: lb://PAYMENT-SERVICE
        predicates:
          - Path=/api/payments/**
        filters:
          - name: Authentication  # JWT 검증
        order: 3
```

### Example 2: 레거시 시스템 통합

```yaml
eraf:
  gateway:
    routes:
      # 새로운 API (v2)
      - id: new-api
        uri: http://new-api-server:8080
        predicates:
          - Path=/api/v2/**
        filters:
          - name: RewritePath
            args: "/api/v2/(?<segment>.*), /$\\{segment}"
        order: 1

      # 레거시 API (v1)
      - id: legacy-api
        uri: http://legacy-server:8080
        predicates:
          - Path=/api/v1/**
        filters:
          - name: RewritePath
            args: "/api/v1/(?<segment>.*), /legacy/$\\{segment}"
          - name: AddRequestHeader
            args: "X-Legacy-Version, 1.0"
        order: 2
```

## Architecture

```
┌─────────────────┐
│   API Gateway   │
│  (eraf-gateway) │
└────────┬────────┘
         │
    ┌────┴─────┬──────────┬──────────┐
    │          │          │          │
┌───▼───┐  ┌──▼───┐  ┌───▼───┐  ┌──▼────┐
│ User  │  │Order │  │Payment│  │Notify │
│Service│  │Service│ │Service│  │Service│
└───────┘  └──────┘  └───────┘  └───────┘
```

## License

Copyright © 2026 ERAF Platform. All rights reserved.
