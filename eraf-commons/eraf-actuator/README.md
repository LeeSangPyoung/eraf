# ERAF Actuator

Spring Boot Actuator 기반의 헬스 체크, 메트릭 수집, 분산 추적 기능을 제공하는 모듈입니다. 프로덕션 환경에서 애플리케이션의 상태를 모니터링하고 성능을 측정하며 문제를 진단할 수 있습니다.

## 주요 기능

- **헬스 체크**: Database, Redis, Kafka 등 외부 시스템 상태 모니터링
- **커스텀 메트릭**: @Counted, @Timed 애노테이션으로 비즈니스 메트릭 수집
- **분산 추적**: @Traced 애노테이션으로 분산 환경의 요청 추적
- **Prometheus 연동**: Micrometer 기반 메트릭을 Prometheus로 Export
- **비즈니스 메트릭**: 주문, 결제 등 비즈니스 도메인 메트릭 수집
- **자동 설정**: Spring Boot AutoConfiguration으로 간편한 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-actuator</artifactId>
</dependency>

<!-- Prometheus 사용 시 -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 1. 헬스 체크 (Health Indicators)

### 기본 헬스 엔드포인트

Spring Boot Actuator의 기본 헬스 체크 엔드포인트를 사용합니다.

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus  # 노출할 엔드포인트
  endpoint:
    health:
      show-details: always  # 헬스 체크 상세 정보 표시
      probes:
        enabled: true       # Liveness/Readiness Probe 활성화

# ERAF Actuator 설정
eraf:
  actuator:
    health-enabled: true
    application-name: my-service
```

### 헬스 체크 조회

```bash
# 전체 헬스 상태
curl http://localhost:8080/actuator/health

# Liveness Probe (프로세스가 살아있는지)
curl http://localhost:8080/actuator/health/liveness

# Readiness Probe (트래픽 받을 준비가 되었는지)
curl http://localhost:8080/actuator/health/readiness
```

**응답 예제**:
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "15.3",
        "url": "jdbc:postgresql://localhost:5432/mydb"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.11",
        "mode": "standalone"
      }
    },
    "kafka": {
      "status": "UP",
      "details": {
        "clusterId": "abc123",
        "nodes": 3
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 100000000000,
        "threshold": 10485760
      }
    }
  }
}
```

---

## 2. 커스텀 헬스 인디케이터

### DatabaseHealthIndicator

데이터베이스 연결 상태 확인:

```java
import com.eraf.actuator.health.DatabaseHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class HealthConfig {

    @Bean
    public DatabaseHealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return new DatabaseHealthIndicator(dataSource, "SELECT 1");
    }

    // PostgreSQL일 경우
    @Bean
    public DatabaseHealthIndicator postgresHealthIndicator(DataSource dataSource) {
        return new DatabaseHealthIndicator(dataSource, "SELECT 1");
    }

    // Oracle일 경우
    @Bean
    public DatabaseHealthIndicator oracleHealthIndicator(DataSource dataSource) {
        return new DatabaseHealthIndicator(dataSource, "SELECT 1 FROM DUAL");
    }
}
```

### RedisHealthIndicator

Redis 연결 상태 확인 (자동 등록):

```yaml
eraf:
  actuator:
    health:
      redis:
        enabled: true      # Redis Health Indicator 활성화
        timeout-ms: 5000   # 타임아웃 (밀리초)
```

### KafkaHealthIndicator

Kafka 클러스터 상태 확인 (자동 등록):

```yaml
eraf:
  actuator:
    health:
      kafka:
        enabled: true      # Kafka Health Indicator 활성화
        timeout-ms: 5000   # 타임아웃 (밀리초)
```

