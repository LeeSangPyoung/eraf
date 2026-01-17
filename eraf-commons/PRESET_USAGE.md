# ERAF Commons Preset 사용 가이드

eraf-commons는 개별 Starter 모듈과 사전 정의된 Preset 조합을 제공합니다.

## Preset이란?

Preset은 자주 사용되는 Starter 조합을 미리 정의해둔 **POM only 모듈**입니다.
다른 프로젝트에서 하나의 dependency만 추가하면, 포함된 모든 Starter를 사용할 수 있습니다.

## 사용 가능한 Preset

### 1. eraf-starter-preset-web-basic
**기본 웹 애플리케이션용**

포함된 Starter:
- eraf-core (필수)
- eraf-starter-web
- eraf-starter-session
- eraf-starter-actuator

사용법:
```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-web-basic</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. eraf-starter-preset-web-jpa
**Web + JPA 애플리케이션용**

포함된 Starter:
- eraf-core (필수)
- eraf-starter-web
- eraf-starter-jpa
- eraf-starter-database

사용법:
```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-web-jpa</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3. eraf-starter-preset-web-redis
**Web + Redis 캐싱 애플리케이션용**

포함된 Starter:
- eraf-core (필수)
- eraf-starter-web
- eraf-starter-redis
- eraf-starter-cache

사용법:
```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-web-redis</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 4. eraf-starter-preset-api-server
**RESTful API 서버용**

포함된 Starter:
- eraf-core (필수)
- eraf-starter-web
- eraf-starter-security
- eraf-starter-swagger
- eraf-starter-actuator

사용법:
```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-api-server</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 5. eraf-starter-preset-microservice
**마이크로서비스용**

포함된 Starter:
- eraf-core (필수)
- eraf-starter-web
- eraf-starter-redis
- eraf-starter-kafka
- eraf-starter-service-client
- eraf-starter-actuator

사용법:
```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-microservice</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 개별 Starter 직접 선택

Preset이 맞지 않는 경우, 개별 Starter를 직접 조합할 수 있습니다:

```xml
<dependencies>
    <!-- Core는 필수 -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- 필요한 Starter만 선택 -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-starter-web</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-starter-mybatis</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-starter-kafka</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## 전체 Starter 목록

| Starter | 설명 |
|---------|------|
| eraf-core | 필수 기본 모듈 |
| eraf-starter-web | Web MVC 지원 |
| eraf-starter-session | 세션 관리 |
| eraf-starter-redis | Redis 연동 |
| eraf-starter-actuator | 모니터링/헬스체크 |
| eraf-starter-security | 보안 기능 |
| eraf-starter-jpa | JPA/Hibernate |
| eraf-starter-mybatis | MyBatis |
| eraf-starter-cache | 캐시 추상화 |
| eraf-starter-kafka | Kafka 메시징 |
| eraf-starter-batch | 배치 처리 |
| eraf-starter-service-client | HTTP 클라이언트 |
| eraf-starter-messaging | 메시징 추상화 |
| eraf-starter-rabbitmq | RabbitMQ |
| eraf-starter-s3 | AWS S3 |
| eraf-starter-elasticsearch | Elasticsearch |
| eraf-starter-ftp | FTP 클라이언트 |
| eraf-starter-tcp | TCP 통신 |
| eraf-starter-notification | 알림 |
| eraf-starter-scheduler | 스케줄링 |
| eraf-starter-statemachine | 상태 머신 |
| eraf-starter-database | 데이터베이스 공통 |
| eraf-starter-swagger | API 문서화 |
| eraf-starter-all | 모든 Starter 포함 |
| eraf-starter-minimal | Core만 포함 |

## Preset vs 개별 선택 비교

### Preset 사용 (추천)
```xml
<!-- 1개 dependency만 추가 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-preset-api-server</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**장점:**
- 간결한 pom.xml
- 검증된 조합
- 버전 관리 용이

### 개별 Starter 선택
```xml
<!-- 5개 dependency 추가 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-security</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-swagger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-starter-actuator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**장점:**
- 정확히 필요한 기능만 선택
- 세밀한 제어 가능

## 빌드 및 설치

eraf-commons를 사용하려면 먼저 로컬 Maven 저장소에 설치해야 합니다:

```bash
cd d:/workspace_eraf/eraf/eraf-commons
mvn clean install -DskipTests
```

빌드 결과:
- 총 32개 모듈 빌드
- `.m2/repository/com/eraf/` 에 설치
- 다른 프로젝트에서 dependency로 사용 가능

## 의존성 확인

프로젝트에서 실제로 포함된 의존성을 확인하려면:

```bash
mvn dependency:tree
```

예시 출력 (preset-api-server 사용 시):
```
[INFO] com.example:my-project:jar:1.0.0
[INFO] \- com.eraf:eraf-starter-preset-api-server:jar:1.0.0-SNAPSHOT:compile
[INFO]    +- com.eraf:eraf-core:jar:1.0.0-SNAPSHOT:compile
[INFO]    +- com.eraf:eraf-starter-web:jar:1.0.0-SNAPSHOT:compile
[INFO]    +- com.eraf:eraf-starter-security:jar:1.0.0-SNAPSHOT:compile
[INFO]    +- com.eraf:eraf-starter-swagger:jar:1.0.0-SNAPSHOT:compile
[INFO]    \- com.eraf:eraf-starter-actuator:jar:1.0.0-SNAPSHOT:compile
```

## eraf-api-gateway와의 차이점

| 구분 | eraf-commons | eraf-api-gateway |
|-----|-------------|------------------|
| **용도** | 라이브러리 제공 | 독립 실행 Gateway |
| **사용 방식** | dependency로 추가 | Maven Profile 선택 빌드 |
| **결과물** | POM only JAR (의존성 메타데이터) | 실행 가능한 FAT JAR |
| **선택 단위** | Preset 또는 개별 Starter | Maven Profile |
| **실행** | 불가능 (라이브러리) | 가능 (java -jar) |

## 예시 프로젝트

### 1. RESTful API 서버 프로젝트

```xml
<project>
    <dependencies>
        <!-- ERAF API Server Preset 사용 -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-starter-preset-api-server</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 2. 마이크로서비스 프로젝트

```xml
<project>
    <dependencies>
        <!-- ERAF Microservice Preset 사용 -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-starter-preset-microservice</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

### 3. 커스텀 조합 프로젝트

```xml
<project>
    <dependencies>
        <!-- Core -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-core</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- 필요한 Starter만 선택 -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-starter-web</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-starter-mybatis</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-starter-rabbitmq</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

## 문의

프로젝트 관련 문의사항은 eraf-commons GitHub 이슈로 등록해주세요.
