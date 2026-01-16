# ERAF Common Module 설계서 v1.4 추가 내용

> 이 문서는 기존 설계서(v1.3)에 추가할 새로운 기능들을 정리한 것입니다.
> docx 설계서에 아래 내용을 추가해주세요.

---

## 1. 신규 Starter 모듈

### 1.1 eraf-starter-swagger (API 문서화)

#### 개요
SpringDoc OpenAPI 3 기반의 API 문서화 자동 설정 모듈

#### 주요 기능
- OpenAPI 3.0 스펙 기반 API 문서 자동 생성
- JWT Bearer 인증 스키마 자동 설정
- API 그룹화 지원
- Swagger UI 제공

#### 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.swagger.enabled | true | Swagger 활성화 |
| eraf.swagger.api-info.title | ERAF API Documentation | API 문서 제목 |
| eraf.swagger.api-info.description | - | API 설명 |
| eraf.swagger.api-info.version | 1.0.0 | API 버전 |
| eraf.swagger.security.enabled | true | JWT 인증 스키마 활성화 |
| eraf.swagger.security.scheme | bearer | 인증 스키마 |
| eraf.swagger.security.bearer-format | JWT | Bearer 형식 |
| eraf.swagger.group.paths-to-match | ["/**"] | 문서화할 경로 패턴 |
| eraf.swagger.group.paths-to-exclude | ["/actuator/**"] | 제외할 경로 패턴 |

#### 자동 등록 빈
- `OpenAPI`: API 문서 정보 및 보안 스키마
- `GroupedOpenApi`: API 그룹 설정

#### 클래스 다이어그램
```
ErafSwaggerAutoConfiguration
├── ErafSwaggerProperties
│   ├── ApiInfo
│   │   ├── Contact
│   │   └── License
│   ├── Security
│   └── Group
├── OpenAPI (Bean)
└── GroupedOpenApi (Bean)
```

---

### 1.2 eraf-starter-kafka (Kafka 메시징)

#### 개요
Apache Kafka 프로듀서/컨슈머 자동 설정 및 DLQ(Dead Letter Queue) 지원

#### 주요 기능
- 표준화된 이벤트 메시지 포맷 (ErafKafkaEvent)
- 프로듀서/컨슈머 자동 설정
- DLQ 자동 전송 (에러 발생 시)
- 재시도 정책 설정
- 트랜잭션 지원

#### 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.kafka.bootstrap-servers | localhost:9092 | Kafka 서버 주소 |
| eraf.kafka.consumer.group-id | - | 컨슈머 그룹 ID |
| eraf.kafka.consumer.auto-offset-reset | earliest | 오프셋 리셋 정책 |
| eraf.kafka.producer.acks | all | ACK 정책 |
| eraf.kafka.producer.retries | 3 | 재시도 횟수 |
| eraf.kafka.retry.max-attempts | 3 | 최대 재시도 횟수 |
| eraf.kafka.retry.backoff-ms | 1000 | 재시도 대기 시간(ms) |
| eraf.kafka.dlq.enabled | true | DLQ 활성화 |
| eraf.kafka.dlq.topic-suffix | .dlq | DLQ 토픽 접미사 |
| eraf.kafka.transaction.enabled | false | 트랜잭션 활성화 |

#### 핵심 클래스

**ErafKafkaEvent<T>**
```java
public class ErafKafkaEvent<T> {
    private String eventId;      // 이벤트 고유 ID
    private String eventType;    // 이벤트 타입
    private T payload;           // 페이로드
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}
```

**ErafKafkaProducer**
- `send(topic, event)`: 비동기 전송
- `sendSync(topic, event)`: 동기 전송
- `sendWithKey(topic, key, event)`: 키 지정 전송

**ErafKafkaConsumer**
- `subscribe(topic, handler)`: 토픽 구독
- `subscribe(topics, handler)`: 다중 토픽 구독

**ErafKafkaErrorHandler**
- 메시지 처리 실패 시 DLQ 자동 전송
- 에러 정보 포함 (원본 토픽, 파티션, 오프셋, 에러 메시지)

#### 클래스 다이어그램
```
ErafKafkaAutoConfiguration
├── ErafKafkaProperties
│   ├── ConsumerConfig
│   ├── ProducerConfig
│   ├── RetryConfig
│   ├── DlqConfig
│   └── TransactionConfig
├── ErafKafkaProducer (Bean)
├── ErafKafkaConsumer (Bean)
└── ErafKafkaErrorHandler (Bean)
    └── DlqMessage (Inner Class)
```

---

### 1.3 eraf-starter-batch (Spring Batch)

