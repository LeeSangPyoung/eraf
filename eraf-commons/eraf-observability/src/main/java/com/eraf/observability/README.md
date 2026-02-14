# ERAF Observability (OpenTelemetry)

OpenTelemetry 기반 분산 추적, 메트릭 수집, 로그 상관관계 기능을 제공합니다.

## 주요 기능

- **분산 추적 (Distributed Tracing)**: 마이크로서비스 간 요청 추적
- **메트릭 수집 (Metrics)**: Counter, Histogram, Gauge 등
- **로그 상관관계 (Log Correlation)**: Trace ID/Span ID 자동 추가
- **자동 계측 (Auto-instrumentation)**: HTTP, DB 쿼리 자동 추적
- **OTLP Export**: Jaeger, Zipkin, Prometheus 등 다양한 백엔드 지원

## 의존성 추가

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-observability</artifactId>
</dependency>
```

## 설정

### 1. 기본 설정

```yaml
eraf:
  observability:
    enabled: true
    service-name: my-service
    service-version: 1.0.0
    environment: production

    tracing:
      enabled: true
      sampling-rate: 1.0  # 100% 샘플링 (운영 환경에서는 0.1 권장)

    metrics:
      enabled: true
      include-jvm: true
      include-http: true

    exporter:
      type: otlp
      endpoint: http://localhost:4317  # OpenTelemetry Collector
      use-grpc: true
```

### 2. Jaeger 사용

```yaml
eraf:
  observability:
    exporter:
      type: otlp
      endpoint: http://jaeger:4317
```

**Jaeger 실행 (Docker):**
```bash
docker run -d --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  jaegertracing/all-in-one:latest
```

**UI 접속:** http://localhost:16686

### 3. 샘플링 설정

```yaml
eraf:
  observability:
    tracing:
      sampling-rate: 0.1  # 10%만 추적 (대용량 트래픽)
```

## 사용 방법

### 1. 자동 계측 (Auto-instrumentation)

Spring Boot 애플리케이션에서 HTTP 요청은 자동으로 추적됩니다.

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // HTTP 요청이 자동으로 Span 생성
        return userService.findById(id);
    }
}
```

**생성되는 Span:**
- Span Name: `GET /users/{id}`
- Attributes: `http.method=GET`, `http.route=/users/{id}`, `http.status_code=200`

### 2. 커스텀 Span 추가

```java
@Service
public class UserService {

    @Autowired
    private TracingUtil tracingUtil;

    public User findById(Long id) {
        // 커스텀 Span 생성
        return tracingUtil.withSpan("UserService.findById", () -> {
            // Span에 속성 추가
            tracingUtil.addAttribute("user.id", id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            // Span에 이벤트 추가
            tracingUtil.addEvent("User found");

            return user;
        });
    }
}
```

### 3. 수동 Span 관리

```java
@Service
public class OrderService {

    @Autowired
    private TracingUtil tracingUtil;

    public void processOrder(Order order) {
        Span span = tracingUtil.startSpan("OrderService.processOrder");
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("order.id", order.getId());
            span.setAttribute("order.amount", order.getAmount());

            // 비즈니스 로직
            validateOrder(order);
            saveOrder(order);
            sendNotification(order);

            span.addEvent("Order processed successfully");

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### 4. 메트릭 수집

```java
@Service
public class PaymentService {

    @Autowired
    private MetricsUtil metricsUtil;

    public void processPayment(Payment payment) {
        long startTime = System.currentTimeMillis();

        try {
            // 결제 처리
            paymentGateway.charge(payment);

            // 성공 카운트
            metricsUtil.incrementCounter("payment.success");

        } catch (Exception e) {
            // 실패 카운트
            metricsUtil.incrementCounter("payment.failure",
                    1,
                    Map.of("error.type", e.getClass().getSimpleName()));
            throw e;

        } finally {
            // 응답 시간 기록
            long duration = System.currentTimeMillis() - startTime;
            metricsUtil.recordHistogram("payment.duration", duration);
        }
    }
}
```

### 5. HTTP 요청 메트릭

```java
@RestController
public class ApiController {

