# ERAF (Enterprise Reusable Asset Framework)

엔터프라이즈 애플리케이션 개발을 위한 재사용 가능한 공통 모듈 라이브러리

## 개요

ERAF는 Spring Boot 3.4.x 기반의 엔터프라이즈 공통 모듈 라이브러리입니다. 반복적으로 구현되는 기능들을 표준화하여 개발 생산성을 높이고 코드 품질을 일관되게 유지할 수 있습니다.

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.4.1
- **Build Tool**: Maven

## 프로젝트 구조

```
eraf-commons/
├── eraf-bom/                    # 버전 관리 BOM
├── eraf-core/                   # 핵심 기능 모듈
├── eraf-starter-web/            # 웹 자동설정 (로깅필터, 파일업로드)
├── eraf-starter-database/       # DB 자동설정
├── eraf-starter-jpa/            # JPA 자동설정
├── eraf-starter-mybatis/        # MyBatis 자동설정
├── eraf-starter-redis/          # Redis 자동설정
├── eraf-starter-cache/          # 캐시 자동설정
├── eraf-starter-session/        # 세션 관리
├── eraf-starter-security/       # 보안 자동설정
├── eraf-starter-actuator/       # Health Check/Metrics
├── eraf-starter-swagger/        # API 문서화 (OpenAPI)
├── eraf-starter-notification/   # 알림 (Email, SMS, Push)
├── eraf-starter-scheduler/      # 스케줄러
├── eraf-starter-statemachine/   # 상태머신
├── eraf-starter-messaging/      # 메시징
├── eraf-starter-kafka/          # Kafka 자동설정
├── eraf-starter-rabbitmq/       # RabbitMQ 자동설정
├── eraf-starter-batch/          # Spring Batch 자동설정
├── eraf-starter-s3/             # S3/파일 스토리지
├── eraf-starter-ftp/            # FTP/SFTP
├── eraf-starter-tcp/            # TCP 클라이언트
├── eraf-starter-elasticsearch/  # Elasticsearch 자동설정
├── eraf-starter-service-client/ # 서비스 간 호출 클라이언트
├── eraf-starter-minimal/        # 최소 번들
└── eraf-starter-all/            # 전체 번들
```

## 빠른 시작

### Maven 의존성 추가

```xml
<!-- BOM 추가 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 필요한 스타터 추가 -->
<dependencies>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-starter-web</artifactId>
    </dependency>
</dependencies>
```

## eraf-core 기능

### 1. 암호화 (crypto)

```java
// AES-256-GCM 암호화
String encrypted = Crypto.encrypt("평문", "secretKey");
String decrypted = Crypto.decrypt(encrypted, "secretKey");

// 비밀번호 해싱 (bcrypt)
String hashed = Password.hash("password123");
boolean matches = Password.verify("password123", hashed);

// JWT 토큰
String token = Jwt.create()
    .subject("userId")
    .claim("role", "ADMIN")
    .expireAfter(Duration.ofHours(1))
    .sign("secretKey");

Map<String, Object> claims = Jwt.parse(token, "secretKey");
```

### 2. 마스킹 (masking)

```java
Masking.name("홍길동");           // 홍*동
Masking.phone("010-1234-5678");  // 010-****-5678
Masking.email("test@email.com"); // te**@email.com
Masking.cardNumber("1234-5678-9012-3456"); // 1234-****-****-3456
Masking.residentNumber("901231-1234567");   // 901231-*******
```

### 3. 검증 어노테이션 (validation)

```java
public class UserRequest {
    @Email
    private String email;

    @Phone
    private String phone;

    @Password(minLength = 8, requireSpecial = true)
    private String password;

    @NoXss
    private String content;

    @NoSqlInjection
    private String searchKeyword;

    @BusinessNo
    private String businessNumber;

    @FileExtension(allowed = {"jpg", "png", "pdf"})
    private String fileName;
}
```

### 4. 표준 응답 (response)

```java
// 성공 응답
ApiResponse.success(data);
ApiResponse.success(data, "처리되었습니다.");

// 에러 응답
ApiResponse.error("ERROR_CODE", "에러 메시지");
ApiResponse.error(CommonErrorCode.BAD_REQUEST);

// 페이지 응답
PageResponse.of(page, totalElements, totalPages, content);
```

### 5. 예외 처리 (exception)

```java
// 비즈니스 예외
throw new BusinessException(CommonErrorCode.NOT_FOUND);
throw new BusinessException(CommonErrorCode.BAD_REQUEST, "상세 메시지");

// 공통 에러 코드
public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", 400),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),
    NOT_FOUND("NOT_FOUND", "리소스를 찾을 수 없습니다.", 404),
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 오류가 발생했습니다.", 500);
}
```

### 6. 유틸리티 (utils)

