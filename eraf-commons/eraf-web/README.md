# ERAF Web

ERAF Web 모듈은 웹 애플리케이션 개발을 위한 핵심 기능을 제공합니다.

## 주요 기능

### 1. Request Context Management

HTTP 요청 정보를 ThreadLocal에 저장하여 애플리케이션 전체에서 접근 가능하게 합니다.

#### 주요 구성 요소

- **RequestContext**: 불변(immutable) 요청 컨텍스트 객체
  - Request ID, Trace ID, Session ID
  - User ID, Username
  - Client IP, User Agent
  - Request Method, URI, Query String
  - Headers, Attributes

- **RequestContextHolder**: ThreadLocal 기반 컨텍스트 홀더
  - `InheritableThreadLocal` 사용으로 자동 전파
  - 스레드 안전성 보장

- **RequestContextFilter**: 자동 컨텍스트 캡처 필터
  - `Order: HIGHEST_PRECEDENCE + 10`으로 조기 실행
  - Spring Security 선택적 통합 (Reflection 사용)
  - 자동 Request ID/Trace ID 생성

### 2. MDC (Mapped Diagnostic Context) 통합

SLF4J MDC와 통합하여 로그에 자동으로 컨텍스트 정보를 포함합니다.

#### MdcContextPropagator

- RequestContext → MDC 자동 전파
- 로그에 자동 추가되는 필드:
  - `requestId`: 요청 고유 ID
  - `traceId`: 분산 추적 ID
  - `sessionId`: 세션 ID
  - `userId`: 사용자 ID
  - `username`: 사용자 이름
  - `clientIp`: 클라이언트 IP

### 3. Async Context Propagation

비동기 메서드에서도 컨텍스트가 전파되도록 합니다.

#### RequestContextTaskDecorator

- `@Async` 메서드로 컨텍스트 전파
- `TaskDecorator` 인터페이스 구현
- 부모 스레드 → 자식 스레드 컨텍스트 복사

## 설정

### application.yml

```yaml
eraf:
  web:
    request-context:
      enabled: true  # RequestContext 기능 활성화 (기본값: true)
      trace-id-header: X-Trace-Id  # Trace ID 헤더 이름 (기본값: X-Trace-Id)
      request-id-header: X-Request-Id  # Request ID 헤더 이름 (기본값: X-Request-Id)

# 로그에 MDC 포함
logging:
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{userId}] - %msg%n'
```

### Auto Configuration

`ErafWebAutoConfiguration`이 자동으로 활성화됩니다.

등록되는 Bean:
- `RequestContextFilter`
- `MdcContextPropagator`
- `RequestContextTaskDecorator`

## 사용 예제

### 1. 기본 사용

```java
@RestController
public class MyController {

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getUsers() {
        // 현재 요청의 컨텍스트 가져오기
        RequestContext context = RequestContextHolder.getContext();

        String requestId = context.getRequestId();
        String userId = context.getUserId();
        String clientIp = context.getClientIp();

        log.info("Getting users for userId: {}", userId);
        // 로그에 자동으로 [requestId] [traceId] [userId] 포함됨

        return ResponseEntity.ok(userService.findAll());
    }
}
```

### 2. Service 레이어에서 사용

```java
@Service
public class UserService {

    public void createUser(UserDto dto) {
        RequestContext context = RequestContextHolder.getContext();

        User user = new User();
        user.setName(dto.getName());
        user.setCreatedBy(context.getUserId());  // 현재 사용자 ID 자동 설정
        user.setCreatedIp(context.getClientIp());  // 클라이언트 IP 자동 설정

        userRepository.save(user);

        log.info("User created: {}", user.getId());
        // 로그에 자동으로 requestId, traceId 포함
    }
}
```

### 3. Async 메서드에서 사용

```java
@Service
public class NotificationService {

    @Async
    public CompletableFuture<Void> sendNotificationAsync(String userId, String message) {
        // RequestContextTaskDecorator가 자동으로 컨텍스트를 전파
        RequestContext context = RequestContextHolder.getContext();

        log.info("Sending notification to user: {}", userId);
        // 로그에 부모 스레드의 requestId, traceId가 자동 포함됨

        // 알림 전송 로직...

        return CompletableFuture.completedFuture(null);
    }
}
```

### 4. 수동으로 컨텍스트 설정 (테스트/배치)