    @Autowired
    private MetricsUtil metricsUtil;

    @GetMapping("/api/data")
    public ResponseEntity<Data> getData() {
        long startTime = System.currentTimeMillis();

        try {
            Data data = dataService.getData();

            // 성공 메트릭
            metricsUtil.incrementRequestCount("GET", "/api/data", 200);

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            // 에러 메트릭
            metricsUtil.incrementRequestCount("GET", "/api/data", 500);
            metricsUtil.incrementErrorCount(e.getClass().getSimpleName());
            throw e;

        } finally {
            // 응답 시간
            long duration = System.currentTimeMillis() - startTime;
            metricsUtil.recordResponseTime("/api/data", duration);
        }
    }
}
```

### 6. Gauge 등록 (관찰 가능한 값)

```java
@Component
public class SystemMetrics {

    @Autowired
    private MetricsUtil metricsUtil;

    private final Queue<Task> taskQueue = new LinkedList<>();

    @PostConstruct
    public void init() {
        // 큐 크기 Gauge 등록
        metricsUtil.registerGauge("task.queue.size",
                () -> (long) taskQueue.size());

        // 메모리 사용량 Gauge
        metricsUtil.registerDoubleGauge("jvm.memory.used",
                () -> {
                    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                    return (double) memoryBean.getHeapMemoryUsage().getUsed();
                });
    }
}
```

### 7. 로그 상관관계

```java
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private TracingUtil tracingUtil;

    public void createOrder(Order order) {
        // Trace ID 가져오기
        String traceId = tracingUtil.getTraceId();
        String spanId = tracingUtil.getSpanId();

        // 로그에 Trace ID 포함
        log.info("Creating order: orderId={}, traceId={}, spanId={}",
                order.getId(), traceId, spanId);

        // MDC에 자동으로 trace_id, span_id 추가됨 (설정 필요)
        log.info("Order created successfully");
    }
}
```

**Logback 설정 (logback-spring.xml):**
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} trace_id=%X{trace_id} span_id=%X{span_id} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

## 분산 추적 예시

### 마이크로서비스 구조

```
Frontend → API Gateway → Order Service → Payment Service → Notification Service
```

### Trace 구조

```
Trace: e7f2a3b1c4d5e6f7
├─ Span: GET /orders (API Gateway)
│  ├─ Span: OrderService.createOrder
│  │  ├─ Span: OrderRepository.save (DB)
│  │  └─ Span: PaymentService.charge (HTTP)
│  │     └─ Span: PaymentGateway.processPayment
│  └─ Span: NotificationService.sendEmail (HTTP)
```

### Order Service 코드

```java
@Service
public class OrderService {

    @Autowired
    private PaymentClient paymentClient;  // Feign Client

    @Autowired
    private TracingUtil tracingUtil;

    public Order createOrder(CreateOrderRequest request) {
        return tracingUtil.withSpan("OrderService.createOrder", () -> {
            // 주문 저장 (DB Span 자동 생성)
            Order order = orderRepository.save(new Order(request));

            tracingUtil.addAttribute("order.id", order.getId());
            tracingUtil.addEvent("Order saved to database");

            // 결제 호출 (HTTP Span 자동 생성 + Trace Context 전파)
            PaymentResult result = paymentClient.charge(order.getPaymentInfo());

            if (result.isSuccess()) {
                order.setStatus(OrderStatus.PAID);
                tracingUtil.addEvent("Payment successful");
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                tracingUtil.setStatus(StatusCode.ERROR, "Payment failed");
            }

            return order;
        });
    }
}
```

## Jaeger UI 사용

### 1. Trace 검색

**Jaeger UI**: http://localhost:16686

- **Service**: `order-service` 선택
- **Operation**: `OrderService.createOrder` 선택
- **Lookback**: Last 1 hour
- **Find Traces** 클릭

### 2. Trace 상세 정보

클릭하면 다음 정보 확인:

- **Duration**: 전체 요청 시간
- **Spans**: 각 구간별 시간
- **Attributes**: 추가한 속성 (order.id 등)
- **Events**: 추가한 이벤트
- **Errors**: 예외 정보

### 3. 성능 병목 찾기

- 가장 오래 걸린 Span 확인
- DB 쿼리, HTTP 호출 시간 분석
- 병렬 처리 가능 여부 확인

## Best Practices

### 1. Span 이름 규칙

```java
// ✅ Good: 명확하고 일관성 있는 이름
tracingUtil.withSpan("UserService.findById", () -> ...);
tracingUtil.withSpan("PaymentGateway.charge", () -> ...);
tracingUtil.withSpan("OrderRepository.save", () -> ...);