### 커스텀 헬스 인디케이터 작성

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("externalApi")
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.example.com/health";

    public ExternalApiHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Health.up()
                    .withDetail("url", apiUrl)
                    .withDetail("status", response.getStatusCode())
                    .withDetail("responseTime", "200ms")
                    .build();
            } else {
                return Health.down()
                    .withDetail("url", apiUrl)
                    .withDetail("status", response.getStatusCode())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("url", apiUrl)
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 3. 메트릭 수집 (Metrics)

### @Counted - 호출 횟수 카운트

메서드 호출 횟수를 자동으로 카운트합니다.

```java
import com.eraf.actuator.metrics.Counted;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Counted(
        value = "orders.created",
        description = "생성된 주문 수",
        extraTags = {"type=online"}
    )
    public Order createOrder(OrderRequest request) {
        // 주문 생성 로직
        return orderRepository.save(order);
    }

    @Counted(
        value = "orders.cancelled",
        description = "취소된 주문 수",
        recordOnException = false  // 예외 시 카운트 안 함
    )
    public void cancelOrder(Long orderId) {
        // 주문 취소 로직
        orderRepository.deleteById(orderId);
    }
}
```

### @Timed - 실행 시간 측정

메서드 실행 시간을 자동으로 측정합니다.

```java
import com.eraf.actuator.metrics.Timed;

@Service
public class PaymentService {

    @Timed(
        value = "payment.process",
        description = "결제 처리 시간",
        histogram = true,                    // 히스토그램 활성화
        percentiles = {0.5, 0.9, 0.95, 0.99} // 50%, 90%, 95%, 99% 퍼센타일
    )
    public PaymentResult processPayment(PaymentRequest request) {
        // 결제 처리 (시간 측정됨)
        return paymentGateway.charge(request);
    }

    @Timed(
        value = "payment.refund",
        extraTags = {"method=credit_card"}
    )
    public void refund(Long paymentId) {
        // 환불 처리
        paymentRepository.refund(paymentId);
    }
}
```

### @Counted + @Timed 동시 사용

```java
@Service
public class UserService {

    @Counted(value = "users.created")
    @Timed(value = "users.create.time", histogram = true)
    public User createUser(UserRequest request) {
        // 호출 횟수와 실행 시간을 모두 수집
        return userRepository.save(new User(request));
    }
}
```

---

## 4. 비즈니스 메트릭 (BusinessMetrics)

프로그래밍 방식으로 커스텀 메트릭을 수집합니다.

```java
import com.eraf.actuator.metrics.BusinessMetrics;
import org.springframework.stereotype.Service;

@Service
public class OrderMetricsService {

    private final BusinessMetrics metrics;

    public OrderMetricsService(BusinessMetrics metrics) {
        this.metrics = metrics;
    }

    public void recordOrderCreated(Order order) {
        // 카운터 증가
        metrics.increment("orders.created",
            "status", order.getStatus(),
            "type", order.getType());
    }

    public void recordOrderAmount(Order order) {
        // 주문 금액 기록
        metrics.recordAmount("orders.amount",
            order.getTotalAmount(),
            order.getCurrency(),
            "type", order.getType());
    }

    public void recordPaymentResult(boolean success) {
        // 성공/실패 기록
        metrics.recordOutcome("payments.result", success);
    }

    public void recordProcessingTime(long durationMs) {
        // 처리 시간 기록
        metrics.recordTime("orders.processing.time",
            durationMs,
            TimeUnit.MILLISECONDS,
            "step", "validation");
    }

    public void trackInventory(Supplier<Integer> inventorySupplier) {
        // 게이지 등록 (실시간 재고 수량)
        metrics.gauge("inventory.quantity", inventorySupplier,
            "product", "product-123");
    }
}
```

### BusinessMetrics API

| 메서드 | 설명 | 예제 |
|--------|------|------|
| `increment(name, tags...)` | 카운터 증가 | `increment("user.login", "status", "success")` |
| `increment(name, amount, tags...)` | 특정 값만큼 증가 | `increment("sales", 1500.0, "currency", "USD")` |
| `gauge(name, supplier, tags...)` | 게이지 등록 (실시간 값) | `gauge("queue.size", queue::size)` |
| `recordTime(name, duration, unit, tags...)` | 시간 기록 | `recordTime("api.latency", 200, MILLISECONDS)` |
| `recordTime(name, supplier, tags...)` | 코드 블록 시간 측정 | `recordTime("db.query", () -> repo.findAll())` |
| `recordDistribution(name, value, tags...)` | 분포 요약 기록 | `recordDistribution("response.size", 1024.0)` |
| `recordAmount(name, amount, currency, tags...)` | 금액 기록 | `recordAmount("revenue", 50000, "USD")` |
| `recordOutcome(name, success, tags...)` | 성공/실패 기록 | `recordOutcome("order.create", true)` |

### 실전 예제

```java
@Service
public class OrderService {

    private final BusinessMetrics metrics;
    private final OrderRepository orderRepository;

    // 주문 처리 전체 메트릭
    public Order processOrder(OrderRequest request) {
        // 시간 측정 + 결과 반환
        return metrics.recordTime("order.process", () -> {
            try {
                // 주문 생성
                Order order = createOrder(request);

                // 주문 생성 카운트
                metrics.increment("order.created",
                    "status", "success",
                    "type", order.getType());

                // 주문 금액 기록
                metrics.recordAmount("order.amount",
                    order.getTotalAmount(),
                    order.getCurrency());

                // 성공 기록
                metrics.recordOutcome("order.result", true);

                return order;

            } catch (Exception e) {
                // 실패 카운트
                metrics.increment("order.created",
                    "status", "failed",
                    "error", e.getClass().getSimpleName());

                // 실패 기록
                metrics.recordOutcome("order.result", false);

                throw e;
            }
        });
    }

    // 실시간 게이지 등록
    @PostConstruct
    public void registerGauges() {
        // 대기 중인 주문 수
        metrics.gauge("orders.pending",
            () -> orderRepository.countByStatus("PENDING"));

        // 처리 중인 주문 수
        metrics.gauge("orders.processing",
            () -> orderRepository.countByStatus("PROCESSING"));
    }
}
```

---

## 5. 분산 추적 (Distributed Tracing)

### @Traced 애노테이션

메서드 실행을 추적하여 분산 환경에서 요청 흐름을 파악합니다.

```java
import com.eraf.actuator.tracing.Traced;

@Service
public class OrderService {

    @Traced(
        value = "orderService.createOrder",
        tags = {"operation=create"},
        logParameters = true,   // 파라미터 로깅
        logResult = true        // 결과 로깅
    )
    public Order createOrder(OrderRequest request) {
        // 이 메서드 실행이 새 Span으로 추적됨
        return orderRepository.save(order);
    }

    @Traced("orderService.findOrder")
    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
```

### TraceContext 사용

프로그래밍 방식으로 추적 정보를 관리합니다.

```java
import com.eraf.actuator.tracing.TraceContext;
import com.eraf.actuator.tracing.TraceContextHolder;

@Service
public class PaymentService {

    public void processPayment(PaymentRequest request) {
        // 현재 Trace ID 조회
        String traceId = TraceContextHolder.getTraceId();
        String spanId = TraceContextHolder.getSpanId();

        log.info("Processing payment - traceId: {}, spanId: {}", traceId, spanId);

        // Trace Context에 커스텀 속성 추가
        TraceContextHolder.setAttribute("userId", request.getUserId());
        TraceContextHolder.setAttribute("paymentMethod", request.getMethod());

        // 결제 처리...
    }

    public void externalApiCall() {
        // 외부 API 호출 시 Trace ID 전파
        String traceId = TraceContextHolder.getTraceId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Trace-ID", traceId);  // 전파

        restTemplate.exchange(url, HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ResponseType.class);
    }
}
```

### TracingFilter 자동 동작

HTTP 요청마다 자동으로 Trace Context를 생성하고 관리합니다.

```
[요청]
  → TracingFilter (Trace ID, Span ID 생성)
  → Controller (@Traced)
    → Service (@Traced)
      → Repository
    ← Service
  ← Controller
← TracingFilter (Context 정리)
```

**로그 예제**:
```
[INFO ] traceId=abc123 spanId=span-1 - OrderController.createOrder started
[INFO ] traceId=abc123 spanId=span-2 - OrderService.createOrder started
[INFO ] traceId=abc123 spanId=span-2 - OrderService.createOrder completed in 150ms
[INFO ] traceId=abc123 spanId=span-1 - OrderController.createOrder completed in 200ms
```

---

## 6. Prometheus 연동

### Prometheus Export 설정

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: prometheus  # Prometheus 엔드포인트 노출
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active:default}
```

### Prometheus Scrape 설정

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:8080']
        labels:
          application: 'my-service'
          environment: 'production'
```

