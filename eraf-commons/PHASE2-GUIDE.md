# ERAF Commons Phase 2 - 사용 가이드

ERAF Commons Phase 2는 프로덕션 환경에서 필수적인 모니터링, 추적, 감사 기능을 강화한 업데이트입니다.

## 📋 목차

1. [Phase 2 개요](#phase-2-개요)
2. [Request Context 통합](#request-context-통합)
3. [Health Check 확장](#health-check-확장)
4. [Logging & Audit 통합](#logging--audit-통합)
5. [Metrics 수집 확장](#metrics-수집-확장)
6. [Entity History Tracking](#entity-history-tracking)
7. [통합 사용 예제](#통합-사용-예제)
8. [프로덕션 배포](#프로덕션-배포)

---

## Phase 2 개요

### 추가된 기능

#### 1. **Request Context Integration** (eraf-web)
- ThreadLocal 기반 HTTP 요청 정보 관리
- SLF4J MDC 자동 통합
- 비동기 메서드 컨텍스트 전파

#### 2. **Health Check Extension** (eraf-actuator)
- Kubernetes Liveness/Readiness Probe
- 의존성 기반 Health 관리
- 타임아웃 기반 외부 시스템 체크

#### 3. **Logging & Audit Integration** (eraf-data-jpa)
- 비동기 감사 로깅 (AsyncAuditLogger)
- 동적 감사 로그 검색 (AuditLogQueryService)
- 자동 감사 로그 삭제 정책 (AuditLogRetentionPolicy)

#### 4. **Metrics Collection Extension** (eraf-actuator)
- CustomMetrics - 메트릭 인스턴스 재사용
- CacheMetrics - 캐시 Hit/Miss 추적
- ApiMetrics - API 호출 통계
- DatabaseMetrics - HikariCP 풀 모니터링

#### 5. **Entity Change History** (eraf-data-jpa)
- Hibernate Envers 통합
- 엔티티 변경 이력 자동 추적
- 리비전 조회 및 비교

### 의존성

```xml
<!-- Phase 2 전체 기능 사용 -->
<dependencies>
    <!-- Request Context -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-web</artifactId>
        <version>${eraf.version}</version>
    </dependency>

    <!-- Health & Metrics -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-actuator</artifactId>
        <version>${eraf.version}</version>
    </dependency>

    <!-- Audit & Entity History -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-data-jpa</artifactId>
        <version>${eraf.version}</version>
    </dependency>

    <!-- 선택적: Entity History (Envers) -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-envers</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 선택적: Prometheus Export -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## Request Context 통합

### 설정

```yaml
eraf:
  web:
    request-context:
      enabled: true
      trace-id-header: X-Trace-Id
      request-id-header: X-Request-Id

# 로그에 MDC 정보 포함
logging:
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{userId}] - %msg%n'
```

### Controller에서 사용

```java
import com.eraf.web.context.RequestContext;
import com.eraf.web.context.RequestContextHolder;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ApiMetrics apiMetrics;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        // 1. RequestContext 자동 주입 (Filter에서 처리)
        RequestContext context = RequestContextHolder.getContext();

        log.info("Creating order for user: {}", context.getUserId());
        // 로그에 자동으로 requestId, traceId, userId 포함됨

        // 2. API 메트릭 시작
        ApiMetrics.ApiCallTimer timer = apiMetrics.start("/api/orders", "POST");

        try {
            // 3. 비즈니스 로직
            Order order = orderService.createOrder(request);

            // 4. 메트릭 기록
            timer.stop(200);
            apiMetrics.recordOutcome("/api/orders", "POST", true);

            return ResponseEntity.ok(toResponse(order));

        } catch (ValidationException e) {
            timer.stop(400);
            apiMetrics.recordError("/api/orders", "POST", "ValidationException");
            throw e;
        }
    }
}
```

### Service에서 사용

```java
@Service
public class OrderService {

    private final AsyncAuditLogger asyncAuditLogger;
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // RequestContext는 Service 레이어에서도 접근 가능
        RequestContext context = RequestContextHolder.getContext();

        // 주문 생성
        Order order = new Order();
        order.setUserId(context.getUserId());
        order.setCreatedIp(context.getClientIp());
        order.setTotalAmount(request.getTotalAmount());
        order = orderRepository.save(order);

        // 감사 로그 (비동기)
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.CREATE)
                .resource("Order")
                .resourceId(String.valueOf(order.getId()))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context.getUserId())
                .username(context.getUsername())
                .clientIp(context.getClientIp())
                .requestUri(context.getUri())
                .requestMethod(context.getMethod())
                .description("Order created with amount: " + order.getTotalAmount())
                .build();

        asyncAuditLogger.logAndForget(auditLog);

        log.info("Order created: {}", order.getId());
        // 로그에 [requestId] [traceId] [userId] 자동 포함

        return order;
    }
}
```

### 비동기 메서드에서 사용

```java
@Service
public class NotificationService {

    @Async
    public CompletableFuture<Void> sendNotificationAsync(Long orderId) {
        // RequestContextTaskDecorator가 자동으로 컨텍스트를 전파
        RequestContext context = RequestContextHolder.getContext();

        log.info("Sending notification for order: {}", orderId);
        // 부모 스레드의 requestId, traceId가 자동으로 전파됨

        // 알림 전송 로직...

        return CompletableFuture.completedFuture(null);
    }
}
```

---

## Health Check 확장

### Liveness Probe 설정

```yaml
eraf:
  actuator:
    health:
      liveness:
        enabled: true

management:
  endpoint:
    health:
      probes:
        enabled: true
```

```java
@Configuration
public class HealthConfig {

    @Bean
    public LivenessHealthIndicator livenessHealthIndicator() {
        LivenessHealthIndicator indicator = new LivenessHealthIndicator();
        return indicator;
    }

    @Bean
    public ApplicationStartupListener startupListener(LivenessHealthIndicator livenessHealthIndicator) {
        return new ApplicationStartupListener(livenessHealthIndicator);
    }
}

@Component
class ApplicationStartupListener {

    private final LivenessHealthIndicator livenessHealthIndicator;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 애플리케이션 시작 완료
        livenessHealthIndicator.markAsReady();
        log.info("Application is ready - Liveness: UP");
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        // 애플리케이션 종료 중
        livenessHealthIndicator.markAsDown();
        log.info("Application shutting down - Liveness: DOWN");
    }
}
```

### Readiness Probe 설정

```yaml
eraf:
  actuator:
    health:
      readiness:
        enabled: true
        startup-delay-seconds: 10
```

```java
@Configuration
public class ReadinessConfig {

    @Bean
    public ReadinessHealthIndicator readinessHealthIndicator() {
        ReadinessHealthIndicator indicator = new ReadinessHealthIndicator();

        // 초기 의존성 설정
        indicator.addDependency("database", true);
        indicator.addDependency("redis", true);
        indicator.addDependency("kafka", false);  // 선택적 의존성

        return indicator;
    }

    @Bean
    public DependencyMonitor dependencyMonitor(ReadinessHealthIndicator readinessHealthIndicator,
                                                DataSource dataSource,
                                                RedisTemplate<?, ?> redisTemplate) {
        return new DependencyMonitor(readinessHealthIndicator, dataSource, redisTemplate);
    }
}

@Component
class DependencyMonitor {

    private final ReadinessHealthIndicator readinessHealthIndicator;
    private final DataSource dataSource;
    private final RedisTemplate<?, ?> redisTemplate;

    @Scheduled(fixedDelay = 10000)  // 10초마다 체크
    public void checkDependencies() {
        // 데이터베이스 체크
        boolean dbReady = checkDatabase();
        readinessHealthIndicator.addDependency("database", dbReady);

        // Redis 체크
        boolean redisReady = checkRedis();
        readinessHealthIndicator.addDependency("redis", redisReady);
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(3);  // 3초 타임아웃
        } catch (Exception e) {
            log.warn("Database health check failed", e);
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return false;
        }
    }
}
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    spec:
      containers:
      - name: app
        image: order-service:1.0.0

        # Liveness Probe - 프로세스 살아있는지 확인
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        # Readiness Probe - 트래픽 받을 준비 확인
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
```

---

## Logging & Audit 통합

### 비동기 감사 로깅

```yaml
eraf:
  jpa:
    audit-retention:
      enabled: true
      retention-days: 365
      hard-delete-enabled: true
      hard-delete-after-days: 730
      cron: "0 0 2 * * ?"
      hard-delete-cron: "0 0 3 * * SUN"
