# ERAF (Enterprise Reusable Asset Framework)

엔터프라이즈 애플리케이션 개발을 위한 재사용 가능한 공통 모듈 라이브러리

## 개요

ERAF는 Spring Boot 3.x 기반의 엔터프라이즈 공통 모듈 라이브러리입니다. 반복적으로 구현되는 기능들을 표준화하여 개발 생산성을 높이고 코드 품질을 일관되게 유지할 수 있습니다.

### 주요 컴포넌트

| 모듈 | 설명 |
|------|------|
| **eraf-commons** | 공통 유틸리티 및 모듈 라이브러리 (24개 모듈) |
| **eraf-api-gateway** | Kong-style API Gateway (13개 기능) |
| **eraf-sample-app** | 샘플 애플리케이션 |

## 기술 스택

- **Java**: 23
- **Spring Boot**: 3.2.x
- **Build Tool**: Maven

## 프로젝트 구조

```
eraf/
├── eraf-commons/                      # 공통 모듈
│   ├── eraf-bom/                      # 버전 관리 BOM
│   ├── eraf-core/                     # 핵심 기능 (암호화, 마스킹, 검증 등)
│   │
│   ├── eraf-web/                      # Web MVC 자동설정
│   ├── eraf-security/                 # Spring Security
│   ├── eraf-session/                  # 세션 관리
│   ├── eraf-swagger/                  # API 문서화
│   │
│   ├── eraf-data-jpa/                 # JPA 자동설정
│   ├── eraf-data-mybatis/             # MyBatis 자동설정
│   ├── eraf-data-redis/               # Redis 연동
│   ├── eraf-data-elasticsearch/       # Elasticsearch 연동
│   ├── eraf-data-database/            # 다중 데이터소스
│   ├── eraf-data-cache/               # 캐시 추상화
│   │
│   ├── eraf-messaging-kafka/          # Kafka 메시징
│   ├── eraf-messaging-rabbitmq/       # RabbitMQ 연동
│   ├── eraf-messaging-common/         # 메시징 추상화
│   │
│   ├── eraf-integration-ftp/          # FTP/SFTP 연동
│   ├── eraf-integration-tcp/          # TCP 소켓 통신
│   ├── eraf-integration-s3/           # AWS S3 연동
│   ├── eraf-integration-http/         # HTTP 클라이언트
│   │
│   ├── eraf-batch/                    # Spring Batch
│   ├── eraf-scheduler/                # 스케줄링
│   ├── eraf-statemachine/             # 상태 머신
│   ├── eraf-notification/             # 알림 (이메일, SMS, 푸시)
│   └── eraf-actuator/                 # Actuator 모니터링
│
├── eraf-api-gateway/                  # API Gateway
│   ├── eraf-gateway-common/           # 공통 필터, 예외 처리
│   ├── eraf-gateway-builder/          # 실행 가능 JAR 빌더
│   ├── eraf-gateway-store-memory/     # InMemory 스토리지
│   ├── eraf-gateway-store-jpa/        # JPA 스토리지
│   │
│   ├── eraf-gateway-feature-rate-limit/      # Rate Limiting
│   ├── eraf-gateway-feature-api-key/         # API Key 인증
│   ├── eraf-gateway-feature-ip-restriction/  # IP 제한
│   ├── eraf-gateway-feature-jwt/             # JWT 검증
│   ├── eraf-gateway-feature-circuit-breaker/ # 서킷 브레이커
│   ├── eraf-gateway-feature-analytics/       # API 분석
│   ├── eraf-gateway-feature-cache/           # 응답 캐싱
│   ├── eraf-gateway-feature-bot-detection/   # 봇 탐지
│   └── eraf-gateway-feature-oauth2/          # OAuth2 인증
│
└── eraf-sample-app/                   # 샘플 애플리케이션
```

## 빠른 시작

### 1. eraf-commons 사용

```xml
<!-- BOM 추가 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 필요한 모듈 추가 -->
<dependencies>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### 2. eraf-api-gateway 빌드 및 실행

```bash
# 전체 모듈 설치 (최초 1회)
cd eraf-commons && mvn clean install -DskipTests
cd ../eraf-api-gateway && mvn clean install -DskipTests

# Gateway 빌드 (필요한 기능만 선택)
cd eraf-api-gateway/eraf-gateway-builder

# 전체 기능
mvn clean package -P full -DskipTests

# 보안 기능만
mvn clean package -P security -DskipTests