### Prometheus 메트릭 조회

```bash
# Prometheus 엔드포인트
curl http://localhost:8080/actuator/prometheus

# 응답 예제 (Prometheus Format)
# HELP orders_created_total 생성된 주문 수
# TYPE orders_created_total counter
orders_created_total{status="success",type="online",} 1523.0

# HELP payment_process_seconds 결제 처리 시간
# TYPE payment_process_seconds summary
payment_process_seconds_count 245.0
payment_process_seconds_sum 12.5
payment_process_seconds{quantile="0.5",} 0.05
payment_process_seconds{quantile="0.9",} 0.12
payment_process_seconds{quantile="0.95",} 0.18
payment_process_seconds{quantile="0.99",} 0.25
```

---

## 7. Grafana 대시보드

### Prometheus 데이터소스 연결

Grafana에서 Prometheus를 데이터소스로 추가:

```
Configuration > Data Sources > Add data source
  - Type: Prometheus
  - URL: http://prometheus:9090
  - Save & Test
```

### PromQL 쿼리 예제

```promql
# 초당 주문 생성 수
rate(orders_created_total[5m])

# 결제 처리 시간 95% 퍼센타일
payment_process_seconds{quantile="0.95"}

# 평균 응답 시간 (최근 5분)
rate(payment_process_seconds_sum[5m]) / rate(payment_process_seconds_count[5m])

# 성공률 계산
sum(rate(orders_created_total{status="success"}[5m])) /
sum(rate(orders_created_total[5m])) * 100

# 대기 중인 주문 수 (게이지)
orders_pending
```

### 대시보드 패널 구성 예제

1. **주문 생성 추이** (Graph)
   - Query: `rate(orders_created_total[5m])`
   - Legend: `{{status}} - {{type}}`

2. **결제 처리 시간 분포** (Heatmap)
   - Query: `payment_process_seconds`
   - Format: Heatmap

3. **성공률** (Gauge)
   - Query: `sum(rate(orders_created_total{status="success"}[5m])) / sum(rate(orders_created_total[5m])) * 100`
   - Unit: Percent (0-100)

