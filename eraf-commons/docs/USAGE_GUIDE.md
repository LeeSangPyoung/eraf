# ERAF Commons 사용 가이드

## 목차
1. [시작하기](#시작하기)
2. [eraf-core 사용법](#eraf-core-사용법)
3. [모듈별 사용법](#모듈별-사용법)
4. [설정 예제](#설정-예제)

---

## 시작하기

### 요구사항

- **Java 23** 이상
- **Spring Boot 3.2.x** 이상
- **Maven 3.9.x** 이상

### Maven 의존성 추가

```xml
<!-- BOM 사용 (권장) -->
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

<!-- 필요한 모듈 추가 -->
<dependencies>
    <!-- 핵심 모듈 (필수) -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core</artifactId>
    </dependency>

    <!-- 웹 모듈 -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-web</artifactId>
    </dependency>

    <!-- 데이터 모듈 (필요시 선택) -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### 모듈 카테고리

| 카테고리 | 모듈 | 설명 |
|---------|------|------|
| **Core** | eraf-core | 핵심 유틸리티 (암호화, 마스킹, 검증 등) |
| **Web** | eraf-web, eraf-security, eraf-session, eraf-swagger | 웹 관련 기능 |
| **Data** | eraf-data-jpa, eraf-data-mybatis, eraf-data-redis, eraf-data-cache, eraf-data-database, eraf-data-elasticsearch | 데이터 처리 |
| **Messaging** | eraf-messaging-kafka, eraf-messaging-rabbitmq, eraf-messaging-common | 메시징 |
| **Integration** | eraf-integration-http, eraf-integration-ftp, eraf-integration-tcp, eraf-integration-s3 | 외부 연동 |
| **Batch** | eraf-batch, eraf-scheduler | 배치/스케줄링 |
| **기타** | eraf-statemachine, eraf-notification, eraf-actuator | 상태머신, 알림, 모니터링 |

---

## eraf-core 사용법

### 1. 암호화 (crypto)

```java
// AES-256-GCM 암호화 (알고리즘 고정)
String encrypted = Crypto.encrypt("민감한 데이터", secretKey);
String decrypted = Crypto.decrypt(encrypted, secretKey);

// 키 생성
String key = Crypto.generateKey();

// 비밀번호 해싱 (bcrypt)
String hash = Password.hash("myPassword123!");
boolean isValid = Password.verify("myPassword123!", hash);

// SHA-256 해시
String hashed = Hash.hash("data");
boolean matches = Hash.verify("data", hashed);

// JWT 토큰
Map<String, Object> claims = Map.of("userId", "user123", "role", "ADMIN");
String token = Jwt.create(claims, jwtSecretKey, 60); // 60분 만료
Claims result = Jwt.verify(token, jwtSecretKey);
```

### 2. 마스킹 (masking)

```java
// 이름: 홍길동 -> 홍*동
Masking.name("홍길동");

// 전화번호: 01012345678 -> 010-****-5678
Masking.phone("01012345678");

// 이메일: test@gmail.com -> te**@gmail.com
Masking.email("test@gmail.com");

// 카드번호: 1234567890125678 -> 1234-****-****-5678
Masking.card("1234567890125678");

// 주민번호: 9001011234567 -> 900101-*******
Masking.residentNo("9001011234567");

// 커스텀 마스킹
Masking.mask("ABCDEFG", 2, 3); // AB***FG
```

### 3. 검증 (validation)

```java
public class UserRequest {
    @Email
    private String email;

    @Phone(type = Phone.PhoneType.MOBILE)
    private String phone;

    @Password  // 복잡도 검증
    private String password;

    @BusinessNo  // 사업자번호 체크섬 검증
    private String businessNo;

    @NoXss
    private String content;

    @NoSqlInjection
    private String query;

    @FileExtension(allowed = {"jpg", "png", "pdf"})
    private String fileName;
}
```

### 4. 유틸리티 (utils)

```java
// 날짜
DateUtils.today();
DateUtils.format(LocalDate.now(), "yyyy-MM-dd");
DateUtils.parseDate("2024-01-15");
DateUtils.daysBetween(start, end);
DateUtils.addMonths(date, 3);
DateUtils.isWeekend(date);

// 문자열
StringUtils.isBlank(str);
StringUtils.defaultIfEmpty(str, "default");
StringUtils.truncate(str, 100);
StringUtils.leftPad("123", 5, '0'); // "00123"
StringUtils.toCamelCase("user_name"); // "userName"
StringUtils.toSnakeCase("userName"); // "user_name"

// 금액
MoneyUtils.format(1234567); // "1,234,567"
MoneyUtils.toKorean(3000000); // "일금 삼백만원정"

// ID 생성
IdGenerator.uuid();    // UUID
IdGenerator.ulid();    // ULID (시간순 정렬)
IdGenerator.nanoid();  // NanoID (짧은 ID)
```

### 5. 채번 (sequence)

```java
// 주문번호: ORD-20240115-00001
@Sequence(prefix = "ORD", reset = Reset.DAILY)
private String orderNo;

// 프로그래밍 방식
String seq = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);
```

### 6. 표준 응답 (response)

```java
// 성공 응답
return ApiResponse.success(data);
return ApiResponse.success(data, "처리 완료");

// 에러 응답
return ApiResponse.error(ErrorCode.USER_NOT_FOUND);
return ApiResponse.error("E001", "사용자를 찾을 수 없습니다");

// 페이징 응답
return PageResponse.of(page.getContent(), page);
```

### 7. HTTP 클라이언트 (http)

```java
// GET 요청
UserResponse user = ErafHttpClient.create()
    .baseUrl("https://api.example.com")
    .timeout(Duration.ofSeconds(30))
    .retry(3)
    .header("Authorization", "Bearer " + token)
    .get("/users/123", UserResponse.class);

// POST 요청
PaymentResponse result = ErafHttpClient.create()
    .baseUrl("https://api.pg.com")
    .timeout(Duration.ofSeconds(30))
    .post("/payments", request, PaymentResponse.class);
```

### 8. 파일/이미지/PDF

```java
// 파일
FileUtils.copy(source, target);
ZipUtils.compress(files, outputPath);
ZipUtils.decompress(zipFile, outputDir);

// 이미지
ImageUtils.resize(image, 800, 600);
ImageUtils.thumbnail(image, 150);
ImageUtils.watermark(image, "CONFIDENTIAL");

// PDF
PdfGenerator.fromHtml(html, outputPath);
PdfMerger.merge(pdfFiles, outputPath);

// Excel
List<Map<String, Object>> data = ExcelReader.read(excelFile);
ExcelWriter.write(data, outputPath);
```

### 9. 바코드/QR

```java
// 바코드 생성
byte[] barcode = BarcodeGenerator.generate("12345678", BarcodeFormat.CODE_128);

// QR코드 생성
byte[] qr = QRCodeGenerator.generate("https://example.com", 300, 300);

// 읽기
String value = BarcodeReader.read(imageFile);
```

---

## 모듈별 사용법

### 1. eraf-data-redis

```java
@Autowired
private ErafCache cache;

@Autowired
private ErafLock lock;

// 캐시
cache.put("key", value, Duration.ofMinutes(30));
Optional<User> user = cache.get("user:123", User.class);

// 분산 락
@DistributedLock(key = "'order:' + #orderId")
public void processOrder(String orderId) {
    // 동시 실행 방지
}
```

### 2. eraf-statemachine

```java
// 상태 머신 정의
@StateMachine(id = "order")
public class OrderStateMachine {

    @Transition(source = "CREATED", target = "PAID", event = "PAY")
    public void onPay(StateInfo state) {
        // 결제 처리
    }

    @Transition(source = "PAID", target = "SHIPPED", event = "SHIP")
    public void onShip(StateInfo state) {
        // 배송 처리
    }
}

// 사용
@Autowired
private ErafStateMachineService stateMachine;

stateMachine.initialize("order", orderId);
stateMachine.sendEvent("order", orderId, "PAY");
String currentState = stateMachine.getCurrentState("order", orderId);
```

### 3. eraf-notification

```java
@Autowired
private ErafNotification notification;

// 이메일
notification.email()
    .to("user@example.com")
    .subject("가입을 축하합니다")
    .template("welcome", Map.of("userName", "홍길동"))
    .send();

// SMS
notification.sms()
    .to("01012345678")
    .message("인증번호: 123456")
    .send();

// 슬랙
notification.slack()
    .channel("#alerts")
    .message("서버 오류 발생!")
    .send();
```

### 4. eraf-scheduler

```java
@ErafScheduled(cron = "0 0 9 * * *")  // 매일 오전 9시
public void dailyReport() {
    // 일일 리포트 생성
}

@ErafScheduled(fixedRate = 60000)  // 1분마다
public void healthCheck() {
    // 헬스 체크
}
```

### 5. eraf-integration-http

```java
// 선언적 API 클라이언트
@ErafClient(name = "user-service", url = "${services.user.url}")
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable String id);

    @PostMapping("/users")
    UserResponse createUser(@RequestBody UserRequest request);
}

// 사용
@Autowired
private UserServiceClient userClient;

UserResponse user = userClient.getUser("123");
```

### 6. eraf-actuator

```yaml
eraf:
  actuator:
    health-enabled: true
    metrics-enabled: true
    health:
      redis:
        enabled: true
      database:
        enabled: true
      kafka:
        enabled: true
```

**자동 등록 Health Indicator:**
- Redis 연결 상태 확인
- Database 연결 상태 확인
- Kafka 클러스터 상태 확인

### 7. eraf-swagger

```yaml
eraf:
  swagger:
    enabled: true
    api-info:
      title: My API
      description: API 문서
      version: 1.0.0
    security:
      enabled: true  # JWT Bearer 인증 자동 설정
```

**Swagger UI 접속:** `http://localhost:8080/swagger-ui.html`

### 8. eraf-messaging-kafka

```java
// Producer
@Autowired
private ErafKafkaProducer producer;

ErafKafkaEvent<OrderDto> event = ErafKafkaEvent.of("ORDER_CREATED", orderDto);
producer.send("order-topic", event);
producer.sendSync("order-topic", event);  // 동기 전송

// Consumer
@Autowired
private ErafKafkaConsumer consumer;

consumer.subscribe("order-topic", event -> {
    // DLQ 자동 전송 (에러 발생 시)
    processOrder(event.getPayload());
});
```

### 9. eraf-batch

```java
@Autowired
private ErafBatchJobBuilder jobBuilder;

// 청크 기반 Job 생성
Job job = jobBuilder
    .name("importJob")
    .chunk(100, Item.class)
    .reader(flatFileItemReader())
    .processor(item -> transform(item))
    .writer(items -> save(items))
    .skipLimit(10)
    .retryLimit(3)
    .build();
```

### 10. 파일 업로드/다운로드 (eraf-web)

```java
@Autowired
private FileStorageService fileStorage;

// 업로드
@PostMapping("/upload")
public ApiResponse<?> upload(@RequestParam MultipartFile file) {
    // 파일 검증
    FileValidationUtils.validateImage(file, 10).throwIfInvalid();

    // 저장 (날짜별 디렉토리 자동 생성)
    StoredFile stored = fileStorage.store(file);
    return ApiResponse.success(stored);
}

// 다운로드
@GetMapping("/download/{filePath}")
public ResponseEntity<Resource> download(@PathVariable String filePath) {
    Resource resource = fileStorage.load(filePath);
    return FileDownloadHelper.download(resource, "파일명.pdf");
}

// 인라인 보기 (이미지 등)
@GetMapping("/view/{filePath}")
public ResponseEntity<Resource> view(@PathVariable String filePath) {
    Resource resource = fileStorage.load(filePath);
    return FileDownloadHelper.inline(resource, "이미지.png");
}
```

### 11. 요청/응답 로깅 (eraf-web)

```yaml
eraf:
  web:
    logging:
      enabled: true
      include-payload: true
      max-payload-length: 1000
      exclude-patterns: ["/actuator", "/health", "/favicon.ico"]
```

**자동 기능:**
- TraceId 자동 생성 및 전파 (X-Trace-Id 헤더)
- 요청/응답 본문 로깅
- 민감정보 자동 마스킹 (password, token, secret 등)
- 처리 시간 측정

---

## 설정 예제

### 최소 구성

```yaml
eraf:
  crypto:
    secret-key: ${ERAF_CRYPTO_SECRET_KEY}
    jwt:
      secret-key: ${ERAF_JWT_SECRET_KEY}
```

### Redis 사용 시

```yaml
eraf:
  statemachine:
    store-type: redis
    state-ttl: 7d

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 알림 사용 시

```yaml
eraf:
  notification:
    email:
      provider: smtp
      smtp:
        host: smtp.gmail.com
        port: 587
        username: ${SMTP_USERNAME}
        password: ${SMTP_PASSWORD}
    sms:
      provider: nhn
```

### PDF 한글 출력 시

```yaml
eraf:
  pdf:
    font-path: /fonts/NanumGothic.ttf
```

### Kafka 사용 시

```yaml
eraf:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: my-group
    dlq:
      enabled: true
      topic-suffix: .dlq
```

### Batch 사용 시

```yaml
eraf:
  batch:
    job:
      chunk-size: 100
      skip-limit: 10
    thread-pool:
      core-size: 4
      max-size: 8
```

### Swagger 사용 시

```yaml
eraf:
  swagger:
    enabled: true
    api-info:
      title: ${spring.application.name} API
      version: ${project.version}
    security:
      enabled: true
```

### 파일 업로드 설정

```yaml
eraf:
  web:
    file-upload:
      enabled: true
      upload-path: /data/uploads
      base-url: /files
      max-file-size-mb: 50
      create-date-directory: true

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 100MB
```

---

## 전체 설정 예시 (application.yml)

```yaml
eraf:
  # 암호화 설정
  crypto:
    secret-key: ${ERAF_CRYPTO_SECRET_KEY}
    jwt:
      secret-key: ${ERAF_JWT_SECRET_KEY}

  # 웹 설정
  web:
    cors-enabled: true
    cors-allowed-origins: ["*"]
    logging:
      enabled: true
      include-payload: true
    file-upload:
      enabled: true
      upload-path: ./uploads
      max-file-size-mb: 10

  # Health Check
  actuator:
    health-enabled: true
    health:
      redis:
        enabled: true
      database:
        enabled: true

  # API 문서화
  swagger:
    enabled: true
    api-info:
      title: My Application API
      version: 1.0.0

  # Kafka
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    dlq:
      enabled: true

  # Batch
  batch:
    job:
      chunk-size: 100

  # 상태머신
  statemachine:
    store-type: redis
    state-ttl: 7d

  # 알림
  notification:
    email:
      enabled: true
    sms:
      enabled: true
      provider: nhn
```

---

## 문의

이슈 및 기능 요청: [GitHub Issues](https://github.com/your-org/eraf-commons/issues)
