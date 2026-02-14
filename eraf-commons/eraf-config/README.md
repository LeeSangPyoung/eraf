# ERAF Config

Spring Cloud Config 기반 중앙 집중식 설정 관리를 지원하는 모듈입니다.

## 기능

- **Config Server**: Git 기반 중앙 설정 저장소
- **Config Client**: 동적 설정 로드
- **다중 환경 지원**: dev, staging, production 등
- **암호화/복호화**: 민감한 설정 값 보호
- **동적 갱신**: @RefreshScope를 통한 런타임 설정 변경

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-config</artifactId>
</dependency>
```

## Config Server 설정

### 1. application.yml

```yaml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/example/config-repo
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
          default-label: main
          clone-on-start: true
        # 또는 로컬 파일 시스템 사용
        native:
          search-locations: classpath:/config,file:./config

eraf:
  config:
    server:
      git-uri: https://github.com/example/config-repo
      git-branch: main
      git-username: ${GIT_USERNAME}
      git-password: ${GIT_PASSWORD}
      clone-on-start: true
```

### 2. Main Application

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### 3. Config 저장소 구조

```
config-repo/
├── application.yml                 # 공통 설정
├── application-dev.yml             # 개발 환경
├── application-staging.yml         # 스테이징 환경
├── application-prod.yml            # 운영 환경
├── user-service.yml                # 특정 서비스 설정
├── user-service-dev.yml
└── user-service-prod.yml
```

**application.yml (공통 설정)**:

```yaml
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate

logging:
  level:
    root: INFO

eraf:
  common:
    timeout: 30000
```

**user-service-prod.yml**:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/users
    username: user_service
    password: '{cipher}AQA...'  # 암호화된 비밀번호

logging:
  level:
    com.example: WARN
```

## Config Client 설정

### 1. 의존성 추가

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. application.yml

```yaml
spring:
  application:
    name: user-service
  profiles:
    active: dev
  config:
    import: "configserver:http://localhost:8888"
  cloud:
    config:
      uri: http://localhost:8888
      name: ${spring.application.name}
      profile: ${spring.profiles.active}
      label: main
      fail-fast: false

eraf:
  config:
    client:
      uri: http://localhost:8888
      name: user-service
      profile: dev
      label: main
      fail-fast: false
      max-retries: 3
      retry-interval: 1000

# Actuator for refresh endpoint
management:
  endpoints:
    web:
      exposure:
        include: refresh,health,info
```

### 3. 설정 사용

```java
@RestController
@RefreshScope  // 동적 갱신 지원
public class ConfigController {

    @Value("${eraf.common.timeout}")
    private int timeout;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of(
            "timeout", timeout,
            "datasourceUrl", datasourceUrl
        );
    }
}
```

## 동적 설정 갱신

### 1. Git 저장소 설정 변경

```bash
# config-repo에서 설정 변경
vi application.yml
git add .
git commit -m "Update timeout"
git push
```

### 2. Refresh 트리거

```bash
# 단일 인스턴스 갱신
curl -X POST http://localhost:8080/actuator/refresh

# 또는 Spring Cloud Bus 사용 (전체 인스턴스)
curl -X POST http://localhost:8080/actuator/bus-refresh
```

### 3. @RefreshScope

```java
@Component
@RefreshScope
public class DynamicConfig {

    @Value("${feature.enabled}")
    private boolean featureEnabled;

    public boolean isFeatureEnabled() {
        return featureEnabled;
    }
}
```

## 암호화/복호화

### 1. Encryption Key 설정

**Config Server application.yml**:

```yaml
encrypt:
  key: mySecretKey123  # 또는 key-store 사용
```

### 2. 암호화

```bash
# 평문 암호화
curl http://localhost:8888/encrypt -d "mySecretPassword"
# 결과: AQATBvCIPX3vBN...

# Git에 암호화된 값 저장
password: '{cipher}AQATBvCIPX3vBN...'
```

### 3. 복호화

```bash
# 암호문 복호화 (테스트용)
curl http://localhost:8888/decrypt -d "AQATBvCIPX3vBN..."
```

**Client에서 자동 복호화**:

```java
@Value("${spring.datasource.password}")  // 자동으로 복호화된 값
private String password;
```

## 다중 Profile

### application.yml (공통)

```yaml
common:
  app-name: ERAF Services
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dev_db

logging:
  level:
    root: DEBUG
```

