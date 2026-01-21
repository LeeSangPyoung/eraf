# ERAF Build Guide

## 1. eraf-commons 빌드

eraf-commons는 전체 모듈을 빌드합니다.

### 빌드 명령어

```bash
cd eraf-commons
mvn clean install -DskipTests
```

### 빌드 결과

25개 모듈이 빌드됩니다:
- eraf-bom
- eraf-core
- eraf-web, eraf-security, eraf-session, eraf-swagger
- eraf-data-jpa, eraf-data-mybatis, eraf-data-redis, eraf-data-elasticsearch, eraf-data-database, eraf-data-cache
- eraf-messaging-kafka, eraf-messaging-rabbitmq, eraf-messaging-common
- eraf-integration-ftp, eraf-integration-tcp, eraf-integration-s3, eraf-integration-http
- eraf-batch, eraf-scheduler, eraf-statemachine, eraf-notification, eraf-actuator

---

## 2. eraf-api-gateway 빌드

### 2.1 전체 모듈 설치 (최초 1회)

```bash
cd eraf-api-gateway
mvn clean install -DskipTests
```

### 2.2 Gateway 빌드 (선택적)

```bash
cd eraf-api-gateway/eraf-gateway-builder
```

### 2.3 사용 가능한 프로파일

#### 프리셋 프로파일

| Profile | 설명 |
|---------|------|
| `full` | 전체 기능 (기본값) |
| `minimal` | Common + Memory Store만 |
| `security` | 보안 기능 (rate-limit, api-key, ip-restriction, jwt, bot-detection) |
| `performance` | 성능 기능 (rate-limit, circuit-breaker, cache) |
| `enterprise` | 전체 + Phase 2 Advanced 기능 |

#### 개별 기능 프로파일

| Profile | 기능 |
|---------|------|
| `rate-limit` | Rate Limiting |
| `api-key` | API Key 인증 |
| `ip-restriction` | IP 제한 |
| `jwt` | JWT 검증 |
| `circuit-breaker` | 서킷 브레이커 |
| `analytics` | API 분석 |
| `cache` | 응답 캐싱 |
| `bot-detection` | 봇 탐지 |
| `oauth2` | OAuth2 인증 |

#### 스토리지 프로파일

| Profile | 설명 |
|---------|------|
| `store-memory` | InMemory 스토리지 (기본값) |
| `store-jpa` | JPA 스토리지 |

### 2.4 빌드 예시

```bash
# 1. 전체 기능 빌드 (기본)
mvn clean package -P full -DskipTests

# 2. 최소 기능 빌드
mvn clean package -P minimal -DskipTests

# 3. 보안 기능만 빌드
mvn clean package -P security -DskipTests

# 4. 성능 기능만 빌드
mvn clean package -P performance -DskipTests

# 5. 특정 기능 조합 빌드
mvn clean package -P minimal,rate-limit,jwt -DskipTests
mvn clean package -P minimal,rate-limit,api-key,cache -DskipTests

# 6. JPA 스토리지 사용
mvn clean package -P full,store-jpa -DskipTests
```

### 2.5 빌드 결과물

```
target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 2.6 실행

```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

---

## 3. eraf-sample-app 빌드

```bash
cd eraf-sample-app
mvn clean package -DskipTests
java -jar target/eraf-sample-app-1.0.0-SNAPSHOT.jar
```

---

## 4. 전체 프로젝트 빌드

```bash
# 1. eraf-commons 빌드
cd eraf-commons
mvn clean install -DskipTests

# 2. eraf-api-gateway 빌드
cd ../eraf-api-gateway
mvn clean install -DskipTests

# 3. eraf-sample-app 빌드
cd ../eraf-sample-app
mvn clean package -DskipTests
```

---

## 5. 트러블슈팅

### 의존성 오류

```bash
# 상위 모듈부터 순서대로 빌드
cd eraf-commons && mvn clean install -DskipTests
cd ../eraf-api-gateway && mvn clean install -DskipTests
```

### 빌드 캐시 문제

```bash
# Maven 로컬 저장소에서 ERAF 관련 캐시 삭제
rm -rf ~/.m2/repository/com/eraf
```

---

## 6. 요구사항

- Java 23
- Maven 3.8+
- Spring Boot 3.2+
