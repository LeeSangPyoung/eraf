# ERAF Commons

엔터프라이즈 애플리케이션을 위한 공통 모듈 및 Spring Boot Starter 모음입니다.

> **빌드 가이드**: Maven Profile 기반 선택적 빌드는 [BUILD_GUIDE.md](../BUILD_GUIDE.md)를 참조하세요.

## 모듈 구조

```
eraf-commons/
├── eraf-core/                      # 핵심 유틸리티 및 공통 클래스
├── eraf-commons-aggregator/        # Maven Profile 기반 통합 JAR 빌더
│
├── eraf-starter-web/               # Web MVC 설정
├── eraf-starter-session/           # 세션 관리
├── eraf-starter-redis/             # Redis 연동
├── eraf-starter-actuator/          # Actuator 모니터링
├── eraf-starter-security/          # Spring Security
├── eraf-starter-jpa/               # JPA/Hibernate
├── eraf-starter-mybatis/           # MyBatis
├── eraf-starter-cache/             # 캐시 추상화
├── eraf-starter-kafka/             # Kafka 메시징
├── eraf-starter-batch/             # Spring Batch
├── eraf-starter-service-client/    # REST 클라이언트
├── eraf-starter-messaging/         # 메시징 추상화
├── eraf-starter-rabbitmq/          # RabbitMQ
├── eraf-starter-s3/                # AWS S3
├── eraf-starter-elasticsearch/     # Elasticsearch
├── eraf-starter-ftp/               # FTP/SFTP
├── eraf-starter-tcp/               # TCP 소켓
├── eraf-starter-notification/      # 알림 (이메일, SMS, 푸시)
├── eraf-starter-scheduler/         # 스케줄링
├── eraf-starter-statemachine/      # 상태 머신
├── eraf-starter-database/          # 다중 데이터소스
├── eraf-starter-swagger/           # OpenAPI 문서화
└── eraf-starter-all/               # 전체 Starter 통합
```

## 빠른 시작

### 1. 개별 Starter 사용

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 선택적 빌드 (Aggregator)

```bash
cd eraf-commons/eraf-commons-aggregator

# Core만 포함 (기본)
mvn clean package -DskipTests

# 특정 Starter 조합
mvn clean package -P web,security,jpa -DskipTests

# 전체 Starter 포함
mvn clean package -P full -DskipTests
```

## 사용 가능한 Profiles

| Profile | 포함 Starter |
|---------|-------------|
| `minimal` | eraf-core만 (기본값) |
| `web` | eraf-starter-web |
| `session` | eraf-starter-session |
| `redis` | eraf-starter-redis |
| `actuator` | eraf-starter-actuator |
| `security` | eraf-starter-security |
| `jpa` | eraf-starter-jpa |
| `mybatis` | eraf-starter-mybatis |
| `cache` | eraf-starter-cache |
| `kafka` | eraf-starter-kafka |
| `batch` | eraf-starter-batch |
| `service-client` | eraf-starter-service-client |
| `messaging` | eraf-starter-messaging |
| `rabbitmq` | eraf-starter-rabbitmq |
| `s3` | eraf-starter-s3 |
| `elasticsearch` | eraf-starter-elasticsearch |
| `ftp` | eraf-starter-ftp |
| `tcp` | eraf-starter-tcp |
| `notification` | eraf-starter-notification |
| `scheduler` | eraf-starter-scheduler |
| `statemachine` | eraf-starter-statemachine |
| `database` | eraf-starter-database |
| `swagger` | eraf-starter-swagger |
| `full` | 모든 Starter |

## Core 유틸리티

`eraf-core` 모듈은 다음 유틸리티 클래스를 제공합니다:

- `StringUtils` - 문자열 처리
- `DateUtils` - 날짜/시간 처리
- `JsonUtils` - JSON 직렬화/역직렬화
- `CollectionUtils` - 컬렉션 유틸리티
- `MapUtils` - Map 유틸리티
- `ArrayUtils` - 배열 유틸리티
- `NumberUtils` - 숫자 처리
- `BooleanUtils` - Boolean 처리
- `ObjectUtils` - 객체 유틸리티
- `ReflectionUtils` - 리플렉션 유틸리티
- `RegexUtils` - 정규표현식 유틸리티
- `Base64Utils` - Base64 인코딩/디코딩
- `IpUtils` - IP 주소 처리
- `UrlUtils` - URL 처리
- `IoUtils` - I/O 유틸리티
- `ExceptionUtils` - 예외 처리
- `EnumUtils` - Enum 유틸리티
- `RandomUtils` - 랜덤 값 생성
- `SystemUtils` - 시스템 정보

## 요구사항

- Java 17+
- Spring Boot 3.2+
- Maven 3.8+
