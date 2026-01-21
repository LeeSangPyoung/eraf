# ERAF Commons

엔터프라이즈 애플리케이션을 위한 공통 모듈 라이브러리입니다.

## 모듈 구조

```
eraf-commons/
├── eraf-bom/                      # BOM (버전 관리)
├── eraf-core/                     # 핵심 유틸리티 (암호화, 마스킹, 검증 등)
│
├── eraf-web/                      # Web MVC 설정
├── eraf-security/                 # Spring Security
├── eraf-session/                  # 세션 관리
├── eraf-swagger/                  # OpenAPI 문서화
│
├── eraf-data-jpa/                 # JPA/Hibernate
├── eraf-data-mybatis/             # MyBatis
├── eraf-data-redis/               # Redis 연동
├── eraf-data-elasticsearch/       # Elasticsearch
├── eraf-data-database/            # 다중 데이터소스
├── eraf-data-cache/               # 캐시 추상화
│
├── eraf-messaging-kafka/          # Kafka 메시징
├── eraf-messaging-rabbitmq/       # RabbitMQ
├── eraf-messaging-common/         # 메시징 추상화
│
├── eraf-integration-ftp/          # FTP/SFTP
├── eraf-integration-tcp/          # TCP 소켓
├── eraf-integration-s3/           # AWS S3
├── eraf-integration-http/         # HTTP 클라이언트
│
├── eraf-batch/                    # Spring Batch
├── eraf-scheduler/                # 스케줄링
├── eraf-statemachine/             # 상태 머신
├── eraf-notification/             # 알림 (이메일, SMS, 푸시)
└── eraf-actuator/                 # Actuator 모니터링
```

## 빠른 시작

### BOM 사용

```xml
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
```

### 모듈 추가

```xml
<dependencies>
    <!-- Core (필수) -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core</artifactId>
    </dependency>

    <!-- Web -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-web</artifactId>
    </dependency>

    <!-- Data -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-data-jpa</artifactId>
    </dependency>
</dependencies>
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
cd eraf-commons
mvn clean install -DskipTests
```

## 요구사항

- Java 17+
- Spring Boot 3.2+
- Maven 3.8+