4. **시스템 헬스** (Stat)
   - Query: `up{job="spring-boot-app"}`
   - Thresholds: 0 (red), 1 (green)

---

## 8. 설정 방법

### application.yml 전체 설정

```yaml
spring:
  application:
    name: my-service

# Spring Boot Actuator
management:
  endpoints:
    web:
      base-path: /actuator
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always        # 헬스 상세 정보 표시
      probes:
        enabled: true             # Liveness/Readiness Probe
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:local}
    distribution:
      percentiles-histogram:
        http.server.requests: true  # HTTP 요청 히스토그램

# ERAF Actuator
eraf:
  actuator:
    health-enabled: true
    metrics-enabled: true
    application-name: ${spring.application.name}

    # 헬스 인디케이터 설정
    health:
      database:
        enabled: true
        timeout-ms: 5000
      redis:
        enabled: true
        timeout-ms: 3000
      kafka:
        enabled: true
        timeout-ms: 5000

  # 메트릭 설정
  metrics:
    prefix: ${spring.application.name}  # 메트릭 이름 접두사
    common-tags:
      service: order-service
      region: ap-northeast-2

  # 추적 설정
  tracing:
    enabled: true
    sample-rate: 1.0  # 샘플링 비율 (0.0 ~ 1.0)
```

---

## 9. 실전 예제

### 완전한 서비스 모니터링 구현

```java
import com.eraf.actuator.metrics.BusinessMetrics;
import com.eraf.actuator.metrics.Counted;
import com.eraf.actuator.metrics.Timed;
import com.eraf.actuator.tracing.Traced;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final BusinessMetrics metrics;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderService(BusinessMetrics metrics,
                        OrderRepository orderRepository,
                        PaymentService paymentService) {
        this.metrics = metrics;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    @Counted(value = "orders.created", description = "생성된 주문 수")
    @Timed(value = "orders.create.time", histogram = true, percentiles = {0.95, 0.99})
    @Traced(value = "OrderService.createOrder", logParameters = true)
    public Order createOrder(OrderRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 주문 생성
            Order order = new Order();
            order.setUserId(request.getUserId());
            order.setItems(request.getItems());
            order.calculateTotalAmount();

            // 2. 주문 저장
            order = orderRepository.save(order);

            // 3. 결제 처리
            PaymentResult paymentResult = paymentService.processPayment(order);

            if (paymentResult.isSuccess()) {
                order.setStatus(OrderStatus.PAID);

                // 성공 메트릭 기록
                metrics.recordOutcome("order.payment", true);
                metrics.recordAmount("order.revenue",
                    order.getTotalAmount(),
                    order.getCurrency(),
                    "status", "success");

            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);

                // 실패 메트릭 기록
                metrics.recordOutcome("order.payment", false);
                metrics.increment("order.payment.failed",
                    "reason", paymentResult.getFailureReason());
            }

            orderRepository.save(order);

            // 처리 시간 기록
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordTime("order.total.time", duration, TimeUnit.MILLISECONDS,
                "status", order.getStatus().name());

            return order;

        } catch (Exception e) {
            // 에러 메트릭 기록
            metrics.increment("order.errors",
                "error", e.getClass().getSimpleName());
            throw e;
        }
    }

    @Timed(value = "orders.find.time")
    @Traced("OrderService.findOrder")
    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> {
                metrics.increment("order.notfound");
                return new NotFoundException("Order not found: " + orderId);
            });
    }

    @Counted(value = "orders.cancelled")
    @Traced("OrderService.cancelOrder")
    public void cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 취소 메트릭 기록
        metrics.increment("order.status.changed",
            "from", "PENDING",
            "to", "CANCELLED");
    }

    // 실시간 메트릭 등록
    @PostConstruct
    public void registerMetrics() {
        // 대기 중인 주문 수
        metrics.gauge("orders.pending",
            () -> orderRepository.countByStatus(OrderStatus.PENDING));

        // 처리 중인 주문 수
        metrics.gauge("orders.processing",
            () -> orderRepository.countByStatus(OrderStatus.PROCESSING));

        // 오늘의 총 주문 수
        metrics.gauge("orders.today",
            () -> orderRepository.countCreatedToday());
    }
}
```