```

```java
@Service
public class UserService {

    private final AsyncAuditLogger asyncAuditLogger;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(UserRequest request) {
        RequestContext context = RequestContextHolder.getContext();

        // 사용자 생성
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);

        // 감사 로그 (비동기, Fire-and-Forget)
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.CREATE)
                .resource("User")
                .resourceId(String.valueOf(user.getId()))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context.getUserId())
                .username(context.getUsername())
                .clientIp(context.getClientIp())
                .requestUri(context.getUri())
                .requestMethod(context.getMethod())
                .description("User created: " + user.getEmail())
                .build();

        asyncAuditLogger.logAndForget(auditLog);

        return user;
    }

    @Transactional
    public void deleteUser(Long userId) {
        RequestContext context = RequestContextHolder.getContext();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String userEmail = user.getEmail();
        userRepository.delete(user);

        // 삭제 감사 로그
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.DELETE)
                .resource("User")
                .resourceId(String.valueOf(userId))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context.getUserId())
                .description("User deleted: " + userEmail)
                .build();

        asyncAuditLogger.logAndForget(auditLog);
    }
}
```

### 감사 로그 검색

```java
@Service
public class AuditService {

    private final AuditLogQueryService auditLogQueryService;

    // 사용자별 최근 활동 조회
    public List<AuditLogEntity> getUserRecentActivities(String userId, int limit) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .userId(userId)
                .deleted(false)
                .build();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogQueryService.search(criteria, pageable).getContent();
    }

    // 특정 리소스의 변경 이력
    public Page<AuditLogEntity> getResourceHistory(String resource, String resourceId, Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .resource(resource)
                .resourceId(resourceId)
                .deleted(false)
                .build();

        return auditLogQueryService.search(criteria, pageable);
    }

    // 기간별 실패한 작업 조회
    public List<AuditLogEntity> getFailedOperations(Instant from, Instant to) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .result(AuditEventStandard.Result.FAILURE)
                .timestampFrom(from)
                .timestampTo(to)
                .deleted(false)
                .build();

        return auditLogQueryService.search(criteria, Pageable.unpaged()).getContent();
    }
}
```

---

## Metrics 수집 확장

### API Metrics

```java
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
            apiMetrics.incrementConcurrentRequests(endpoint);
            chain.doFilter(request, response);

            int statusCode = httpResponse.getStatus();
            long duration = timer.stop(statusCode);

            // Slow API 경고
            if (ApiMetrics.isSlowApi(duration)) {
                log.warn("Slow API: {} {} - {}ms", method, endpoint, duration);
            }

        } finally {
            apiMetrics.decrementConcurrentRequests(endpoint);
        }
    }
}
```

### Cache Metrics

```java
@Service
public class ProductCacheService {

