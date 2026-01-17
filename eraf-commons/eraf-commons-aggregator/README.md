# ERAF Commons Aggregator

Maven Profile을 사용하여 선택한 Starter들만 포함된 **단일 통합 JAR**를 생성합니다.

## 사용 방법

### 1. Core만 포함 (Minimal)

```bash
cd eraf-commons/eraf-commons-aggregator
mvn clean package -P minimal -DskipTests
```

**결과**: `target/eraf-commons-1.0.0-SNAPSHOT.jar` (332KB)
**포함**: eraf-core만

### 2. Redis + Kafka 포함

```bash
mvn clean package -P redis,kafka -DskipTests
```

**결과**: `target/eraf-commons-1.0.0-SNAPSHOT.jar` (366KB)
**포함**: eraf-core + eraf-starter-redis + eraf-starter-kafka

### 3. Web + JPA + Database 포함

```bash
mvn clean package -P web,jpa,database -DskipTests
```

**포함**: eraf-core + eraf-starter-web + eraf-starter-jpa + eraf-starter-database

### 4. 모든 Starter 포함 (Full)

```bash
mvn clean package -P full -DskipTests
```

**포함**: 모든 Starter 모듈

## 사용 가능한 Profile

| Profile | Starter |
|---------|---------|
| `minimal` | core만 (기본값) |
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

## Maven 저장소에 설치

생성된 JAR를 로컬 Maven 저장소에 설치하려면:

```bash
mvn clean install -P redis,kafka -DskipTests
```

설치 위치: `~/.m2/repository/com/eraf/eraf-commons-aggregator/1.0.0-SNAPSHOT/`

## 다른 프로젝트에서 사용

### 방법 1: 직접 JAR 사용

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-commons-aggregator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 방법 2: 빌드한 JAR를 로컬에 복사

```bash
# JAR 복사
cp target/eraf-commons-1.0.0-SNAPSHOT.jar /your/project/libs/

# 프로젝트에서 사용
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-commons</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/eraf-commons-1.0.0-SNAPSHOT.jar</systemPath>
</dependency>
```

## 빌드 예시

### 예시 1: 기본 웹 애플리케이션

```bash
mvn clean package -P web,session,actuator -DskipTests
```

### 예시 2: API 서버

```bash
mvn clean package -P web,security,swagger,actuator -DskipTests
```

### 예시 3: 마이크로서비스

```bash
mvn clean package -P web,redis,kafka,service-client,actuator -DskipTests
```

### 예시 4: Web + Database (JPA)

```bash
mvn clean package -P web,jpa,database -DskipTests
```

### 예시 5: Web + Database (MyBatis)

```bash
mvn clean package -P web,mybatis,database -DskipTests
```

## JAR 내용 확인

생성된 JAR에 포함된 클래스 확인:

```bash
jar -tf target/eraf-commons-1.0.0-SNAPSHOT.jar | grep "^com/eraf"
```

의존성 확인:

```bash
mvn dependency:tree -P redis,kafka
```

## 주의사항

1. **Core는 항상 포함됩니다** - 모든 Starter의 기반이 되는 필수 모듈입니다.
2. **Profile을 지정하지 않으면 minimal이 활성화됩니다** - core만 포함됩니다.
3. **여러 Profile을 조합할 수 있습니다** - 쉼표로 구분합니다.
4. **Spring Boot 의존성은 제외됩니다** - com.eraf 모듈만 포함되고 Spring Boot 등의 외부 라이브러리는 제외됩니다.

## Shade Plugin 동작

Maven Shade Plugin이 다음 작업을 수행합니다:

1. **선택한 Starter JAR들을 하나로 병합**
2. **Spring Boot AutoConfiguration 메타데이터 병합**
   - META-INF/spring.factories
   - META-INF/spring.handlers
   - META-INF/spring.schemas
   - META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
3. **외부 의존성 제외** (Spring Boot, Jackson 등)
4. **서명 파일 제거** (META-INF/*.SF, *.DSA, *.RSA)

## eraf-api-gateway와의 비교

| 항목 | eraf-commons-aggregator | eraf-api-gateway |
|-----|------------------------|------------------|
| 목적 | 라이브러리 통합 JAR | 독립 실행 Gateway |
| 선택 방식 | Maven Profile | Maven Profile |
| 결과물 크기 | 300~400KB (Starter만) | 55MB (전체 의존성 포함) |
| 실행 가능 | ❌ (라이브러리) | ✅ (java -jar) |
| 사용 방법 | 다른 프로젝트 dependency | 독립 실행 |
| Spring Boot 포함 | ❌ 제외됨 | ✅ 포함됨 (Fat JAR) |

## 문의

문의사항은 eraf-commons 프로젝트 이슈로 등록해주세요.