### 커스텀 헬스 인디케이터 - 외부 결제 시스템

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("paymentGateway")
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String healthCheckUrl;

    public PaymentGatewayHealthIndicator(RestTemplate restTemplate,
                                          @Value("${payment.gateway.health-url}") String healthCheckUrl) {
        this.restTemplate = restTemplate;
        this.healthCheckUrl = healthCheckUrl;
    }

    @Override
    public Health health() {
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<PaymentHealthResponse> response =
                restTemplate.getForEntity(healthCheckUrl, PaymentHealthResponse.class);

            long responseTime = System.currentTimeMillis() - startTime;

            if (response.getStatusCode() == HttpStatus.OK &&
                response.getBody() != null &&
                response.getBody().isOperational()) {

                return Health.up()
                    .withDetail("gateway", "Stripe")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("status", "operational")
                    .withDetail("lastChecked", Instant.now())
                    .build();
            } else {
                return Health.down()
                    .withDetail("gateway", "Stripe")
                    .withDetail("status", "degraded")
                    .withDetail("message", response.getBody().getMessage())
                    .build();
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            return Health.down()
                .withDetail("gateway", "Stripe")
                .withDetail("error", e.getClass().getSimpleName())
                .withDetail("message", e.getMessage())
                .withDetail("responseTime", responseTime + "ms")
                .build();
        }
    }
}
```

---

## 10. Kubernetes 통합

### Liveness/Readiness Probe 설정

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    spec:
      containers:
      - name: order-service
        image: order-service:1.0.0
        ports:
        - containerPort: 8080

        # Liveness Probe - 컨테이너가 살아있는지 확인
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        # Readiness Probe - 트래픽 받을 준비가 되었는지 확인
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
```

### Prometheus ServiceMonitor

```yaml
# servicemonitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: order-service
  labels:
    app: order-service
spec:
  selector:
    matchLabels:
      app: order-service
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

---

## 11. 모범 사례

### 1. 메트릭 네이밍

```java
// 좋은 예 - 명확하고 일관성 있는 네이밍
metrics.increment("order.created.success");
metrics.increment("order.created.failed");
metrics.recordTime("order.processing.time");

// 나쁜 예 - 불명확하고 일관성 없음
metrics.increment("order");
metrics.increment("createOrderFail");
metrics.recordTime("time");
```

### 2. 태그 활용

```java
// 좋은 예 - 태그로 메트릭 분류
metrics.increment("order.created",
    "status", "success",
    "type", "online",
    "payment_method", "credit_card");

// 나쁜 예 - 메트릭 이름에 모든 정보 포함
metrics.increment("order.created.success.online.creditcard");
```

### 3. 게이지 vs 카운터

```java
// 카운터: 누적 값 (계속 증가)
metrics.increment("orders.created");  // 총 주문 수

// 게이지: 현재 값 (증가/감소 모두 가능)
metrics.gauge("orders.pending", queue::size);  // 현재 대기 중인 주문 수
```

### 4. 헬스 체크 타임아웃

```yaml
# 외부 시스템 헬스 체크는 타임아웃 설정 필수
eraf:
  actuator:
    health:
      redis:
        enabled: true
        timeout-ms: 3000  # 3초 이내 응답 없으면 DOWN
```

### 5. 샘플링

```yaml
# 트래픽이 높은 환경에서는 샘플링 비율 조정
eraf:
  tracing:
    sample-rate: 0.1  # 10%만 추적 (성능 최적화)
```

---

## 12. 문제 해결

### 메트릭이 수집되지 않음

**문제**: @Counted, @Timed가 작동하지 않음

**해결책**:
1. `@EnableAspectJAutoProxy` 확인
2. Bean으로 등록된 클래스인지 확인
3. AOP Proxy 문제 (self-invocation)

```java
// 나쁜 예 - self-invocation (AOP 미작동)
@Service
public class OrderService {
    public void createOrder() {
        this.internalMethod();  // AOP가 적용되지 않음
    }

    @Counted
    private void internalMethod() { }
}

// 좋은 예 - 별도 Bean 호출
@Service
public class OrderService {
    @Autowired
    private OrderHelper helper;

    public void createOrder() {
        helper.internalMethod();  // AOP 정상 작동
    }
}

@Component
class OrderHelper {
    @Counted
    public void internalMethod() { }
}
```

### Prometheus 엔드포인트 404

**문제**: `/actuator/prometheus` 404 오류

**해결책**:
1. `micrometer-registry-prometheus` 의존성 추가
2. `management.endpoints.web.exposure.include=prometheus` 설정 확인

### 헬스 체크 타임아웃

**문제**: 헬스 체크가 너무 오래 걸림

**해결책**:
```yaml
eraf:
  actuator:
    health:
      database:
        timeout-ms: 3000  # 타임아웃 단축
```

---

## 참고 자료

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [eraf-observability Documentation](../eraf-observability/) - OpenTelemetry 통합

---

## 13. Phase 2: Advanced Health & Metrics (신규 기능)

Phase 2에서는 Kubernetes 네이티브 헬스 체크 및 고급 메트릭 수집 기능이 추가되었습니다.

### 13.1. Liveness & Readiness Health Indicators

Kubernetes의 Liveness와 Readiness Probe를 위한 전용 Health Indicator입니다.

#### LivenessHealthIndicator

애플리케이션 프로세스가 살아있는지 확인합니다. 장애 시 Pod를 재시작합니다.

```java
import com.eraf.actuator.health.LivenessHealthIndicator;

@Configuration
public class HealthConfig {