    private final CacheMetrics cacheMetrics;
    private final Map<String, Product> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        cacheMetrics.registerCache("productCache");
    }

    public Product getProduct(String productId) {
        Product product = cache.get(productId);

        if (product != null) {
            cacheMetrics.recordHit("productCache");
            return product;
        } else {
            cacheMetrics.recordMiss("productCache");

            Timer.Sample sample = cacheMetrics.startLoad("productCache");
            try {
                product = loadFromDatabase(productId);
                cache.put(productId, product);
                cacheMetrics.recordPut("productCache");
                cacheMetrics.stopLoad(sample, "productCache", true);
            } catch (Exception e) {
                cacheMetrics.stopLoad(sample, "productCache", false);
                throw e;
            }

            return product;
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void updateCacheSize() {
        cacheMetrics.setSize("productCache", cache.size());
    }
}
```

### Custom Metrics

```java
@Service
public class OrderMetricsService {

    private final CustomMetrics customMetrics;

    public void recordOrderCreated(Order order) {
        // Counter 증가
        customMetrics.counter("orders.created", "status", order.getStatus())
                .increment();

        // 주문 금액 분포
        customMetrics.distributionSummary("orders.amount", "currency", order.getCurrency())
                .record(order.getTotalAmount());
    }

    public void processOrderWithTimer(Order order) {
        // Timer로 처리 시간 측정
        Timer timer = customMetrics.timer("orders.processing.time", "type", order.getType());
        timer.record(() -> {
            // 주문 처리 로직
            processOrder(order);
        });
    }

    @PostConstruct
    public void registerGauges() {
        // 실시간 대기 중인 주문 수
        customMetrics.gauge("orders.pending",
                () -> orderRepository.countByStatus("PENDING"));
    }
}
```

---

## Entity History Tracking

### Envers 설정

```yaml
eraf:
  jpa:
    envers:
      enabled: true

spring:
  jpa:
    properties:
      org.hibernate.envers:
        audit_table_suffix: _aud
        revision_field_name: rev
        revision_type_field_name: revtype
```

### Entity에 @Audited 추가

```java
import org.hibernate.envers.Audited;
import com.eraf.jpa.entity.BaseEntity;

@Entity
@Table(name = "products")
@Audited  // Envers 감사 활성화
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String description;

    // Getters and Setters
}
```

### 변경 이력 조회

```java
@Service
public class ProductHistoryService {