```java
@Component
public class BatchJob {

    @Scheduled(cron = "0 0 * * * ?")
    public void runBatch() {
        // 배치 작업용 컨텍스트 수동 생성
        RequestContext context = RequestContext.builder()
                .requestId(UUID.randomUUID().toString())
                .userId("system")
                .username("Batch System")
                .build();

        RequestContextHolder.setContext(context);

        try {
            // 배치 작업 실행
            processBatch();

            log.info("Batch completed");
            // 로그에 설정한 requestId, userId 포함됨
        } finally {
            // 반드시 정리
            RequestContextHolder.clear();
        }
    }
}
```

### 5. Custom Attribute 활용

```java
@Component
public class RequestEnrichmentInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestContext context = RequestContextHolder.getContext();

        if (context != null) {
            // 커스텀 속성 추가 (예: 권한 정보, 테넌트 ID 등)
            String tenantId = request.getHeader("X-Tenant-Id");

            // 주의: RequestContext는 불변이므로 속성 추가 시 새로 생성 필요
            RequestContext enrichedContext = RequestContext.builder()
                    .requestId(context.getRequestId())
                    .traceId(context.getTraceId())
                    .sessionId(context.getSessionId())
                    .userId(context.getUserId())
                    .username(context.getUsername())
                    .clientIp(context.getClientIp())
                    .userAgent(context.getUserAgent())
                    .method(context.getMethod())
                    .uri(context.getUri())
                    .queryString(context.getQueryString())
                    .locale(context.getLocale())
                    .requestTime(context.getRequestTime())
                    .headers(context.getHeaders())
                    .attributes(context.getAttributes())
                    .attribute("tenantId", tenantId)  // 추가 속성
                    .build();

            RequestContextHolder.setContext(enrichedContext);
        }

        return true;
    }
}
```

## Spring Security 통합

RequestContextFilter는 Spring Security가 클래스패스에 있을 경우 자동으로 통합됩니다.

```java
// Spring Security가 있으면 자동으로 사용자 정보 추출
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
if (authentication != null && authentication.isAuthenticated()) {
    String username = authentication.getName();
    // RequestContext에 자동 설정됨
}
```

## 로깅 패턴 권장 사항

### Logback (logback-spring.xml)

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{userId}] - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{sessionId}] [%X{userId}] [%X{clientIp}] - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Log4j2 (log4j2.xml)

```xml
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{userId}] - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## 성능 고려사항

### ThreadLocal 메모리 누수 방지

- `RequestContextFilter`가 자동으로 `finally` 블록에서 정리
- 수동 설정 시 **반드시** `try-finally`로 정리

```java
RequestContext context = RequestContext.builder()...build();
RequestContextHolder.setContext(context);
try {
    // 작업 수행
} finally {
    RequestContextHolder.clear();  // 필수!
    MdcContextPropagator.clear();  // MDC도 정리
}
```

### InheritableThreadLocal 주의사항

- 스레드 풀 사용 시 컨텍스트가 재사용될 수 있음
- `RequestContextTaskDecorator`를 사용하여 안전하게 전파
- 작업 완료 후 자동 정리됨

## 테스트

### 단위 테스트에서 RequestContext 모킹

```java
@SpringBootTest
class MyServiceTest {

    @BeforeEach
    void setUp() {
        RequestContext context = RequestContext.builder()
                .requestId("test-request-id")
                .userId("test-user")
                .username("Test User")
                .clientIp("127.0.0.1")
                .build();

        RequestContextHolder.setContext(context);
        MdcContextPropagator.propagate(context);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
        MdcContextPropagator.clear();
    }

    @Test
    void testWithContext() {
        // 테스트 코드...
        RequestContext context = RequestContextHolder.getContext();
        assertThat(context.getUserId()).isEqualTo("test-user");
    }
}
```

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-web</artifactId>
    <version>${eraf.version}</version>
</dependency>
```

**선택적 의존성:**
- Spring Security: 자동 사용자 정보 추출 (없어도 동작)

## 통합 모듈

- **eraf-core**: 공통 유틸리티, ErafContext
- **eraf-security**: JWT, API Key 인증 (RequestContext 사용)
- **eraf-data-jpa**: Audit Logging (RequestContext에서 사용자 정보 자동 추출)
- **eraf-actuator**: Health Check, Metrics (RequestContext 정보 활용)

## 문의

기술 지원 및 문의: ERAF Development Team
