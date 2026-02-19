# ERAF Commons

엔터프라이즈 애플리케이션을 위한 공통 모듈 라이브러리입니다.

**47개 모듈 | 578개 Java 클래스 | Spring Boot 3.2.11 | Java 21**

## 모듈 구조

```
eraf-commons/
├── eraf-bom/                      # BOM (버전 관리 - 46개 모듈)
│
├── Core (10 modules)
│   ├── eraf-core/                 # 핵심 유틸리티
│   ├── eraf-core-crypto/          # 암호화/복호화
│   ├── eraf-core-util/            # 공통 유틸
│   ├── eraf-core-exception/       # 예외 처리
│   ├── eraf-core-validation/      # 검증
│   ├── eraf-core-resilience/      # 복원력 패턴
│   ├── eraf-core-async/           # 비동기 처리
│   ├── eraf-core-i18n/            # 국제화
│   ├── eraf-core-http/            # HTTP 유틸
│   └── eraf-core-system/          # 시스템 유틸
│
├── Web (5 modules)
│   ├── eraf-web/                  # Web MVC 설정
│   ├── eraf-security/             # Spring Security (JWT/API Key/OAuth2/RBAC)
│   ├── eraf-session/              # 세션 관리
│   ├── eraf-swagger/              # OpenAPI 문서화
│   └── eraf-gateway/              # API Gateway
│
├── Data (6 modules)
│   ├── eraf-data-jpa/             # JPA/Hibernate
│   ├── eraf-data-mybatis/         # MyBatis
│   ├── eraf-data-redis/           # Redis (분산락, 캐시)
│   ├── eraf-data-elasticsearch/   # Elasticsearch
│   ├── eraf-data-cache/           # 캐시 추상화
│   └── eraf-data-mongo/           # MongoDB
│
├── Messaging (2 modules)
│   ├── eraf-messaging-kafka/      # Kafka (DLQ 포함)
│   └── eraf-messaging-rabbitmq/   # RabbitMQ (DLQ 포함)
│
├── Integration (6 modules)
│   ├── eraf-integration-ftp/      # FTP/SFTP
│   ├── eraf-integration-tcp/      # TCP 소켓
│   ├── eraf-integration-s3/       # AWS S3
│   ├── eraf-integration-http/     # HTTP/Feign
│   ├── eraf-integration-grpc/     # gRPC
│   └── eraf-integration-websocket/# WebSocket/STOMP
│
├── Processing (7 modules)
│   ├── eraf-batch/                # Spring Batch
│   ├── eraf-scheduler/            # 스케줄링
│   ├── eraf-statemachine/         # 상태 머신
│   ├── eraf-saga/                 # Saga 패턴
│   ├── eraf-outbox/               # Outbox 패턴
│   ├── eraf-workflow/             # 워크플로우 엔진
│   └── eraf-report/               # 리포트 (CSV/HTML/PDF/Excel)
│
├── Observability (3 modules)
│   ├── eraf-actuator/             # Health/Metrics/Tracing
│   ├── eraf-observability/        # OpenTelemetry
│   └── eraf-notification/         # 알림 (이메일, SMS, Slack, Teams)
│
├── Document & Media (4 modules)
│   ├── eraf-excel/                # Excel 처리
│   ├── eraf-pdf/                  # PDF 생성
│   ├── eraf-barcode/              # Barcode/QR
│   └── eraf-image/                # 이미지 처리
│
└── Other (3 modules)
    ├── eraf-config/               # 동적 설정/Cloud Config/Vault
    ├── eraf-feature-flag/         # Feature Flag
    └── eraf-test/                 # 테스트 유틸
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
        <artifactId>eraf-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-data-jpa</artifactId>
    </dependency>
</dependencies>
```

## 엔터프라이즈 패턴

| 패턴 | 모듈 | 설명 |
|------|------|------|
| **Saga** | `eraf-saga` | 분산 트랜잭션 (보상 트랜잭션) |
| **State Machine** | `eraf-statemachine` | 유한 상태 기계 |
| **Workflow** | `eraf-workflow` | 워크플로우 엔진 (결재/승인) |
| **Outbox** | `eraf-outbox` | 이벤트 발행 보장 |
| **Feature Flag** | `eraf-feature-flag` | 기능 플래그 (점진적 롤아웃) |
| **Circuit Breaker** | `eraf-core-resilience` | 서킷 브레이커, 재시도, 벌크헤드 |
| **Distributed Lock** | `eraf-data-redis` | Redis 기반 분산 락 |
| **Idempotency** | `eraf-core` | 멱등성 보장 |

## 빌드

```bash
mvn clean install                    # 전체 빌드
mvn clean install -DskipTests        # 테스트 제외
mvn validate                         # Enforcer 검증
mvn checkstyle:check                 # 코드 스타일 검사
mvn test jacoco:report               # 커버리지 리포트
```

## 요구사항

- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.11
- **Spring Cloud**: 2023.0.3
- **Maven**: 3.8+

## 문서

- [CONTRIBUTING.md](CONTRIBUTING.md) - 기여 가이드
- [CHANGELOG.md](CHANGELOG.md) - 변경 이력
- [SECURITY.md](SECURITY.md) - 보안 정책
- [ERAF-COMMONS-FEATURE-CATALOG.md](ERAF-COMMONS-FEATURE-CATALOG.md) - 기능 카탈로그
- [ERAF-COMMONS-ANALYSIS-REPORT.md](ERAF-COMMONS-ANALYSIS-REPORT.md) - 분석 리포트
