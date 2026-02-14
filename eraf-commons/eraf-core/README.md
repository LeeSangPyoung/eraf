# ERAF Core

프레임워크의 핵심 기능 (설정, 컨텍스트, 로깅, 세션)을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 설정 관리 (Config)
- **ErafProperties**: ERAF 프레임워크 설정
- **ProfileManager**: 프로파일 관리
- **PropertySourceLoader**: 속성 소스 로더
- **ConfigurationManager**: 설정 관리자
- **ConfigEncryption**: 설정 암호화 지원

### 2. 컨텍스트 관리 (Context)
- **ErafContext**: 요청 컨텍스트 (ThreadLocal)
- **ContextHolder**: 컨텍스트 보관소
- 사용자 정보, 요청 ID, 타임스탬프 등 관리

### 3. 로깅 (Logging)
- **AuditLogger**: 감사 로그
- **StructuredLogger**: 구조화된 로그 (JSON)
- **LoggingFilter**: HTTP 요청/응답 로깅
- **PerformanceLogger**: 성능 측정 로그
- **AuditLogStore**: 감사 로그 저장소

### 4. 세션 관리 (Session)
- **SessionManager**: 세션 관리자
- Redis/DB/Memory 기반 세션 지원

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-crypto (암호화)
- eraf-core-util (유틸리티)
- eraf-core-exception (예외 처리)
- eraf-core-validation (검증)
- eraf-core-resilience (안정성)
- eraf-core-async (비동기)
- eraf-core-i18n (국제화)
- eraf-core-http (HTTP)
- eraf-core-system (시스템)

**외부 라이브러리**:
- Spring Boot Starter
- Spring Boot Validation
- Spring Boot AOP

## 📝 사용 예시

### ErafContext 사용
```java
@Service
public class OrderService {

    public Order createOrder(OrderRequest request) {
        // 현재 사용자 정보 조회
        String userId = ErafContext.getCurrentUserId();
        String requestId = ErafContext.getRequestId();

        Order order = new Order(userId, request);
        order.setRequestId(requestId);

        return orderRepository.save(order);
    }
}
```

### ContextHolder를 통한 컨텍스트 설정
```java
@Component
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String userId = extractUserId(request);
        String requestId = UUID.randomUUID().toString();

        // 컨텍스트 설정
        ErafContext context = new ErafContext();
        context.setUserId(userId);
        context.setRequestId(requestId);
        context.setTimestamp(LocalDateTime.now());

        ContextHolder.setContext(context);

        try {
            chain.doFilter(request, response);
        } finally {
            ContextHolder.clear(); // 반드시 정리
        }
    }
}
```

### 감사 로그
```java
@Service
public class UserService {

    @Autowired
    private AuditLogger auditLogger;

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 감사 로그 기록
        auditLogger.log(
            "USER_DELETE",
            "사용자 삭제: " + user.getEmail(),
            Map.of("userId", userId, "email", user.getEmail())
        );

        userRepository.delete(user);
    }
}
```

### 구조화된 로그
```java
@Service
public class PaymentService {

    @Autowired
    private StructuredLogger logger;

    public Payment processPayment(PaymentRequest request) {
        // JSON 형식 로그
        logger.info("payment.processing", Map.of(
            "orderId", request.getOrderId(),
            "amount", request.getAmount(),
            "method", request.getMethod()
        ));

        Payment payment = paymentGateway.process(request);

        logger.info("payment.completed", Map.of(
            "paymentId", payment.getId(),
            "transactionId", payment.getTransactionId()
        ));

        return payment;
    }
}
```

### 성능 측정
```java
@Service
public class ReportService {

    @Autowired
    private PerformanceLogger perfLogger;

    public Report generateReport(String reportType) {
        long startTime = System.currentTimeMillis();

        try {
            Report report = reportGenerator.generate(reportType);
            return report;
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            perfLogger.log("report.generate", elapsed, Map.of("type", reportType));
        }
    }
}
```

### 설정 관리
```yaml
# application.yml
eraf:
  context:
    enabled: true
    include-request-id: true
  logging:
    audit:
      enabled: true
      store: database
    structured:
      enabled: true
      format: json
  session:
    timeout: 1800  # 30분
    store: redis
```

### 프로파일별 설정
```java
@Configuration
@Profile("production")
public class ProductionConfig {

    @Autowired
    private ErafProperties erafProperties;

    @PostConstruct
    public void init() {
        // 프로덕션 환경 설정 검증
        if (!erafProperties.getLogging().getAudit().isEnabled()) {
            throw new IllegalStateException("Audit logging must be enabled in production");
        }
    }
}
```

## 🏗️ 주요 클래스

**설정 (Config)**:
- `ErafProperties` - ERAF 설정
- `ProfileManager` - 프로파일 관리
- `ConfigurationManager` - 설정 관리

**컨텍스트 (Context)**:
- `ErafContext` - 요청 컨텍스트
- `ContextHolder` - ThreadLocal 컨텍스트 보관소

**로깅 (Logging)**:
- `AuditLogger` - 감사 로그
- `StructuredLogger` - 구조화된 로그
- `PerformanceLogger` - 성능 로그
- `LoggingFilter` - HTTP 로깅 필터

**세션 (Session)**:
- `SessionManager` - 세션 관리

## 📚 ErafContext 구조

```java
public class ErafContext {
    private String userId;        // 사용자 ID
    private String requestId;     // 요청 ID (추적용)
    private String traceId;       // 분산 추적 ID
    private LocalDateTime timestamp; // 요청 시작 시간
    private Map<String, Object> attributes; // 추가 속성
}
```

## 📚 로그 형식

### 감사 로그
```json
{
  "timestamp": "2024-01-15T14:30:25",
  "userId": "user123",
  "action": "USER_DELETE",
  "message": "사용자 삭제: user@example.com",
  "details": {
    "userId": 456,
    "email": "user@example.com"
  },
  "requestId": "req-abc123",
  "ipAddress": "192.168.1.100"
}
```

### 구조화된 로그
```json
{
  "timestamp": "2024-01-15T14:30:25",
  "level": "INFO",
  "event": "payment.completed",
  "data": {
    "paymentId": 789,
    "transactionId": "TXN-20240115-001"
  },
  "requestId": "req-abc123"
}
```

## ⚠️ 주의사항

- **ErafContext**: 반드시 요청 종료 시 `ContextHolder.clear()` 호출
- **감사 로그**: 민감한 정보 (비밀번호, 카드번호 등) 마스킹 필수
- **성능 로그**: 대용량 처리 시 비동기 로깅 권장
- **세션**: 분산 환경에서는 Redis 세션 사용 권장

## 🔧 설정 예시

### 감사 로그 DB 저장
```java
@Configuration
public class AuditConfig {

    @Bean
    public AuditLogStore auditLogStore() {
        return new DatabaseAuditLogStore(); // DB에 저장
    }
}
```

### 로깅 필터 등록
```java
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
```