    @Bean
    public LivenessHealthIndicator livenessHealthIndicator() {
        LivenessHealthIndicator indicator = new LivenessHealthIndicator();

        // 애플리케이션 시작 완료 시 호출
        indicator.markAsReady();

        // 치명적인 에러 발생 시 호출 (Pod 재시작 유도)
        // indicator.markAsDown();

        return indicator;
    }
}
```

**사용 예제**:
```java
@Service
public class ApplicationStartupListener {

    private final LivenessHealthIndicator livenessHealthIndicator;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 애플리케이션 준비 완료
        livenessHealthIndicator.markAsReady();
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        // 애플리케이션 종료 중
        livenessHealthIndicator.markAsDown();
    }
}
```

**엔드포인트**:
```bash
curl http://localhost:8080/actuator/health/liveness

# 응답 (UP):
{
  "status": "UP",
  "details": {
    "application": "READY"
  }
}
```

#### ReadinessHealthIndicator

애플리케이션이 트래픽을 받을 준비가 되었는지 확인합니다. 준비되지 않으면 트래픽을 차단합니다.

```java
import com.eraf.actuator.health.ReadinessHealthIndicator;

@Configuration
public class HealthConfig {

    @Bean
    public ReadinessHealthIndicator readinessHealthIndicator() {
        ReadinessHealthIndicator indicator = new ReadinessHealthIndicator();

        // 의존성 추가 (데이터베이스, 캐시 등)
        indicator.addDependency("database", true);
        indicator.addDependency("redis", true);
        indicator.addDependency("kafka", false);  // 선택적 의존성

        return indicator;
    }
}
```

**동적 의존성 관리**:
```java
@Service
public class DependencyMonitor {

    private final ReadinessHealthIndicator readinessHealthIndicator;

    @Scheduled(fixedDelay = 10000)  // 10초마다 체크
    public void checkDependencies() {
        // 데이터베이스 체크
        boolean dbReady = checkDatabase();
        readinessHealthIndicator.addDependency("database", dbReady);

        // Redis 체크
        boolean redisReady = checkRedis();
        readinessHealthIndicator.addDependency("redis", redisReady);
    }

    public void onDatabaseConnectionFailed() {
        // DB 연결 실패 시 즉시 Readiness를 DOWN으로 변경
        readinessHealthIndicator.addDependency("database", false);
    }

    public void onDatabaseConnectionRecovered() {
        // DB 연결 복구 시 Readiness를 UP으로 변경
        readinessHealthIndicator.addDependency("database", true);
    }
}
```

**엔드포인트**:
```bash
curl http://localhost:8080/actuator/health/readiness

# 응답 (UP):
{
  "status": "UP",
  "details": {
    "readinessState": "ACCEPTING_TRAFFIC",
    "dependencies": {
      "database": true,
      "redis": true,
      "kafka": false
    }
  }
}

# 응답 (DOWN - 의존성 실패):
{
  "status": "DOWN",
  "details": {
    "readinessState": "REFUSING_TRAFFIC",
    "dependencies": {
      "database": false,
      "redis": true,
      "kafka": false
    },
    "reason": "Required dependency 'database' is not ready"
  }
}
```

#### 설정

```yaml
eraf:
  actuator:
    health:
      liveness:
        enabled: true
      readiness:
        enabled: true
        startup-delay-seconds: 10  # 시작 후 10초 대기

management:
  endpoint:
    health:
      probes:
        enabled: true  # Liveness/Readiness probe 활성화
```

### 13.2. CustomMetrics - 고급 메트릭 수집

메트릭 인스턴스 재사용으로 성능을 최적화한 고급 메트릭 수집 API입니다.

```java
import com.eraf.actuator.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

@Service
public class OrderService {

    private final CustomMetrics customMetrics;

    public OrderService(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }

    public void processOrder(Order order) {
        // Timer 재사용 - 동일한 이름/태그로 여러 번 호출해도 하나의 인스턴스만 생성
        Timer timer = customMetrics.timer("order.process.time", "type", order.getType());
        timer.record(() -> {
            // 주문 처리 로직
            processOrderInternal(order);
        });

        // Counter 재사용
        Counter counter = customMetrics.counter("order.processed", "status", "success");
        counter.increment();
    }

    public void trackOrderAmount(Order order) {
        // DistributionSummary로 금액 분포 추적
        customMetrics.distributionSummary("order.amount", "currency", order.getCurrency())
                .record(order.getTotalAmount());
    }

    public void trackWithSLA() {
        // SLA (Service Level Agreement) 기반 Timer
        Timer slaTimer = customMetrics.slaTimer("api.response.time",
                Duration.ofMillis(100),   // 100ms
                Duration.ofMillis(500),   // 500ms
                Duration.ofSeconds(1));    // 1s

        slaTimer.record(Duration.ofMillis(250));
        // 100ms-500ms bucket에 기록됨
    }
}
```

**FunctionTimer - 누적 통계 추적**:
```java
@Service
public class MetricsService {