```java
// 날짜
DateUtils.now();
DateUtils.format(localDate, "yyyy-MM-dd");
DateUtils.parse("2024-01-01", "yyyy-MM-dd");
DateUtils.between(start, end);

// 금액
MoneyUtils.format(1234567);        // "1,234,567"
MoneyUtils.toKorean(1234567);      // "백이십삼만사천오백육십칠"
MoneyUtils.calculate(price, quantity, taxRate);

// 문자열
StringUtils.isEmpty(str);
StringUtils.nvl(str, "default");
StringUtils.maskMiddle("홍길동", '*');

// ID 생성
IdGenerator.uuid();                // UUID
IdGenerator.timeBasedId();         // 시간 기반 ID
IdGenerator.snowflakeId();         // Snowflake ID
```

### 7. 채번 (sequence)

```java
public class Order {
    @Sequence(prefix = "ORD", reset = Reset.DAILY, digits = 5)
    private String orderNo;  // ORD-20240101-00001
}

// 직접 생성
String seq = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);
```

### 8. 공통코드 (code)

```java
// 어노테이션 기반 검증
public class Request {
    @Code(group = "ORDER_STATUS")
    private String status;
}

// 서비스 사용
@Autowired
private CodeService codeService;

List<CodeItem> statuses = codeService.getByGroup("ORDER_STATUS");
String statusName = codeService.getName("ORDER_STATUS", "PENDING");
```

### 9. HTTP 클라이언트 (http)

```java
ErafHttpClient client = ErafHttpClient.create()
    .baseUrl("https://api.example.com")
    .timeout(Duration.ofSeconds(30))
    .bearerToken("token")
    .retry(3);

// JSON 요청
User user = client.get("/users/1", User.class);
User created = client.post("/users", request, User.class);

// 파일 업로드
client.uploadFile("/upload", file, "file");
client.uploadFileWithData("/upload", file, "file", Map.of("type", "document"));

// 파일 다운로드
byte[] data = client.downloadFile("/files/1");
client.downloadFile("/files/1", new File("download.pdf"));
```

### 10. 파일 처리 (file, image, excel, pdf)

```java
// 파일 유틸
FileUtils.read(path);
FileUtils.write(path, content);
FileUtils.copy(source, target);
ZipUtils.zip(files, outputPath);
ZipUtils.unzip(zipFile, outputDir);

// 이미지 처리
ImageUtils.resize(image, 800, 600);
ImageUtils.crop(image, x, y, width, height);
ImageUtils.rotate(image, 90);
ImageUtils.watermark(image, "Copyright");
ImageUtils.thumbnail(image, 200, 200);

// Excel
List<Map<String, Object>> data = ExcelReader.read(file);
ExcelWriter.write(data, headers, outputPath);

// PDF
PdfGenerator.fromHtml(html, outputPath);
PdfMerger.merge(pdfFiles, outputPath);
PdfSplitter.split(pdfFile, outputDir);
String text = PdfTextUtils.extract(pdfFile);
```

### 10-1. 파일 업로드/다운로드 서비스

```java
// 파일 저장소 서비스
@Autowired
private FileStorageService fileStorage;

// 파일 업로드
StoredFile stored = fileStorage.store(multipartFile);
StoredFile stored = fileStorage.store(multipartFile, "documents");
List<StoredFile> files = fileStorage.storeAll(multipartFiles);

// 파일 다운로드
Resource resource = fileStorage.load(filePath);

// 파일 관리
boolean exists = fileStorage.exists(filePath);
fileStorage.delete(filePath);
fileStorage.copy(sourcePath, destPath);
fileStorage.move(sourcePath, destPath);

// 파일 검증
ValidationResult result = FileValidationUtils.validateImage(file, 10); // 10MB 제한
result.throwIfInvalid();

boolean isImage = FileValidationUtils.isImageFile(file);
boolean isDangerous = FileValidationUtils.isDangerousFile(file);

// 파일 다운로드 응답 생성
return FileDownloadHelper.download(resource, "문서.pdf");
return FileDownloadHelper.inline(resource, "이미지.png"); // 브라우저에서 직접 표시
```

### 11. 바코드/QR (barcode)

```java
// 바코드 생성
byte[] barcode = BarcodeGenerator.generate("1234567890", BarcodeFormat.CODE_128, 300, 100);

// QR 코드 생성
byte[] qrCode = QRCodeGenerator.generate("https://example.com", 300, 300);

// 바코드 읽기
String value = BarcodeReader.read(imageFile);
```

### 12. 로깅 (logging)

```java
// 구조화된 로깅
StructuredLogger log = StructuredLogger.getLogger(MyService.class);
log.info("주문 생성", Map.of("orderId", orderId, "amount", amount));

// 감사 로깅
AuditLogger.log("ORDER_CREATE", "orders", orderId, "SUCCESS", details);
```