// ❌ Bad: 불명확하거나 너무 일반적인 이름
tracingUtil.withSpan("process", () -> ...);
tracingUtil.withSpan("doSomething", () -> ...);
```

### 2. 속성 추가

```java
// ✅ Good: 유용한 메타데이터
span.setAttribute("user.id", userId);
span.setAttribute("order.amount", order.getAmount());
span.setAttribute("http.status_code", 200);

// ❌ Bad: 민감한 정보
span.setAttribute("user.password", password);  // 절대 금지!
span.setAttribute("credit.card.number", cardNumber);
```

### 3. 샘플링

```yaml
# 개발 환경: 100% 샘플링
eraf.observability.tracing.sampling-rate: 1.0

# 운영 환경: 10% 샘플링 (트래픽이 높으면)
eraf.observability.tracing.sampling-rate: 0.1

# 중요한 요청은 항상 추적하도록 커스텀 Sampler 구현 가능
```

### 4. 메트릭 이름 규칙

```java
// ✅ Good: 명확한 네임스페이스와 단위
metricsUtil.incrementCounter("http.server.requests");
metricsUtil.recordHistogram("payment.duration.milliseconds", duration);
metricsUtil.registerGauge("task.queue.size", () -> queueSize);

// ❌ Bad: 불명확한 이름
metricsUtil.incrementCounter("count");
metricsUtil.recordHistogram("time", duration);
```

### 5. 에러 처리

```java
try {
    // 비즈니스 로직
} catch (Exception e) {
    // 예외 기록 (스택 트레이스 포함)
    tracingUtil.recordException(e);

    // 에러 메트릭
    metricsUtil.incrementErrorCount(e.getClass().getSimpleName());

    throw e;
}
```

## 성능 영향

### Overhead

- **Tracing**: 요청당 < 1ms (샘플링 시)
- **Metrics**: 거의 없음 (in-memory)
- **Network**: OTLP Exporter는 배치 전송으로 최소화

### 최적화

1. **샘플링 사용**: 운영 환경에서 10% 샘플링
2. **배치 전송**: 기본 설정으로 1초마다 배치 전송
3. **필요한 Span만**: 중요한 구간에만 커스텀 Span 추가

## 트러블슈팅

### 문제: Trace가 표시되지 않음

**원인**: Exporter 설정 오류

**해결**:
```bash
# OpenTelemetry Collector 확인
docker logs otel-collector

# 엔드포인트 확인
curl http://localhost:4317
```

### 문제: Trace가 끊김 (마이크로서비스 간)

**원인**: Trace Context 전파 실패

**해결**: Feign Client에 OpenTelemetry Interceptor 추가
```yaml
# 자동 설정 활성화
spring:
  sleuth:
    propagation:
      type: W3C  # W3C Trace Context 사용
```

### 문제: 성능 저하

**원인**: 100% 샘플링

**해결**: 샘플링 비율 조정
```yaml
eraf.observability.tracing.sampling-rate: 0.1  # 10%로 감소
```

## 참고 자료

- [OpenTelemetry 공식 문서](https://opentelemetry.io/docs/)
- [Jaeger 문서](https://www.jaegertracing.io/docs/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)