#### 개요
Spring Batch 잡 빌더 및 리스너 자동 설정

#### 주요 기능
- Fluent API 기반 Job 빌더
- 잡/스텝 실행 로깅
- 재시도/스킵 정책 설정
- 스레드 풀 설정

#### 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.batch.enabled | true | Batch 활성화 |
| eraf.batch.job.chunk-size | 100 | 청크 크기 |
| eraf.batch.job.skip-limit | 10 | 스킵 제한 |
| eraf.batch.job.retry-limit | 3 | 재시도 제한 |
| eraf.batch.thread-pool.core-size | 4 | 코어 스레드 수 |
| eraf.batch.thread-pool.max-size | 8 | 최대 스레드 수 |

#### 핵심 클래스

**ErafBatchJobBuilder**
```java
Job job = erafBatchJobBuilder
    .name("sampleJob")
    .chunk(100, Item.class)
    .reader(itemReader)
    .processor(itemProcessor)
    .writer(itemWriter)
    .skipLimit(10)
    .retryLimit(3)
    .listener(customListener)
    .build();
```

**ErafJobListener**
- 잡 시작/종료 시 로깅
- 실행 시간 측정
- 실패 정보 로깅

**ErafStepListener**
- 스텝 시작/종료 시 로깅
- 읽기/처리/쓰기 카운트 로깅
- 스킵/에러 카운트 로깅

---

## 2. eraf-starter-actuator 확장

### 2.1 추가된 Health Indicator

#### RedisHealthIndicator
- Redis 연결 상태 확인 (PING/PONG)
- 조건부 등록: `RedisConnectionFactory` 빈 존재 시

#### DatabaseHealthIndicator
- DB 연결 상태 확인 (SELECT 1)
- DB 제품명, 버전, URL 정보 제공
- 조건부 등록: `DataSource` 빈 존재 시

#### KafkaHealthIndicator
- Kafka 클러스터 연결 상태 확인
- 클러스터 ID, 노드 수 정보 제공
- 조건부 등록: `KafkaAdmin` 빈 존재 시

### 2.2 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.actuator.health.redis.enabled | true | Redis Health 활성화 |
| eraf.actuator.health.database.enabled | true | DB Health 활성화 |
| eraf.actuator.health.kafka.enabled | true | Kafka Health 활성화 |

---

## 3. eraf-starter-web 확장

### 3.1 RequestLoggingFilter

#### 개요
HTTP 요청/응답 로깅 및 TraceId 전파 필터

#### 주요 기능
- TraceId 자동 생성 및 전파 (X-Trace-Id 헤더)
- MDC를 통한 TraceId 로깅 연동
- 요청/응답 본문 로깅 (설정 가능)
- 민감 정보 자동 마스킹
- 처리 시간 측정

#### 마스킹 대상 필드
- password, passwd, pwd
- token, accessToken, refreshToken
- secret, secretKey
- authorization
- credit, card

#### 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.web.logging.enabled | true | 로깅 활성화 |
| eraf.web.logging.include-payload | true | 본문 로깅 포함 |
| eraf.web.logging.max-payload-length | 1000 | 최대 본문 길이 |
| eraf.web.logging.exclude-patterns | ["/actuator", "/health", "/favicon.ico"] | 제외 경로 |

### 3.2 FileStorageService

#### 개요
파일 업로드/다운로드 서비스 인터페이스 및 로컬 구현체

#### 주요 기능
- 파일 저장 (단일/다중)
- 날짜별 디렉토리 자동 생성
- UUID 기반 파일명 생성
- 파일 로드/삭제/복사/이동
- MD5 체크섬 계산
- Path Traversal 공격 방지

#### 설정 속성

| 속성 | 기본값 | 설명 |
|-----|-------|------|
| eraf.web.file-upload.enabled | true | 파일 업로드 활성화 |
| eraf.web.file-upload.upload-path | ./uploads | 저장 경로 |
| eraf.web.file-upload.base-url | /files | 파일 URL prefix |
| eraf.web.file-upload.create-date-directory | true | 날짜별 디렉토리 |
| eraf.web.file-upload.max-file-size-mb | 10 | 최대 파일 크기(MB) |

---

## 4. eraf-core 신규 기능

### 4.1 파일 처리 유틸리티 (com.eraf.core.file)

#### 클래스 목록

| 클래스 | 설명 |
|-------|------|
| FileStorageService | 파일 저장소 인터페이스 |
| LocalFileStorageService | 로컬 파일 시스템 구현체 |
| StoredFile | 저장된 파일 정보 DTO |
| FileDownloadHelper | 파일 다운로드 응답 생성 헬퍼 |
| FileValidationUtils | 파일 유효성 검사 유틸리티 |
| FileStorageException | 파일 저장소 예외 |
| FileNotFoundException | 파일 미발견 예외 |