### application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/prod_db

logging:
  level:
    root: WARN
```

### Client 실행

```bash
# 개발 환경
java -jar app.jar --spring.profiles.active=dev

# 운영 환경
java -jar app.jar --spring.profiles.active=prod
```

## 우선순위

설정 값 우선순위 (높음 → 낮음):

1. 명령행 인수 (`--server.port=9000`)
2. 환경 변수 (`SERVER_PORT=9000`)
3. `{service-name}-{profile}.yml` (Config Server)
4. `{service-name}.yml` (Config Server)
5. `application-{profile}.yml` (Config Server)
6. `application.yml` (Config Server)
7. 로컬 `application.yml`

## Native Profile (로컬 파일)

### Config Server

```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations:
            - classpath:/config
            - file:./config
            - file:/etc/eraf/config
```

### 파일 구조

```
config/
├── application.yml
├── user-service.yml
└── order-service.yml
```

## 고급 기능

### 1. Composite Configuration

```yaml
spring:
  cloud:
    config:
      server:
        composite:
          - type: git
            uri: https://github.com/example/common-config
          - type: git
            uri: https://github.com/example/service-config
            pattern: user-service*
```

### 2. Health Check

```bash
curl http://localhost:8888/actuator/health
```

### 3. Config Server Endpoints

```bash
# 특정 서비스 설정 조회
GET http://localhost:8888/{service-name}/{profile}
GET http://localhost:8888/{service-name}/{profile}/{label}

# 예제
GET http://localhost:8888/user-service/dev
GET http://localhost:8888/user-service/prod/main
```

### 4. Bootstrap vs Import

**Legacy (Spring Boot 2.x)**:

```yaml
# bootstrap.yml
spring:
  cloud:
    config:
      uri: http://localhost:8888
```

**Modern (Spring Boot 3.x)**:

```yaml
# application.yml
spring:
  config:
    import: "configserver:http://localhost:8888"
```

## 보안

### 1. Config Server 인증

```yaml
# Config Server
spring:
  security:
    user:
      name: admin
      password: secret

# Client
spring:
  cloud:
    config:
      uri: http://localhost:8888
      username: admin
      password: secret
```

### 2. SSH Git 연결

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: git@github.com:example/config-repo.git
          ignore-local-ssh-settings: false
          private-key: |
            -----BEGIN RSA PRIVATE KEY-----
            ...
            -----END RSA PRIVATE KEY-----
```

## Spring Cloud Bus (선택)

### 의존성

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

### 설정

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

management:
  endpoints:
    web:
      exposure:
        include: bus-refresh
```

### 사용

```bash
# 모든 인스턴스 갱신
curl -X POST http://localhost:8080/actuator/bus-refresh

# 특정 서비스만 갱신
curl -X POST http://localhost:8080/actuator/bus-refresh?destination=user-service:**
```

## 문제 해결

### Config Server 연결 실패

```yaml
# fail-fast: true → 시작 실패
# fail-fast: false → 로컬 설정으로 대체
spring:
  cloud:
    config:
      fail-fast: false
      retry:
        max-attempts: 6
        initial-interval: 1000
```

### Git 저장소 동기화 이슈

```bash
# 캐시 초기화
rm -rf ~/.config-repo

# Config Server 재시작
```

### 암호화 키 관리

```yaml
# 환경 변수 사용
encrypt:
  key: ${ENCRYPT_KEY}

# 또는 Key Store
encrypt:
  key-store:
    location: classpath:server.jks
    password: ${KEYSTORE_PASSWORD}
    alias: config-server-key
```

## 모범 사례

1. **민감한 설정 암호화**: DB 비밀번호, API 키 등은 반드시 암호화
2. **버전 관리**: Git을 통한 설정 이력 관리
3. **환경별 분리**: dev/staging/prod 설정 명확히 구분
4. **Fail-fast 비활성화**: 운영 환경에서는 false로 설정하여 가용성 확보
5. **Health Check**: Config Server 상태 모니터링
6. **Refresh Scope**: 동적 갱신이 필요한 Bean에만 적용

## 참고

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Encryption and Decryption](https://cloud.spring.io/spring-cloud-config/reference/html/#_encryption_and_decryption)
- [Spring Cloud Bus](https://spring.io/projects/spring-cloud-bus)