    private final EntityRevisionService entityRevisionService;

    // 제품의 전체 변경 이력
    public List<ProductHistoryDto> getProductHistory(Long productId) {
        List<EntityRevision<Product>> history =
                entityRevisionService.getEntityHistory(Product.class, productId);

        return history.stream()
                .map(revision -> {
                    Product product = revision.getEntity();
                    RevisionEntity revInfo = revision.getRevision();

                    return ProductHistoryDto.builder()
                            .revisionId(revInfo.getId())
                            .revisionType(revision.getRevisionTypeString())
                            .timestamp(revInfo.getRevisionDate())
                            .modifiedBy(revInfo.getUsername())
                            .name(product.getName())
                            .price(product.getPrice())
                            .description(product.getDescription())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 특정 시점의 제품 상태
    public Product getProductAtDate(Long productId, Date date) {
        return entityRevisionService.findEntityAtDate(Product.class, productId, date);
    }

    // 두 리비전 비교
    public Map<String, Object> compareRevisions(Long productId, Long rev1, Long rev2) {
        Product product1 = entityRevisionService.findEntityAtRevision(Product.class, productId, rev1);
        Product product2 = entityRevisionService.findEntityAtRevision(Product.class, productId, rev2);

        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(product1.getName(), product2.getName())) {
            changes.put("name", Map.of("from", product1.getName(), "to", product2.getName()));
        }

        if (!Objects.equals(product1.getPrice(), product2.getPrice())) {
            changes.put("price", Map.of("from", product1.getPrice(), "to", product2.getPrice()));
        }

        return changes;
    }
}
```

---

## 통합 사용 예제

완전한 CRUD 서비스에서 모든 Phase 2 기능을 통합한 예제입니다.

```java
import com.eraf.web.context.RequestContext;
import com.eraf.web.context.RequestContextHolder;
import com.eraf.jpa.audit.AsyncAuditLogger;
import com.eraf.jpa.audit.AuditEventStandard;
import com.eraf.jpa.envers.EntityRevisionService;
import com.eraf.actuator.metrics.ApiMetrics;
import com.eraf.actuator.metrics.CustomMetrics;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AsyncAuditLogger asyncAuditLogger;
    private final EntityRevisionService entityRevisionService;
    private final CustomMetrics customMetrics;

    @Transactional
    public Product createProduct(ProductRequest request) {
        // 1. Request Context (자동 주입)
        RequestContext context = RequestContextHolder.getContext();

        // 2. Custom Metrics - 처리 시간 측정
        Timer timer = customMetrics.timer("product.create.time");
        return timer.record(() -> {

            // 3. 제품 생성
            Product product = new Product();
            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setDescription(request.getDescription());
            product = productRepository.save(product);

            // 4. Counter 증가
            customMetrics.counter("product.created", "category", request.getCategory())
                    .increment();

            // 5. 비동기 감사 로그
            AuditLogEntity auditLog = AuditEventStandard.builder()
                    .action(AuditEventStandard.Action.CREATE)
                    .resource("Product")
                    .resourceId(String.valueOf(product.getId()))
                    .result(AuditEventStandard.Result.SUCCESS)
                    .userId(context.getUserId())
                    .username(context.getUsername())
                    .clientIp(context.getClientIp())
                    .requestUri(context.getUri())
                    .requestMethod(context.getMethod())
                    .description("Product created: " + product.getName())
                    .build();

            asyncAuditLogger.logAndForget(auditLog);

            // 6. Envers가 자동으로 변경 이력 저장

            log.info("Product created: {}", product.getId());
            // 로그에 [requestId] [traceId] [userId] 자동 포함

            return product;
        });
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest request) {
        RequestContext context = RequestContextHolder.getContext();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        String oldName = product.getName();
        BigDecimal oldPrice = product.getPrice();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product = productRepository.save(product);

        // 감사 로그
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.UPDATE)
                .resource("Product")
                .resourceId(String.valueOf(productId))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context.getUserId())
                .description(String.format("Product updated: %s -> %s, Price: %s -> %s",
                        oldName, product.getName(), oldPrice, product.getPrice()))
                .build();

