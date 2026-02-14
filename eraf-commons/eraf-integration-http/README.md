# ERAF Integration - HTTP

선언적 HTTP 클라이언트를 지원하는 모듈입니다.

## 기능

- **선언적 API 클라이언트**: `@ErafClient` 어노테이션 기반
- **JWT 자동 전파**: Authorization 헤더 자동 추가
- **TraceId/UserId 헤더**: 분산 추적 및 사용자 컨텍스트 전파
- **Circuit Breaker**: 장애 차단 및 빠른 실패
- **재시도 로직**: 실패 시 자동 재시도
- **타임아웃 설정**: 연결 및 읽기 타임아웃

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-integration-http</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
eraf:
  http:
    connect-timeout: 5000
    read-timeout: 30000
    max-connections: 200
    max-connections-per-route: 20
    default-headers:
      User-Agent: ERAF-HTTP-Client/1.0
      Accept: application/json
```

## 사용법

### 1. 선언적 HTTP 클라이언트

```java
import com.eraf.http.ErafClient;
import org.springframework.web.bind.annotation.*;

@ErafClient(
    value = "user-service",  // Service Discovery 이름
    path = "/api",
    circuitBreaker = true,
    retry = 3,
    timeout = 30000
)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable("id") Long id);

    @PostMapping("/users")
    UserResponse createUser(@RequestBody UserRequest request);

    @PutMapping("/users/{id}")
    UserResponse updateUser(@PathVariable("id") Long id, @RequestBody UserRequest request);

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
```

### 2. 직접 URL 지정

```java
@ErafClient(
    url = "https://api.example.com",
    path = "/v1"
)
public interface ExternalApiClient {

    @GetMapping("/data")
    DataResponse getData(@RequestParam("query") String query);
}
```

### 3. 클라이언트 사용

```java
@Service
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public UserResponse getUser(Long id) {
        return userClient.getUser(id);
    }

    public UserResponse createUser(UserRequest request) {
        return userClient.createUser(request);
    }
}
```

## 고급 기능

### 1. JWT 자동 전파

```java
// SecurityContext에서 JWT를 자동으로 추출하여 Authorization 헤더에 추가
@ErafClient(value = "protected-service")
public interface ProtectedApiClient {

    @GetMapping("/protected/data")
    DataResponse getProtectedData();  // Authorization: Bearer {JWT} 자동 추가
}
```

### 2. TraceId/UserId 헤더

```java
// X-Trace-Id, X-User-Id 헤더 자동 추가
@ErafClient(value = "traced-service")
public interface TracedApiClient {

    @GetMapping("/traced/data")
    DataResponse getTracedData();
}
```

**Request Headers 예시**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
X-Trace-Id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
X-User-Id: user123
```

### 3. Circuit Breaker 설정

```yaml
eraf:
  http:
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      wait-duration-in-open-state: 60000
      sliding-window-size: 100
```

```java
@ErafClient(
    value = "unstable-service",
    circuitBreaker = true  // Circuit Breaker 활성화
)
public interface UnstableApiClient {
    @GetMapping("/data")
    DataResponse getData();
}
```

### 4. 재시도 설정

```java
@ErafClient(
    value = "retry-service",
    retry = 5  // 최대 5회 재시도
)
public interface RetryApiClient {
    @GetMapping("/data")
    DataResponse getData();
}
```

### 5. 커스텀 헤더

```java
@ErafClient(value = "custom-service")
public interface CustomHeaderClient {

    @GetMapping("/data")
    DataResponse getData(
        @RequestHeader("X-Custom-Header") String customHeader,
        @RequestHeader("X-API-Key") String apiKey
    );
}
```

### 6. 파일 업로드

```java
@ErafClient(value = "file-service")
public interface FileClient {

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    UploadResponse upload(@RequestPart("file") MultipartFile file);
}
```

### 7. 동기/비동기 호출

```java
import reactor.core.publisher.Mono;
import java.util.concurrent.CompletableFuture;

@ErafClient(value = "async-service")
public interface AsyncApiClient {

    // 동기 호출
    @GetMapping("/sync")
    DataResponse getSync();

    // 비동기 호출 (Mono)
    @GetMapping("/async/mono")
    Mono<DataResponse> getAsyncMono();

    // 비동기 호출 (CompletableFuture)
    @GetMapping("/async/future")
    CompletableFuture<DataResponse> getAsyncFuture();
}
```

## Interceptor 커스터마이징

```java
import com.eraf.http.ErafHttpRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {

    @Bean
    public ErafHttpRequestInterceptor customInterceptor() {
        return (request, headers) -> {
            // 커스텀 헤더 추가
            headers.add("X-Custom-Timestamp", String.valueOf(System.currentTimeMillis()));
            headers.add("X-Tenant-Id", getCurrentTenantId());
        };
    }
}
```

## 에러 처리

```java
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class SafeApiService {

    private final UserClient userClient;

    public UserResponse getUserSafely(Long id) {
        try {
            return userClient.getUser(id);
        } catch (HttpClientErrorException.NotFound e) {
            // 404 처리
            return null;
        } catch (HttpClientErrorException e) {
            // 4xx 에러 처리
            throw new BusinessException("Client error: " + e.getStatusCode());
        } catch (HttpServerErrorException e) {
            // 5xx 에러 처리
            throw new BusinessException("Server error: " + e.getStatusCode());
        }
    }
}
```

## Fallback 패턴

```java
@ErafClient(
    value = "fallback-service",
    circuitBreaker = true
)
public interface FallbackApiClient {

    @GetMapping("/data")
    DataResponse getData();
}

@Component
public class FallbackApiClientFallback implements FallbackApiClient {

    @Override
    public DataResponse getData() {
        // Fallback 응답 반환
        return DataResponse.empty();
    }
}
```

## 타임아웃 설정

```yaml
eraf:
  http:
    connect-timeout: 5000   # 연결 타임아웃 (ms)
    read-timeout: 30000     # 읽기 타임아웃 (ms)
```

```java
@ErafClient(
    value = "slow-service",
    timeout = 60000  # 개별 클라이언트 타임아웃 (60초)
)
public interface SlowApiClient {
    @GetMapping("/slow-operation")
    DataResponse getSlowData();
}
```

## 모범 사례

1. **Service Discovery**: URL 하드코딩 대신 service name 사용
2. **JWT 전파**: @ErafClient 사용으로 자동 JWT 전파 활용
3. **Circuit Breaker**: 불안정한 서비스에는 반드시 활성화
4. **재시도**: Idempotent한 GET 요청에만 재시도 사용
5. **타임아웃**: 적절한 타임아웃으로 리소스 낭비 방지
6. **에러 처리**: 4xx/5xx 에러에 대한 명확한 처리 로직
7. **Fallback**: Circuit Open 시 대체 응답 제공

## 참고

- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Resilience4j](https://resilience4j.readme.io/)
- [WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