# 특정 기능 조합
mvn clean package -P minimal,rate-limit,jwt,api-key -DskipTests

# 실행
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

### 3. Gateway 빌드 프로파일

| 프로파일 | 설명 |
|---------|------|
| `full` | 전체 기능 (기본값) |
| `minimal` | Common + Memory Store만 |
| `security` | 보안 기능 (rate-limit, api-key, ip-restriction, jwt, bot-detection) |
| `performance` | 성능 기능 (rate-limit, circuit-breaker, cache) |

개별 기능: `rate-limit`, `api-key`, `ip-restriction`, `jwt`, `circuit-breaker`, `analytics`, `cache`, `bot-detection`, `oauth2`

## eraf-core 주요 기능

### 1. 암호화 (crypto)

```java
// AES-256-GCM 암호화
String encrypted = Crypto.encrypt("평문", "secretKey");
String decrypted = Crypto.decrypt(encrypted, "secretKey");

// 비밀번호 해싱 (bcrypt)
String hashed = Password.hash("password123");
boolean matches = Password.verify("password123", hashed);

// JWT 토큰
String token = Jwt.create()
    .subject("userId")
    .claim("role", "ADMIN")
    .expireAfter(Duration.ofHours(1))
    .sign("secretKey");
```

### 2. 마스킹 (masking)

```java
Masking.name("홍길동");           // 홍*동
Masking.phone("010-1234-5678");  // 010-****-5678
Masking.email("test@email.com"); // te**@email.com
Masking.cardNumber("1234-5678-9012-3456"); // 1234-****-****-3456
```

### 3. 검증 어노테이션 (validation)

```java
public class UserRequest {
    @Email
    private String email;

    @Phone
    private String phone;

    @Password(minLength = 8, requireSpecial = true)
    private String password;

    @NoXss
    private String content;
}
```

### 4. 표준 응답 (response)

```java
ApiResponse.success(data);
ApiResponse.error("ERROR_CODE", "에러 메시지");
PageResponse.of(page, totalElements, totalPages, content);
```

### 5. HTTP 클라이언트 (http)

```java
ErafHttpClient client = ErafHttpClient.create()
    .baseUrl("https://api.example.com")
    .timeout(Duration.ofSeconds(30))
    .bearerToken("token");

User user = client.get("/users/1", User.class);
```

## 모듈 목록

| 카테고리 | 모듈 | 설명 |
|---------|------|------|
| **Core** | `eraf-core` | 핵심 유틸리티 (암호화, JWT, 마스킹, 검증 등) |
| **Web** | `eraf-web` | Web MVC 자동설정, CORS, 로깅 |
| | `eraf-security` | Spring Security 자동설정 |
| | `eraf-session` | 세션 관리 |
| | `eraf-swagger` | OpenAPI 문서화 |
| **Data** | `eraf-data-jpa` | JPA/Hibernate 자동설정 |
| | `eraf-data-mybatis` | MyBatis 자동설정 |
| | `eraf-data-redis` | Redis 연동 (분산락, 캐시) |
| | `eraf-data-elasticsearch` | Elasticsearch 연동 |
| | `eraf-data-database` | 다중 데이터소스 |
| | `eraf-data-cache` | 캐시 추상화 |
| **Messaging** | `eraf-messaging-kafka` | Kafka Producer/Consumer |
| | `eraf-messaging-rabbitmq` | RabbitMQ 연동 |
| | `eraf-messaging-common` | 메시징 추상화 |
| **Integration** | `eraf-integration-ftp` | FTP/SFTP 연동 |
| | `eraf-integration-tcp` | TCP 소켓 통신 |
| | `eraf-integration-s3` | AWS S3 연동 |
| | `eraf-integration-http` | HTTP 클라이언트 |
| **Processing** | `eraf-batch` | Spring Batch 자동설정 |
| | `eraf-scheduler` | 스케줄링 관리 |
| | `eraf-statemachine` | 상태 머신 |
| | `eraf-notification` | 알림 (이메일, SMS, 푸시) |
| | `eraf-actuator` | Actuator 모니터링 |

## 빌드

```bash
# eraf-commons 빌드
cd eraf-commons
mvn clean install -DskipTests

# eraf-api-gateway 빌드
cd eraf-api-gateway
mvn clean install -DskipTests

# eraf-sample-app 빌드
cd eraf-sample-app
mvn clean package -DskipTests
```

## 요구사항

- Java 23
- Maven 3.8+
- Spring Boot 3.2+

## 라이선스

MIT License