### 13. 분산 락 (lock)

```java
@DistributedLock(key = "'order:' + #orderId", waitTime = 5, leaseTime = 10)
public void processOrder(String orderId) {
    // 동시 실행 방지
}

@OptimisticRetry(maxRetries = 3, backoff = 100)
public void updateWithRetry() {
    // 낙관적 락 충돌 시 재시도
}
```

### 14. 멱등성 (idempotent)

```java
@Idempotent(key = "#request.transactionId", ttl = 3600)
public ApiResponse<?> createPayment(PaymentRequest request) {
    // 중복 요청 방지
}
```

### 15. 기능 토글 (config)

```java
@Feature("NEW_CHECKOUT_FLOW")
public void newCheckout() {
    // 기능이 비활성화되면 FeatureDisabledException 발생
}

@Feature(value = "BETA_FEATURE", fallback = "'기본값'")
public String getBetaFeature() {
    // 비활성화 시 fallback SpEL 표현식 결과 반환
}
```

### 16. 이벤트 (event)

```java
// 이벤트 발행
@Autowired
private EventPublisher eventPublisher;

eventPublisher.publish(new OrderCreatedEvent(orderId));

// 이벤트 핸들러
@EventHandler
public void onOrderCreated(OrderCreatedEvent event) {
    // 동기 처리
}

@AsyncEventHandler
public void onOrderCreatedAsync(OrderCreatedEvent event) {
    // 비동기 처리
}

@AfterCommit
@EventHandler
public void afterCommit(OrderCreatedEvent event) {
    // 트랜잭션 커밋 후 처리
}
```

### 17. 국제화 (i18n)

```java
@Autowired
private MessageService messageService;

String msg = messageService.get("error.not_found");
String msgWithArgs = messageService.get("welcome.message", userName);

// 응답 자동 번역
@TranslateResponse
public ApiResponse<?> getUser() {
    return ApiResponse.success(user, "user.fetch.success");
}
```

### 18. 컨텍스트 (context)

```java
// 컨텍스트 설정
ErafContext context = ErafContextHolder.getContext();
context.setUserId("user123");
context.setTraceId("trace-xxx");
context.set("customKey", value);

// 컨텍스트 조회
String userId = ErafContextHolder.getContext().getUserId();
```

## Starter 모듈

### eraf-starter-web

웹 애플리케이션 자동 설정

```yaml
eraf:
  web:
    cors-enabled: true
    cors-allowed-origins: ["*"]
    idempotent:
      enabled: true
    lock:
      enabled: true
    feature-toggle:
      enabled: true
    logging:
      enabled: true
      include-payload: true
      max-payload-length: 1000
      exclude-patterns: ["/actuator", "/health"]
    file-upload:
      enabled: true
      upload-path: ./uploads
      base-url: /files
      create-date-directory: true
      max-file-size-mb: 10
```

**자동 등록 빈:**
- GlobalExceptionHandler
- IdempotentAspect (InMemory)
- DistributedLockAspect (InMemory)
- SequenceAspect
- FeatureToggleAspect
- MessageAspect
- CodeService
- RequestLoggingFilter (TraceId 자동 전파)
- FileStorageService (로컬 파일 저장소)

### eraf-starter-jpa

JPA 감사 기능 자동 설정

```java
// BaseEntity 상속
@Entity
public class Order extends BaseEntity {
    // createdAt, createdBy, updatedAt, updatedBy 자동 관리
}

// ID 포함 버전
@Entity
public class Product extends BaseIdEntity {
    // id + 감사 필드 자동 관리
}
```

### eraf-starter-redis

Redis 기반 분산 기능

```yaml
eraf:
  redis:
    lock:
      enabled: true
    sequence:
      enabled: true
    idempotent:
      enabled: true
      ttl: 24h
```

**자동 등록 빈:**
- RedisLockProvider
- RedisSequenceGenerator
- RedisIdempotencyStore
- DistributedLockAspect (Redis)
- IdempotentAspect (Redis)

### eraf-starter-actuator

Health Check 및 메트릭 확장

```yaml
eraf:
  actuator:
    health-enabled: true
    metrics-enabled: true
    application-name: my-app
    health:
      redis:
        enabled: true
      database:
        enabled: true
      kafka:
        enabled: true
```

**자동 등록 빈:**
- RedisHealthIndicator (Redis 연결 상태)
- DatabaseHealthIndicator (DB 연결 상태)
- KafkaHealthIndicator (Kafka 클러스터 상태)

### eraf-starter-swagger

API 문서화 (SpringDoc OpenAPI 3)