    private final CustomMetrics customMetrics;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);

    @PostConstruct
    public void registerFunctionTimer() {
        // 누적된 호출 횟수와 총 실행 시간을 추적
        customMetrics.functionTimer(
                "requests.total",
                this,
                service -> service.totalRequests.get(),     // 총 호출 수
                service -> service.totalDuration.get(),     // 총 실행 시간 (나노초)
                "endpoint", "/api/orders"
        );
    }

    public void recordRequest(long durationNanos) {
        totalRequests.incrementAndGet();
        totalDuration.addAndGet(durationNanos);
    }
}
```

### 13.3. CacheMetrics - 캐시 성능 메트릭

캐시 Hit/Miss 비율 및 성능을 추적합니다.

```java
import com.eraf.actuator.metrics.CacheMetrics;

@Service
public class CacheService {

    private final CacheMetrics cacheMetrics;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public CacheService(CacheMetrics cacheMetrics) {
        this.cacheMetrics = cacheMetrics;

        // 캐시 등록 (메트릭 초기화)
        cacheMetrics.registerCache("userCache");
    }

    public User getUser(String userId) {
        User user = cache.get(userId);

        if (user != null) {
            // Cache Hit
            cacheMetrics.recordHit("userCache");
            return user;
        } else {
            // Cache Miss
            cacheMetrics.recordMiss("userCache");

            // DB에서 로드
            Timer.Sample sample = cacheMetrics.startLoad("userCache");
            try {
                user = loadUserFromDatabase(userId);
                cache.put(userId, user);
                cacheMetrics.recordPut("userCache");

                cacheMetrics.stopLoad(sample, "userCache", true);
            } catch (Exception e) {
                cacheMetrics.stopLoad(sample, "userCache", false);
                throw e;
            }

            return user;
        }
    }

    public void evictUser(String userId) {
        cache.remove(userId);
        cacheMetrics.recordEviction("userCache");
    }

    // 캐시 크기 업데이트
    @Scheduled(fixedDelay = 5000)
    public void updateCacheSize() {
        cacheMetrics.setSize("userCache", cache.size());
    }
}
```

**수집되는 메트릭**:
- `cache.gets` (tag: result=hit/miss) - Hit/Miss 카운트
- `cache.puts` - Put 카운트
- `cache.evictions` - Eviction 카운트
- `cache.size` - 현재 캐시 크기
- `cache.hit.ratio` - Hit 비율 (0.0 ~ 1.0)
- `cache.load.time` - 캐시 로드 시간

**Prometheus 쿼리**:
```promql
# Hit 비율 (%)
cache_hit_ratio{cache="userCache"} * 100

# 초당 Cache Miss 수
rate(cache_gets_total{cache="userCache",result="miss"}[5m])

# 평균 캐시 로드 시간
rate(cache_load_time_seconds_sum[5m]) / rate(cache_load_time_seconds_count[5m])
```

### 13.4. ApiMetrics - API 호출 메트릭

API 엔드포인트별 호출 횟수, 응답 시간, 에러율을 추적합니다.

```java
import com.eraf.actuator.metrics.ApiMetrics;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ApiMetrics apiMetrics;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        ApiMetrics.ApiCallTimer timer = apiMetrics.start("/api/orders", "POST");

        try {
            Order order = orderService.createOrder(request);

            // 성공 기록
            long duration = timer.stop(200);

            // 요청/응답 크기 기록
            apiMetrics.recordRequestSize("/api/orders", "POST", calculateRequestSize(request));
            apiMetrics.recordResponseSize("/api/orders", "POST", calculateResponseSize(order));

            // 성공 기록
            apiMetrics.recordOutcome("/api/orders", "POST", true);

            return ResponseEntity.ok(order);

        } catch (ValidationException e) {
            // 400 에러
            timer.stop(400);
            apiMetrics.recordError("/api/orders", "POST", "ValidationException");
            apiMetrics.recordOutcome("/api/orders", "POST", false);
            throw e;

        } catch (Exception e) {
            // 500 에러
            long duration = timer.stopWithError(e.getClass().getSimpleName());
            apiMetrics.recordOutcome("/api/orders", "POST", false);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        ApiMetrics.ApiCallTimer timer = apiMetrics.start("/api/orders/{id}", "GET");

        Order order = orderService.findOrder(id);
        timer.stop(200);

        return ResponseEntity.ok(order);
    }
}
```

**Filter를 통한 자동 메트릭 수집**:
```java
import com.eraf.actuator.metrics.ApiMetrics;
import jakarta.servlet.Filter;

@Component
public class ApiMetricsFilter implements Filter {

    private final ApiMetrics apiMetrics;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String endpoint = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        ApiMetrics.ApiCallTimer timer = apiMetrics.start(endpoint, method);

