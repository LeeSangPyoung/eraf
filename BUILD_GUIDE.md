# ERAF Build Guide

Maven Profile 기반 빌드 시스템을 사용하여 필요한 기능만 선택적으로 빌드할 수 있습니다.

---

## 1. eraf-commons 빌드

### 1.1 사전 요구사항

```bash
# eraf-commons 전체 모듈 먼저 설치 (최초 1회)
cd eraf-commons
mvn clean install -DskipTests
```

### 1.2 빌드 위치

```bash
cd eraf-commons/eraf-commons-aggregator
```

### 1.3 사용 가능한 프로파일

| Profile | 설명 |
|---------|------|
| `minimal` | Core만 포함 (기본값) |
| `full` | 모든 Starter 포함 |
| `web` | Web Starter |
| `session` | Session Starter |
| `redis` | Redis Starter |
| `actuator` | Actuator Starter |
| `security` | Security Starter |
| `jpa` | JPA Starter |
| `mybatis` | MyBatis Starter |
| `cache` | Cache Starter |
| `kafka` | Kafka Starter |
| `batch` | Batch Starter |
| `service-client` | Service Client Starter |
| `messaging` | Messaging Starter |
| `rabbitmq` | RabbitMQ Starter |
| `s3` | AWS S3 Starter |
| `elasticsearch` | Elasticsearch Starter |
| `ftp` | FTP Starter |
| `tcp` | TCP Starter |
| `notification` | Notification Starter |
| `scheduler` | Scheduler Starter |
| `statemachine` | State Machine Starter |
| `database` | Database Starter |
| `swagger` | Swagger Starter |

### 1.4 빌드 예시

```bash
# 1. Core만 빌드 (기본)
mvn clean package -P minimal -DskipTests

# 2. 전체 Starter 빌드
mvn clean package -P full -DskipTests

# 3. 단일 Starter 빌드
mvn clean package -P web -DskipTests
mvn clean package -P ftp -DskipTests
mvn clean package -P redis -DskipTests

# 4. 여러 Starter 조합 빌드
mvn clean package -P web,security,jpa -DskipTests
mvn clean package -P redis,cache -DskipTests
mvn clean package -P kafka,rabbitmq,messaging -DskipTests

# 5. 웹 애플리케이션용 조합
mvn clean package -P web,security,jpa,cache,actuator,swagger -DskipTests

# 6. 배치 처리용 조합
mvn clean package -P batch,jpa,scheduler -DskipTests

# 7. 메시징 시스템용 조합
mvn clean package -P kafka,rabbitmq,messaging,redis -DskipTests
```

### 1.5 빌드 결과

```
eraf-commons/eraf-commons-aggregator/target/eraf-commons-1.0.0-SNAPSHOT.jar
```

---

## 2. eraf-api-gateway 빌드

### 2.1 사전 요구사항

```bash
# 1. eraf-commons 먼저 설치
cd eraf-commons
mvn clean install -DskipTests

# 2. eraf-api-gateway 전체 모듈 설치 (최초 1회)
cd ../eraf-api-gateway
mvn clean install -DskipTests
```

### 2.2 빌드 위치

```bash
cd eraf-api-gateway/eraf-gateway-builder
```

### 2.3 사용 가능한 프로파일

#### 기본/프리셋 프로파일

| Profile | 설명 | 포함 기능 |
|---------|------|----------|
| `minimal` | 공통 모듈만 | Common, Memory Store |
| `full` | Phase 1 + OAuth2 | 모든 Core 기능 + OAuth2 |
| `security` | 보안 중심 | Rate Limit, API Key, IP Restriction, JWT, Bot Detection |
| `performance` | 성능 중심 | Rate Limit, Circuit Breaker, Cache |
| `enterprise` | 엔터프라이즈 | Phase 1 + Phase 2 전체 (Redis 필요) |

#### 개별 기능 프로파일

| Profile | 설명 |
|---------|------|
| `rate-limit` | 요청 속도 제한 |
| `api-key` | API Key 인증 |
| `ip-restriction` | IP 기반 접근 제어 |
| `jwt` | JWT 토큰 검증 |
| `circuit-breaker` | 서킷 브레이커 |
| `analytics` | API 분석/통계 |
| `cache` | 응답 캐싱 |
| `bot-detection` | 봇 탐지 |
| `oauth2` | OAuth2 인증 |

#### 스토리지 프로파일

| Profile | 설명 |
|---------|------|
| `store-memory` | 인메모리 스토리지 (기본) |
| `store-jpa` | JPA 기반 DB 스토리지 |

### 2.4 빌드 예시

```bash
# 1. 최소 빌드 (공통 모듈만)
mvn clean package -P minimal -DskipTests

# 2. 전체 기능 빌드 (Phase 1 + OAuth2)
mvn clean package -P full -DskipTests

# 3. 단일 기능 빌드
mvn clean package -P rate-limit -DskipTests
mvn clean package -P jwt -DskipTests
mvn clean package -P oauth2 -DskipTests

# 4. 여러 기능 조합 빌드
mvn clean package -P rate-limit,api-key,jwt -DskipTests
mvn clean package -P cache,analytics -DskipTests
mvn clean package -P rate-limit,circuit-breaker,cache -DskipTests

# 5. 프리셋 사용
mvn clean package -P security -DskipTests
mvn clean package -P performance -DskipTests

# 6. 커스텀 조합 (인증 + 캐시)
mvn clean package -P api-key,jwt,oauth2,cache -DskipTests

# 7. 모니터링 중심
mvn clean package -P analytics,circuit-breaker,rate-limit -DskipTests
```

### 2.5 빌드 결과

```
eraf-api-gateway/eraf-gateway-builder/target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 2.6 실행 방법

```bash
# JAR 실행
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar

# 포트 변경
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar --server.port=9090

# 프로파일 지정
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 3. 빠른 시작 가이드

### 3.1 기본 웹 API 게이트웨이

```bash
# eraf-commons 빌드
cd eraf-commons/eraf-commons-aggregator
mvn clean install -P web,security -DskipTests

# eraf-api-gateway 빌드
cd ../../eraf-api-gateway/eraf-gateway-builder
mvn clean package -P full -DskipTests

# 실행
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 3.2 경량 Rate Limit 게이트웨이

```bash
cd eraf-api-gateway/eraf-gateway-builder
mvn clean package -P rate-limit,store-memory -DskipTests
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 3.3 보안 중심 게이트웨이

```bash
cd eraf-api-gateway/eraf-gateway-builder
mvn clean package -P security -DskipTests
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

---

## 4. 트러블슈팅

### 4.1 의존성 오류

```bash
# 상위 모듈부터 순서대로 빌드
cd eraf-commons && mvn clean install -DskipTests
cd ../eraf-api-gateway && mvn clean install -DskipTests
```

### 4.2 빌드 캐시 문제

```bash
# Maven 로컬 저장소에서 ERAF 관련 캐시 삭제
rm -rf ~/.m2/repository/com/eraf
```

### 4.3 Phase 2 Advanced 기능

다음 기능들은 추가 의존성이 필요합니다:

| 기능 | 필요 의존성 |
|------|------------|
| `rate-limit-advanced` | Redis |
| `analytics-advanced` | Micrometer |
| `validation` | 코드 수정 필요 |
| `load-balancer` | 코드 수정 필요 |

---

## 5. 참고 사항

- 모든 빌드는 `-DskipTests` 옵션으로 테스트 건너뛰기 가능
- 프로파일은 쉼표(,)로 구분하여 여러 개 지정 가능
- `full` 프로파일 사용 시 모든 Core 기능이 포함됨
- 기본 포트: 8080