#### FileStorageService 메서드

| 메서드 | 설명 |
|-------|------|
| store(MultipartFile) | 파일 저장 |
| store(MultipartFile, directory) | 지정 디렉토리에 저장 |
| store(MultipartFile, directory, filename) | 지정 파일명으로 저장 |
| store(InputStream, directory, filename, contentType) | 스트림으로 저장 |
| storeAll(List<MultipartFile>) | 다중 파일 저장 |
| load(filePath) | 파일 로드 (Resource) |
| exists(filePath) | 존재 여부 확인 |
| delete(filePath) | 파일 삭제 |
| deleteAll(List<filePath>) | 다중 삭제 |
| deleteDirectory(directory) | 디렉토리 삭제 |
| copy(source, dest) | 파일 복사 |
| move(source, dest) | 파일 이동 |
| getUrl(filePath) | 파일 URL 반환 |

#### FileValidationUtils 메서드

| 메서드 | 설명 |
|-------|------|
| isAllowedExtension(file, extensions) | 확장자 검증 |
| isImageFile(file) | 이미지 파일 여부 |
| isDocumentFile(file) | 문서 파일 여부 |
| isDangerousFile(file) | 위험 파일 여부 |
| isValidSize(file, maxSize) | 크기 검증 |
| validate(file, extensions, maxSizeMB) | 종합 검증 |
| validateImage(file, maxSizeMB) | 이미지 검증 |
| validateDocument(file, maxSizeMB) | 문서 검증 |

#### FileDownloadHelper 메서드

| 메서드 | 설명 |
|-------|------|
| download(resource, filename) | 다운로드 응답 생성 |
| inline(resource, filename) | 인라인 표시 응답 |
| getContentType(filename) | Content-Type 추출 |
| encodeFilename(filename) | 파일명 URL 인코딩 |
| isImage/isVideo/isAudio/isDocument | 파일 타입 확인 |

---

## 5. 클래스 다이어그램 (전체)

```
eraf-commons/
├── eraf-core/
│   └── com.eraf.core.file/
│       ├── FileStorageService (Interface)
│       ├── LocalFileStorageService
│       ├── StoredFile
│       ├── FileDownloadHelper
│       ├── FileValidationUtils
│       │   └── ValidationResult
│       ├── FileStorageException
│       └── FileNotFoundException
│
├── eraf-starter-web/
│   ├── ErafWebAutoConfiguration
│   ├── ErafWebProperties
│   │   ├── LoggingConfig
│   │   └── FileUploadConfig
│   └── filter/
│       └── RequestLoggingFilter
│
├── eraf-starter-actuator/
│   ├── ErafActuatorAutoConfiguration
│   ├── ErafActuatorProperties
│   │   └── HealthConfig
│   └── health/
│       ├── RedisHealthIndicator
│       ├── DatabaseHealthIndicator
│       └── KafkaHealthIndicator
│
├── eraf-starter-swagger/
│   ├── ErafSwaggerAutoConfiguration
│   └── ErafSwaggerProperties
│       ├── ApiInfo
│       ├── Contact
│       ├── License
│       ├── Security
│       └── Group
│
├── eraf-starter-kafka/
│   ├── ErafKafkaAutoConfiguration
│   ├── ErafKafkaProperties
│   ├── ErafKafkaProducer
│   ├── ErafKafkaConsumer
│   ├── ErafKafkaEvent<T>
│   └── ErafKafkaErrorHandler
│       └── DlqMessage
│
└── eraf-starter-batch/
    ├── ErafBatchAutoConfiguration
    ├── ErafBatchProperties
    ├── ErafBatchJobBuilder
    ├── ErafJobListener
    ├── ErafStepListener
    └── ErafItemProcessor<I,O>
```

---

## 6. 버전 변경 이력

### v1.4 (현재)
- **신규 모듈**
  - eraf-starter-swagger: API 문서화 (OpenAPI 3)
  - eraf-starter-kafka: Kafka 메시징 (DLQ 지원)
  - eraf-starter-batch: Spring Batch 자동 설정

- **기능 확장**
  - eraf-starter-actuator: Redis/DB/Kafka Health Indicator 추가
  - eraf-starter-web: RequestLoggingFilter, FileStorageService 추가
  - eraf-core: 파일 처리 유틸리티 패키지 추가

- **개선 사항**
  - TraceId 자동 전파 및 MDC 연동
  - 민감정보 자동 마스킹
  - 파일 업로드 보안 강화 (Path Traversal 방지)
