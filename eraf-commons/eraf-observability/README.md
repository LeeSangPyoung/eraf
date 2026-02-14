# ERAF Observability

OpenTelemetry 기반의 분산 추적(Distributed Tracing) 및 메트릭(Metrics) 모듈입니다.

## 기능

- **분산 추적 (Distributed Tracing)**: OpenTelemetry를 사용한 요청 추적
- **메트릭 수집 (Metrics)**: Counter, Histogram, Gauge, UpDownCounter 지원
- **OTLP 내보내기**: Jaeger, Zipkin 등 OTLP 호환 백엔드로 전송
- **간편한 API**: TracingUtil, MetricsUtil 유틸리티 제공

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-observability</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
eraf:
  observability:
    enabled: true
    service-name: my-service
    service-version: 1.0.0
    environment: production

    tracing:
      enabled: true
      sampling-rate: 1.0  # 0.0 ~ 1.0 (1.0 = 100% 샘플링)

    metrics:
      enabled: true

    exporter:
      endpoint: http://localhost:4317  # OTLP gRPC endpoint
      timeout-ms: 10000
```

## 사용법

### 1. TracingUtil - 분산 추적

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final TracingUtil tracingUtil;

    public User getUser(Long userId) {
        // Span 자동 생성 및 관리
        return tracingUtil.withSpan("getUserById", () -> {
            // 비즈니스 로직
            tracingUtil.setAttribute("user.id", userId.toString());
            User user = userRepository.findById(userId).orElseThrow();

            // 이벤트 기록
            tracingUtil.addEvent("user_loaded");

            return user;
        });
    }

    public void updateUser(User user) {
        tracingUtil.withSpan("updateUser",
            () -> userRepository.save(user),
            Map.of(
                "user.id", user.getId().toString(),
                "user.email", user.getEmail()
            )
        );
    }
}
```

### 2. MetricsUtil - 메트릭 수집

```java
@RestController
@RequiredArgsConstructor
public class ApiController {

    private final MetricsUtil metricsUtil;

    @GetMapping("/api/users")
    public List<User> getUsers() {
        long startTime = System.currentTimeMillis();

        try {
            List<User> users = userService.findAll();

            // HTTP 요청 카운트 증가
            metricsUtil.incrementRequestCount("GET", "/api/users", 200);

            // 응답 시간 기록
            long duration = System.currentTimeMillis() - startTime;
            metricsUtil.recordResponseTime("/api/users", duration);

            return users;
        } catch (Exception e) {
            metricsUtil.incrementErrorCount(e.getClass().getSimpleName(), "/api/users");
            throw e;
        }
    }
}
```

### 3. Gauge 등록 - 시스템 메트릭

```java
@Configuration
public class MetricsConfig {

    @Bean
    public ApplicationRunner registerGauges(MetricsUtil metricsUtil) {
        return args -> {
            // CPU 사용률
            metricsUtil.registerDoubleGauge("system.cpu.usage",
                () -> osBean.getSystemCpuLoad());

            // 메모리 사용량
            metricsUtil.registerGauge("jvm.memory.used",
                () -> Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

            // 활성 스레드 수
            metricsUtil.registerGauge("jvm.threads.active",
                () -> (long) Thread.activeCount());
        };
    }
}
```

## 메트릭 타입

### Counter (증가만 가능)
```java
metricsUtil.incrementCounter("http.requests");
metricsUtil.incrementCounter("http.requests", 1, Map.of("method", "GET"));
```

### Histogram (분포 측정)
```java
metricsUtil.recordHistogram("http.duration", 123.45);
metricsUtil.recordHistogram("request.size", 1024.0, Map.of("endpoint", "/api"));
```

### Gauge (관찰 가능한 값)
```java
metricsUtil.registerGauge("queue.size", () -> queue.size());
metricsUtil.registerDoubleGauge("cpu.usage", () -> getCpuUsage());
```

### UpDownCounter (증가/감소 가능)
```java
metricsUtil.addUpDownCounter("active.connections", 1);  // 증가
metricsUtil.addUpDownCounter("active.connections", -1); // 감소
```

## Jaeger 연동 예시

### Docker Compose

```yaml
services:
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"  # Jaeger UI
      - "4317:4317"    # OTLP gRPC
      - "4318:4318"    # OTLP HTTP
    environment:
      - COLLECTOR_OTLP_ENABLED=true
```

### 설정

```yaml
eraf:
  observability:
    exporter:
      endpoint: http://localhost:4317
```

Jaeger UI: http://localhost:16686

## 참고

- [OpenTelemetry 공식 문서](https://opentelemetry.io/docs/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [OTLP Specification](https://opentelemetry.io/docs/specs/otlp/)