        try {
            // 동시 요청 수 증가
            apiMetrics.incrementConcurrentRequests(endpoint);

            chain.doFilter(request, response);

            // 응답 기록
            int statusCode = httpResponse.getStatus();
            long duration = timer.stop(statusCode);

            // Slow API 체크 (1초 이상)
            if (ApiMetrics.isSlowApi(duration)) {
                log.warn("Slow API detected: {} {} - {}ms", method, endpoint, duration);
            }

        } finally {
            // 동시 요청 수 감소
            apiMetrics.decrementConcurrentRequests(endpoint);
        }
    }
}
```

**수집되는 메트릭**:
- `api.calls` - API 호출 횟수 (tag: endpoint, method, status, status.series)
- `api.response.time` - 응답 시간 (percentiles: 50%, 90%, 95%, 99%)
- `api.request.size` - 요청 크기 (bytes)
- `api.response.size` - 응답 크기 (bytes)
- `api.errors` - 에러 횟수 (tag: endpoint, method, error.type)
- `api.outcomes` - 성공/실패 (tag: endpoint, method, outcome)
- `api.concurrent.requests` - 동시 요청 수

**Prometheus 쿼리**:
```promql
# 초당 API 호출 수
rate(api_calls_total[5m])

# 95% 응답 시간
api_response_time_seconds{quantile="0.95",endpoint="/api/orders",method="POST"}

# 에러율 (%)
sum(rate(api_calls_total{status.series="5xx"}[5m])) /
sum(rate(api_calls_total[5m])) * 100

# 평균 응답 시간
rate(api_response_time_seconds_sum[5m]) / rate(api_response_time_seconds_count[5m])
```

### 13.5. DatabaseMetrics - 데이터베이스 메트릭

HikariCP 커넥션 풀 메트릭을 수집합니다 (HikariCP가 있을 경우 자동 활성화).

```java
import com.eraf.actuator.metrics.DatabaseMetrics;

@Configuration
public class MetricsConfig {

    @Bean
    public DatabaseMetrics databaseMetrics(DataSource dataSource, MeterRegistry meterRegistry) {
        DatabaseMetrics metrics = new DatabaseMetrics(dataSource, meterRegistry);
        metrics.bindDataSourceMetrics();
        return metrics;
    }
}
```

**수집되는 메트릭** (HikariCP):
- `db.pool.active` - 활성 연결 수
- `db.pool.idle` - 유휴 연결 수
- `db.pool.pending` - 대기 중인 연결 요청 수
- `db.pool.total` - 총 연결 수
- `db.pool.max` - 최대 연결 수
- `db.pool.min` - 최소 유휴 연결 수
- `db.pool.wait.time` - 연결 대기 시간
- `db.pool.usage.time` - 연결 사용 시간
- `db.pool.timeout.count` - 타임아웃 발생 수

**Grafana 대시보드 패널**:
```promql
# 커넥션 풀 사용률 (%)
(db_pool_active / db_pool_max) * 100

# 대기 중인 연결 요청
db_pool_pending

# 평균 연결 대기 시간
rate(db_pool_wait_time_seconds_sum[5m]) / rate(db_pool_wait_time_seconds_count[5m])
```

### 13.6. 설정 예제

```yaml
eraf:
  actuator:
    # Liveness & Readiness
    health:
      liveness:
        enabled: true
      readiness:
        enabled: true
        startup-delay-seconds: 10

    # 메트릭 활성화
    metrics:
      custom-enabled: true
      cache-enabled: true
      api-enabled: true
      database-enabled: true

# Spring Boot Actuator
management:
  endpoints:
    web:
      exposure:
        include: '*'  # 모든 엔드포인트 노출
  endpoint:
    health:
      probes:
        enabled: true  # Liveness/Readiness Probe
  metrics:
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
        api.response.time: true
```

### 13.7. Kubernetes Deployment 예제

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-service
spec:
  template:
    spec:
      containers:
      - name: app
        image: my-service:1.0.0

        # Phase 2 Liveness Probe
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

        # Phase 2 Readiness Probe
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## eraf-actuator vs eraf-observability

| 항목 | eraf-actuator | eraf-observability |
|------|---------------|-------------------|
| 메트릭 백엔드 | **Micrometer** (Spring Boot 기본) | **OpenTelemetry** (CNCF 표준) |
| 주요 용도 | Spring Actuator 통합, Prometheus Export | 분산 추적, 멀티 벤더 지원 |
| 헬스 체크 | ✅ 제공 | ❌ 없음 |
| @Counted/@Timed | ✅ 제공 | ❌ 없음 |
| 분산 추적 | 간단한 Trace Context | 완전한 OpenTelemetry Trace |
| Prometheus | ✅ 기본 지원 | 추가 설정 필요 |
| Jaeger/Zipkin | 추가 설정 필요 | ✅ 기본 지원 |

**권장 사용**:
- **eraf-actuator**: Spring Boot 애플리케이션의 기본 모니터링
- **eraf-observability**: 멀티 클라우드, 벤더 중립적 관찰성이 필요한 경우
