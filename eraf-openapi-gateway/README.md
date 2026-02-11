# 🌟 ERAF OpenAPI Gateway

> **World's Best API Gateway - ERAF's Pride**

ERAF OpenAPI Gateway는 Kong과 유사한 완벽한 기능을 갖춘 API Gateway로, Spring Cloud Gateway를 기반으로 하며 ERAF Commons의 모든 기능을 최대한 활용합니다.

## ✨ 주요 기능

### 🚀 Core Features
- **동적 라우팅**: DB 기반 실시간 라우트 설정 및 업데이트
- **로드 밸런싱**: Round Robin, Weighted Round Robin, Least Connections, IP Hash
- **Health Check**: 자동 헬스 체크 및 실패한 타겟 제외

### 🔐 Security & Authentication
- **JWT 인증**: JWT 토큰 기반 인증 및 검증
- **API Key 인증**: API Key 기반 인증
- **IP 제한**: Whitelist/Blacklist 기반 IP 접근 제어
- **Bot 감지**: User-Agent 기반 봇 감지 및 차단

### 🛡️ Reliability & Performance
- **Rate Limiting**: Redis 기반 분산 Rate Limiting
- **Circuit Breaker**: Resilience4j 기반 Circuit Breaker 패턴
- **Response Caching**: Redis 기반 응답 캐싱
- **CORS**: Cross-Origin Resource Sharing 처리

### 📊 Monitoring & Analytics
- **Analytics**: 요청/응답 분석 및 메트릭 수집
- **Logging**: 상세한 요청/응답 로깅
- **Metrics**: Prometheus 메트릭 지원
- **Actuator**: Spring Boot Actuator 통합

## 🏗️ 아키텍처

```
eraf-openapi-gateway/
├── eraf-openapi-core/          # 도메인 엔티티 및 Repository
├── eraf-openapi-admin/         # Admin REST API
├── eraf-openapi-features/      # Spring Cloud Gateway 필터
└── eraf-openapi-application/   # 실행 가능한 애플리케이션
```

## 🚀 Quick Start

### 1. Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### 2. 데이터베이스 및 Redis 실행

```bash
docker-compose up -d
```

서비스 확인:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- pgAdmin: `http://localhost:5050` (admin@eraf.com / admin123)
- Redis Commander: `http://localhost:8081`

### 3. 애플리케이션 빌드

```bash
mvn clean install -DskipTests
```

### 4. 애플리케이션 실행

```bash
cd eraf-openapi-application
mvn spring-boot:run
```

또는:

```bash
java -jar eraf-openapi-application/target/eraf-openapi-gateway.jar
```

### 5. 접속 확인

- Gateway: `http://localhost:9000`
- Admin API: `http://localhost:9000/admin`
- Actuator: `http://localhost:9000/actuator`
- Health: `http://localhost:9000/actuator/health`

## 📚 Admin API 사용법

### Service 관리

#### Service 생성
```bash
curl -X POST http://localhost:9000/admin/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-backend-service",
    "protocol": "http",
    "host": "localhost",
    "port": 8080,
    "basePath": "/api",
    "connectTimeout": 5000,
    "readTimeout": 30000,
    "writeTimeout": 30000,
    "retries": 3,
    "loadBalancingAlgorithm": "ROUND_ROBIN",
    "healthCheckEnabled": true,
    "healthCheckPath": "/actuator/health",
    "healthCheckInterval": 30000,
    "enabled": true
  }'
```

#### Service 조회
```bash
# 모든 Service 조회
curl http://localhost:9000/admin/services

# 활성화된 Service만 조회
curl http://localhost:9000/admin/services?enabledOnly=true

# ID로 조회
curl http://localhost:9000/admin/services/1
```

### Route 관리

#### Route 생성
```bash
curl -X POST http://localhost:9000/admin/routes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "api-route",
    "paths": ["/api/**"],
    "methods": ["GET", "POST"],
    "serviceId": 1,
    "priority": 0,
    "stripPath": true,
    "enabled": true
  }'
```

### Plugin 관리

#### Rate Limit Plugin 생성
```bash
curl -X POST http://localhost:9000/admin/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "rate-limit",
    "scope": "ROUTE",
    "routeId": 1,
    "config": {
      "limit": 100,
      "window_seconds": 60,
      "key_prefix": "rate_limit"
    },
    "priority": 100,
    "enabled": true
  }'
```

#### JWT Auth Plugin 생성
```bash
curl -X POST http://localhost:9000/admin/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jwt-auth",
    "scope": "GLOBAL",
    "config": {
      "secret_key": "your-secret-key",
      "exclude_paths": ["/health", "/actuator"],
      "required": true
    },
    "priority": 200,
    "enabled": true
  }'
```

#### Circuit Breaker Plugin 생성
```bash
curl -X POST http://localhost:9000/admin/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "circuit-breaker",
    "scope": "SERVICE",
    "serviceId": 1,
    "config": {
      "name": "my-service-cb",
      "failure_rate_threshold": 50,
      "slow_call_rate_threshold": 100,
      "slow_call_duration_seconds": 5,
      "minimum_number_of_calls": 10,
      "wait_duration_in_open_state_seconds": 60
    },
    "priority": 500,
    "enabled": true
  }'
```

### Target 관리

#### Target 추가 (Load Balancing)
```bash
curl -X POST http://localhost:9000/admin/targets \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 1,
    "host": "localhost",
    "port": 8081,
    "weight": 100,
    "enabled": true
  }'
```

## 🔌 지원하는 Plugin 목록

| Plugin | 설명 | Scope |
|--------|------|-------|
| `rate-limit` | Rate Limiting (Redis 기반) | GLOBAL, SERVICE, ROUTE |
| `jwt-auth` | JWT 인증 | GLOBAL, SERVICE, ROUTE |
| `api-key` | API Key 인증 | GLOBAL, SERVICE, ROUTE |
| `circuit-breaker` | Circuit Breaker 패턴 | SERVICE, ROUTE |
| `cache` | 응답 캐싱 (Redis 기반) | ROUTE |
| `analytics` | 요청/응답 분석 | GLOBAL, SERVICE, ROUTE |
| `cors` | CORS 처리 | GLOBAL |
| `ip-restriction` | IP 기반 접근 제어 | GLOBAL, SERVICE, ROUTE |
| `bot-detection` | Bot 감지 및 차단 | GLOBAL, ROUTE |

## 🔧 Configuration

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eraf_gateway
    username: eraf
    password: eraf123
```

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Gateway Configuration
```yaml
server:
  port: 9000

spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
```

## 📊 Monitoring

### Actuator Endpoints
- Health: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`
- Gateway Routes: `GET /actuator/gateway/routes`

### Logs
로그 파일 위치: `logs/eraf-openapi-gateway.log`

## 🛠️ Development

### 모듈 구조
1. **eraf-openapi-core**: JPA 엔티티, Repository
2. **eraf-openapi-admin**: Admin REST API
3. **eraf-openapi-features**: Spring Cloud Gateway 필터 구현
4. **eraf-openapi-application**: 실행 가능한 메인 애플리케이션

### ERAF Commons 활용
- `eraf-core`: 공통 유틸리티
- `eraf-data-jpa`: JPA 설정 및 Base Repository
- `eraf-data-cache`: 캐싱 추상화
- `eraf-data-redis`: Redis 통합
- `eraf-security`: 보안 기능
- `eraf-web`: Web 공통 기능
- `eraf-actuator`: 모니터링

## 📝 License

Copyright © 2024 ERAF

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

**🌟 World's Best API Gateway - ERAF's Pride 🌟**