```yaml
eraf:
  swagger:
    enabled: true
    api-info:
      title: My API Documentation
      description: API 설명
      version: 1.0.0
      contact:
        name: 개발팀
        email: dev@example.com
    security:
      enabled: true
      scheme-name: bearerAuth
      scheme: bearer
      bearer-format: JWT
    group:
      default-group: all
      paths-to-match: ["/**"]
      paths-to-exclude: ["/actuator/**"]
```

**자동 등록 빈:**
- OpenAPI (JWT 인증 스키마 자동 설정)
- GroupedOpenApi

### eraf-starter-kafka

Apache Kafka 자동 설정

```yaml
eraf:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: my-group
      auto-offset-reset: earliest
    producer:
      acks: all
      retries: 3
    retry:
      max-attempts: 3
      backoff-ms: 1000
    dlq:
      enabled: true
      topic-suffix: .dlq
    transaction:
      enabled: false
```

```java
// 메시지 발행
@Autowired
private ErafKafkaProducer producer;

ErafKafkaEvent<OrderDto> event = ErafKafkaEvent.of("ORDER_CREATED", orderDto);
producer.send("order-topic", event);

// 메시지 수신
@Autowired
private ErafKafkaConsumer consumer;

consumer.subscribe("order-topic", event -> {
    OrderDto order = event.getPayload();
    // 처리
});
```

**자동 등록 빈:**
- ErafKafkaProducer
- ErafKafkaConsumer
- ErafKafkaErrorHandler (DLQ 지원)

### eraf-starter-batch

Spring Batch 자동 설정

```yaml
eraf:
  batch:
    enabled: true
    job:
      chunk-size: 100
      skip-limit: 10
      retry-limit: 3
    thread-pool:
      core-size: 4
      max-size: 8
```

```java
@Autowired
private ErafBatchJobBuilder jobBuilder;

Job job = jobBuilder
    .name("sampleJob")
    .chunk(100, SampleItem.class)
    .reader(itemReader)
    .processor(itemProcessor)
    .writer(itemWriter)
    .build();
```

**자동 등록 빈:**
- ErafBatchJobBuilder
- ErafJobListener (실행 로깅)
- ErafStepListener (스텝 로깅)

### eraf-starter-notification

알림 발송 (Email, SMS, Push)

```yaml
eraf:
  notification:
    email:
      enabled: true
      host: smtp.gmail.com
      port: 587
    sms:
      provider: twilio  # twilio, naver, nhn, aws-sns, custom
      twilio:
        account-sid: xxx
        auth-token: xxx
        from-number: +1234567890
    push:
      provider: fcm  # fcm, apns
      fcm:
        credentials-path: /path/to/firebase.json
```

```java
@Autowired
private NotificationService notificationService;

// 이메일
notificationService.sendEmail("to@email.com", "제목", "내용");

// SMS
notificationService.sendSms("010-1234-5678", "메시지");

// Push
notificationService.sendPush("deviceToken", "제목", "내용", Map.of("key", "value"));
```

### eraf-starter-scheduler

스케줄러 관리

```java
@ErafScheduled(name = "dailyReport", cron = "0 0 9 * * ?", description = "일간 리포트 생성")
public void generateDailyReport() {
    // 작업 실행
}

// 작업 조회
@Autowired
private ErafJobRegistry jobRegistry;

List<ErafJobInfo> jobs = jobRegistry.getAllJobs();
List<ErafJobHistory> history = jobRegistry.getJobHistory("dailyReport", 10);
```

### eraf-starter-statemachine

상태머신 관리

```java
@StateMachine(name = "order")
@Transitions({
    @Transition(from = "PENDING", to = "CONFIRMED", event = "CONFIRM"),
    @Transition(from = "CONFIRMED", to = "SHIPPED", event = "SHIP"),
    @Transition(from = "SHIPPED", to = "DELIVERED", event = "DELIVER"),
    @Transition(from = {"PENDING", "CONFIRMED"}, to = "CANCELLED", event = "CANCEL")
})
public class OrderStateMachine {
}

@Autowired
private ErafStateMachineService stateMachineService;

stateMachineService.transition("order", orderId, "PENDING", "CONFIRM");
```

## 설정 예시

```yaml
eraf:
  web:
    cors-enabled: true
    idempotent:
      enabled: true
    lock:
      enabled: true

  database:
    auditing-enabled: true
    code-repository-enabled: true
    audit-log-enabled: true

  jpa:
    auditing-enabled: true

  redis:
    lock:
      enabled: true
    sequence:
      enabled: true
    idempotent:
      enabled: true
      ttl: 24h

  notification:
    email:
      enabled: true
    sms:
      enabled: true
      provider: naver
    push:
      enabled: true
      provider: fcm
```

## 빌드

```bash
cd eraf-commons
mvn clean install
```

## 라이선스

MIT License