        asyncAuditLogger.logAndForget(auditLog);

        // 메트릭
        customMetrics.counter("product.updated").increment();

        // Envers가 자동으로 변경 이력 저장 (UPDATE)

        return product;
    }

    @Transactional(readOnly = true)
    public ProductHistoryResponse getProductWithHistory(Long productId) {
        // 현재 제품 정보
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // 변경 이력 조회 (Envers)
        List<EntityRevision<Product>> history =
                entityRevisionService.getEntityHistory(Product.class, productId);

        // 감사 로그 조회
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .resource("Product")
                .resourceId(String.valueOf(productId))
                .deleted(false)
                .build();

        List<AuditLogEntity> auditLogs = auditLogQueryService.search(
                criteria,
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();

        return ProductHistoryResponse.builder()
                .product(product)
                .revisionHistory(toRevisionDtos(history))
                .auditLogs(toAuditDtos(auditLogs))
                .build();
    }
}
```

---

## 프로덕션 배포

### application.yml (Production)

```yaml
server:
  port: 8080

spring:
  application:
    name: order-service

  datasource:
    url: jdbc:postgresql://db.example.com:5432/orders
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        show_sql: false

# ERAF Phase 2
eraf:
  # Request Context
  web:
    request-context:
      enabled: true
      trace-id-header: X-Trace-Id
      request-id-header: X-Request-Id

  # JPA & Audit
  jpa:
    auditing-enabled: true
    envers:
      enabled: true
    audit-retention:
      enabled: true
      retention-days: 365
      hard-delete-enabled: true
      hard-delete-after-days: 730
      cron: "0 0 2 * * ?"
      hard-delete-cron: "0 0 3 * * SUN"

  # Actuator & Health
  actuator:
    health:
      liveness:
        enabled: true
      readiness:
        enabled: true
        startup-delay-seconds: 10
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
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
      environment: production
    distribution:
      percentiles-histogram:
        http.server.requests: true
        api.response.time: true

# Logging
logging:
  level:
    com.eraf: INFO
    org.springframework.web: INFO
    org.hibernate: WARN
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{userId}] - %msg%n'
    file: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{requestId}] [%X{traceId}] [%X{sessionId}] [%X{userId}] [%X{clientIp}] - %msg%n'
  file:
    name: /var/log/order-service/application.log
    max-size: 100MB
    max-history: 30
```

### Kubernetes 배포

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: order-service:1.0.0
        ports:
        - containerPort: 8080
          name: http

        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password

        # Phase 2 Health Probes
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3

        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 2Gi

---
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: production
spec:
  selector:
    app: order-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
# Prometheus ServiceMonitor
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: order-service
  namespace: production
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

### Grafana 대시보드

**주요 패널**:

1. **Request Context** - 요청 추적
```promql
# 초당 요청 수
rate(api_calls_total[5m])

# 95% 응답 시간
api_response_time_seconds{quantile="0.95"}

# 에러율
sum(rate(api_calls_total{status.series="5xx"}[5m])) /
sum(rate(api_calls_total[5m])) * 100
```

2. **Cache Metrics** - 캐시 성능
```promql
# Cache Hit 비율
cache_hit_ratio * 100

# 초당 Cache Miss
rate(cache_gets_total{result="miss"}[5m])
```

3. **Database Metrics** - DB 커넥션 풀
```promql
# 커넥션 풀 사용률
(db_pool_active / db_pool_max) * 100

# 대기 중인 연결
db_pool_pending
```

4. **Audit Logs** - 감사 로그 통계
- 일별 감사 로그 생성 수
- 액션별 분포 (CREATE, UPDATE, DELETE)
- 사용자별 활동

---

## 문의 및 지원

Phase 2 기능 관련 문의:
- 기술 지원: ERAF Development Team
- 문서: 각 모듈의 README.md 참조
  - [eraf-web](./eraf-web/README.md)
  - [eraf-actuator](./eraf-actuator/README.md)
  - [eraf-data-jpa](./eraf-data-jpa/README.md)
