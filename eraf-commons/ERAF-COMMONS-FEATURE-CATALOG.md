# ERAF Commons - Enterprise Reusable Asset Factory

## 기능 카탈로그 v2.0

> **기술 스택**: Spring Boot 3.2.11 / Java 21 / Maven Multi-Module
> **총 모듈 수**: 47개 (BOM 1 + 기능 모듈 46)
> **총 Java 클래스**: 578개
> **Spring Cloud**: 2023.0.3

---

# 목차

| # | 카테고리 | 모듈 | 클래스 수 | 신규 |
|---|---------|------|----------|------|
| 1 | **Core - Context** | eraf-core | 10 | |
| 2 | **Core - Async** | eraf-core-async | 13 | NEW |
| 3 | **Core - Crypto** | eraf-core-crypto | 12 | NEW |
| 4 | **Core - Exception** | eraf-core-exception | 10 | NEW |
| 5 | **Core - HTTP** | eraf-core-http | 5 | NEW |
| 6 | **Core - I18n** | eraf-core-i18n | 6 | NEW |
| 7 | **Core - Resilience** | eraf-core-resilience | 33 | NEW |
| 8 | **Core - System** | eraf-core-system | 12 | NEW |
| 9 | **Core - Util** | eraf-core-util | 43 | NEW |
| 10 | **Core - Validation** | eraf-core-validation | 17 | NEW |
| 11 | **Web** | eraf-web | 15 | |
| 12 | **Security** | eraf-security | 37 | |
| 13 | **Session** | eraf-session | 6 | |
| 14 | **API Documentation** | eraf-swagger | 2 | |
| 15 | **Data - JPA** | eraf-data-jpa | 51 | |
| 16 | **Data - Redis** | eraf-data-redis | 17 | |
| 17 | **Data - MongoDB** | eraf-data-mongo | 11 | NEW |
| 18 | **Data - Elasticsearch** | eraf-data-elasticsearch | 5 | |
| 19 | **Data - MyBatis** | eraf-data-mybatis | 6 | |
| 20 | **Data - Cache** | eraf-data-cache | 5 | |
| 21 | **Messaging - Kafka** | eraf-messaging-kafka | 13 | |
| 22 | **Messaging - RabbitMQ** | eraf-messaging-rabbitmq | 5 | |
| 23 | **Integration - FTP** | eraf-integration-ftp | 5 | |
| 24 | **Integration - TCP** | eraf-integration-tcp | 4 | |
| 25 | **Integration - S3** | eraf-integration-s3 | 5 | |
| 26 | **Integration - HTTP** | eraf-integration-http | 5 | |
| 27 | **Integration - gRPC** | eraf-integration-grpc | 6 | NEW |
| 28 | **Integration - WebSocket** | eraf-integration-websocket | 6 | NEW |
| 29 | **Config** | eraf-config | 9 | NEW |
| 30 | **Batch** | eraf-batch | 6 | |
| 31 | **Scheduler** | eraf-scheduler | 7 | |
| 32 | **Saga** | eraf-saga | 27 | |
| 33 | **State Machine** | eraf-statemachine | 17 | |
| 34 | **Workflow** | eraf-workflow | 16 | NEW |
| 35 | **Outbox** | eraf-outbox | 5 | NEW |
| 36 | **Feature Flag** | eraf-feature-flag | 22 | NEW |
| 37 | **Gateway** | eraf-gateway | 6 | NEW |
| 38 | **Actuator** | eraf-actuator | 29 | |
| 39 | **Observability** | eraf-observability | 8 | NEW |
| 40 | **Notification** | eraf-notification | 27 | |
| 41 | **Report** | eraf-report | 12 | NEW |
| 42 | **Excel** | eraf-excel | 4 | |
| 43 | **PDF** | eraf-pdf | 5 | |
| 44 | **Barcode** | eraf-barcode | 4 | |
| 45 | **Image** | eraf-image | 2 | |
| 46 | **Test** | eraf-test | 7 | NEW |
| 47 | **BOM** | eraf-bom | - | |

---

# A. Core 카테고리

> v1.0의 모놀리식 eraf-core (147 클래스)가 10개의 독립 모듈로 분해됨

---

## A-1. eraf-core (컨텍스트 및 기본 인프라) — 10 클래스

> 요청 컨텍스트, API 응답 표준화, 메시징 추상화

### A-1-1. 요청 컨텍스트 (context)

| # | 기능 | 설명 |
|---|------|------|
| 1 | ErafContext | ThreadLocal 기반 요청별 컨텍스트 관리 |
| 2 | ErafContextHolder | 컨텍스트 저장/조회/초기화 |
| 3 | 추적 ID 관리 | 요청별 고유 Trace ID, Request ID 자동 생성 |
| 4 | 사용자 정보 저장 | 현재 요청의 userId, username, clientIp, userAgent 저장 |
| 5 | 커스텀 속성 저장소 | 요청 범위에서 임의의 키-값 속성 저장/조회 |

### A-1-2. API 응답 표준화 (response)

| # | 기능 | 설명 |
|---|------|------|
| 1 | ApiResponse | 성공/실패 통일 응답 형식 (타임스탬프 자동 포함) |
| 2 | ErrorResponse | 필드별 오류 정보 포함 에러 응답 |
| 3 | PageResponse | 페이징 응답 (페이지 정보, 총 건수, 데이터) |

### A-1-3. 메시징 추상화 (messaging)

| # | 기능 | 설명 |
|---|------|------|
| 1 | ErafMessage 모델 | 표준 메시지 포맷 |
| 2 | MessagePublisher | 발행 추상화 인터페이스 |
| 3 | MessageListener | 수신 추상화 인터페이스 |

---

## A-2. eraf-core-async (비동기 처리) — 13 클래스

> CompletableFuture 기반 비동기 작업 관리 및 상태 추적

| # | 기능 | 설명 |
|---|------|------|
| 1 | AsyncTask / AsyncTaskManager | CompletableFuture 기반 비동기 작업 생성 및 실행 |
| 2 | 작업 상태 추적 | PENDING/RUNNING/COMPLETED/FAILED/CANCELLED 상태 관리 |
| 3 | 진행률 콜백 | 비동기 작업의 진행 상황을 실시간으로 콜백 수신 |
| 4 | 타임아웃 제어 | 비동기 작업별 타임아웃 시간 설정 |
| 5 | 자동 구성 | @AutoConfiguration 기반 AsyncTaskManager 자동 등록 |

---

## A-3. eraf-core-crypto (암호화 및 보안) — 12 클래스

> AES, RSA, SHA, HMAC, JWT, bcrypt 등 암호화 전체 기능

| # | 기능 | 설명 |
|---|------|------|
| 1 | AES-256-GCM 대칭키 암호화 | IV 자동 생성 포함 대칭키 암/복호화 |
| 2 | SHA-256 해싱 | Hex 또는 Base64 출력 형식 선택 가능 |
| 3 | HMAC-SHA256 서명 | 메시지 무결성 검증용 HMAC 서명 생성 및 검증 |
| 4 | JWT 토큰 (HS256) | JWT 생성/검증, 만료 처리, 클레임 추출 |
| 5 | JWT 토큰 (RS256) | RSA 공개키/개인키 기반 JWT 생성/검증 |
| 6 | RSA-2048 비대칭 서명 | 키 쌍 생성, Base64 인코딩/디코딩 |
| 7 | 비밀번호 해싱 | bcrypt 기반 (cost factor 조정 가능) |
| 8 | CryptoException | 암호화 관련 통합 예외 |

---

## A-4. eraf-core-exception (예외 처리) — 10 클래스

> 비즈니스/시스템/검증 예외 및 전역 핸들링

| # | 기능 | 설명 |
|---|------|------|
| 1 | BusinessException | ErrorCode 기반 비즈니스 예외 (메시지 포맷팅 지원) |
| 2 | SystemException | 시스템 레벨 예외 처리 |
| 3 | ValidationException | 필드별 오류 정보 포함 검증 예외 (Builder 패턴) |
| 4 | ErrorCode 인터페이스 | 프로젝트별 에러 코드 정의 인터페이스 |
| 5 | CommonErrorCode | BAD_REQUEST, UNAUTHORIZED, FORBIDDEN 등 공통 에러 코드 |
| 6 | GlobalExceptionHandler | 전역 예외 핸들러 (HTTP 상태 코드 매핑, 표준 응답 변환) |

---

## A-5. eraf-core-http (HTTP 통신) — 5 클래스

> OkHttp 기반 HTTP 클라이언트

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTTP 클라이언트 | OkHttp 기반 GET/POST/PUT/DELETE/PATCH 요청 |
| 2 | 파일 업로드/다운로드 | HTTP를 통한 파일 전송 |
| 3 | 스트리밍 | 대용량 응답 스트리밍 처리 |
| 4 | 재시도 | 요청 실패 시 자동 재시도 |
| 5 | 쿠키 관리 | 쿠키 조회/설정/삭제 |

---

## A-6. eraf-core-i18n (다국어 지원) — 6 클래스

> Spring MessageSource 통합 다국어 메시지 처리

| # | 기능 | 설명 |
|---|------|------|
| 1 | 로케일 리졸버 | 요청별 로케일 자동 결정 |
| 2 | 메시지 서비스 | Spring MessageSource 통합 다국어 메시지 조회 |
| 3 | @Message 어노테이션 | AOP 기반 메시지 국제화 자동 처리 |
| 4 | 메시지 존재 확인 | 메시지 코드 존재 여부 확인, 기본값 지원 |
| 5 | 자동 구성 | MessageSource 빈 자동 등록 |

---

## A-7. eraf-core-resilience (복원력 패턴) — 33 클래스

> Circuit Breaker, Retry, Timeout, Rate Limit, Bulkhead, Lock, Idempotent

| # | 기능 | 설명 |
|---|------|------|
| 1 | @CircuitBreaker | CLOSED/OPEN/HALF_OPEN 상태 전이, 장애 자동 감지 및 차단 |
| 2 | CircuitBreakerRegistry | 여러 Circuit Breaker 인스턴스 중앙 관리 |
| 3 | @RateLimit | Token Bucket 알고리즘 기반 요청 속도 제한 |
| 4 | RateLimiter.Registry | Rate Limiter 인스턴스 중앙 관리 |
| 5 | @Retry | 지수 백오프, 재시도 대상 예외 지정, 최대 재시도 횟수 설정 |
| 6 | @Timeout | 메서드 실행 시간 제한, 초과 시 예외 발생 |
| 7 | @Bulkhead | 스레드풀 격리, 동시 실행 수 제한 |
| 8 | @DistributedLock | SpEL 키 기반 분산 락 (대기/유지 시간 설정) |
| 9 | @OptimisticRetry | 낙관적 잠금 충돌 시 자동 재시도 |
| 10 | @Idempotent | 멱등성 보장 (중복 요청 방지, 이전 결과 반환) |
| 11 | LockProvider | 락 저장소 추상화 (InMemory 기본 제공, Redis 확장) |
| 12 | IdempotencyStore | 멱등성 저장소 추상화 (InMemory 기본 제공, Redis 확장) |
| 13 | AOP 기반 적용 | 모든 패턴을 어노테이션으로 선언적 적용 |

---

## A-8. eraf-core-system (공통코드 & 시퀀스) — 12 클래스

> 공통코드 관리 및 채번 시스템

### A-8-1. 공통코드 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | CodeService | 코드 CRUD, 그룹별 조회, 활성화/비활성화 |
| 2 | @Code 어노테이션 | 입력값의 공통코드 유효성 자동 검증 |
| 3 | CodeRepository | 저장소 추상화 (InMemory 기본 제공, JPA 확장) |
| 4 | CodeItem 모델 | 그룹, 코드, 이름, 설명, 정렬순서, 활성화 여부 |

### A-8-2. 시퀀스 생성

| # | 기능 | 설명 |
|---|------|------|
| 1 | @GenerateSequence | 시퀀스 값 자동 생성 및 필드 주입 |
| 2 | SequenceGenerator | 일/월/년 단위 자동 리셋, 커스텀 포맷 패턴 |
| 3 | SequenceAspect | AOP 기반 자동 채번 |

---

## A-9. eraf-core-util (유틸리티) — 43 클래스

> 22개 정적 유틸리티, 파일 저장소, 템플릿 엔진, 데이터 변환

### A-9-1. 유틸리티 클래스 (22개)

| # | 유틸리티 | 주요 기능 |
|---|---------|----------|
| 1 | StringUtils | 270+ 메서드: null/empty 체크, 패딩, 케이스 변환(camelCase/snake_case/kebab-case) |
| 2 | JsonUtils | Jackson 래핑 JSON 파싱, null-safe 처리 |
| 3 | DateUtils | 날짜/시간 변환, 계산, 포맷팅 (Java 8 Time API) |
| 4 | CollectionUtils | null-safe 컬렉션 필터링, 매핑, 변환 |
| 5 | MapUtils | null-safe Map 조회, 병합, 변환 |
| 6 | ArrayUtils | 배열 병합, 검색, 변환 |
| 7 | NumberUtils | 숫자 변환, 검증 |
| 8 | BooleanUtils | null-safe 불린 처리, 논리 연산 |
| 9 | MoneyUtils | BigDecimal 기반 금액 처리, 부동소수점 오류 방지 |
| 10 | IdGenerator | UUID, Snowflake 등 고유 ID 생성 |
| 11 | RandomUtils | 난수 문자열, 숫자, UUID 생성 |
| 12 | IpUtils | 클라이언트 IP 추출, IP 주소 검증 |
| 13 | UrlUtils | URL 파싱, 인코딩, 쿼리 파라미터 처리 |
| 14 | PathMatcher | 와일드카드 기반 경로 패턴 매칭 |
| 15 | RegexUtils | 정규표현식 매칭, 교체, 추출 |
| 16 | ReflectionUtils | 리플렉션 필드 접근, 메서드 호출 |
| 17 | ExceptionUtils | 스택 트레이스 처리, 루트 원인 추출 |
| 18 | Base64Utils | Base64 인코딩/디코딩 |
| 19 | EnumUtils | Enum 이름 조회, 변환 |
| 20 | IoUtils | 스트림 처리 유틸리티 |
| 21 | ObjectUtils | null-safe 비교, 해시 |
| 22 | SystemUtils | OS, Java 버전 등 시스템 정보 조회 |

### A-9-2. 파일 관리 (file)

| # | 기능 | 설명 |
|---|------|------|
| 1 | FileStorageService | 파일 저장소 추상화 인터페이스 (로컬/S3/FTP 확장) |
| 2 | LocalFileStorageService | 로컬 파일 시스템 구현체 |
| 3 | FileTypeDetector | 매직 넘버 기반 + 확장자 기반 파일 타입 식별 |
| 4 | FileValidationUtils | 파일 크기, 확장자, 타입 검증 |
| 5 | FileDownloadHelper | 브라우저 호환성 보장 다운로드 헬퍼 |

### A-9-3. 데이터 변환 (converter)

| # | 기능 | 설명 |
|---|------|------|
| 1 | JsonConverter | JSON ↔ 객체 변환 (Pretty Print, List/Map 지원) |
| 2 | XmlConverter | XML ↔ 객체 변환 |
| 3 | MapConverter | Object ↔ Map 변환 (깊은 복사, 병합) |
| 4 | BaseMapper | 제네릭 Entity ↔ DTO 매핑 |

### A-9-4. 템플릿 엔진 (template)

| # | 기능 | 설명 |
|---|------|------|
| 1 | TemplateEngine | 템플릿 처리 추상화 인터페이스 |
| 2 | ThymeleafTemplateEngine | Thymeleaf 기반 구현체 |
| 3 | FreemarkerTemplateEngine | FreeMarker 기반 구현체 |
| 4 | AdvancedTemplateEngine | 복합 템플릿 처리 |

---

## A-10. eraf-core-validation (입력값 검증) — 17 클래스

> Jakarta Bean Validation 기반 커스텀 검증 어노테이션

| # | 검증 어노테이션 | 설명 |
|---|---------------|------|
| 1 | @BusinessNo | 사업자번호 형식 및 체크섬 검증 |
| 2 | @Email | RFC 표준 이메일 형식 검증 |
| 3 | @Phone | 전화번호 형식 검증 (한국 번호 지원) |
| 4 | @Password | 비밀번호 강도 검증 (길이, 대소문자, 특수문자) |
| 5 | @FileExtension | 파일 확장자 화이트리스트 검증 |
| 6 | @NoXss | XSS(Cross-Site Scripting) 공격 방지 |
| 7 | @NoSqlInjection | SQL 인젝션 공격 방지 |
| 8 | @NoPathTraversal | 경로 탐색(Path Traversal) 공격 방지 |

---

# B. Web 카테고리

---

## B-1. eraf-web (웹 애플리케이션 공통) — 15 클래스

> eraf-core 모듈들의 기능을 웹 환경에서 자동 구성하는 Spring Boot Starter

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTTP 요청/응답 자동 로깅 | 모든 HTTP 요청/응답 자동 기록, Trace ID/Request ID 자동 생성 |
| 2 | 민감 정보 마스킹 | password, token, secret, creditCard 등 로그 내 자동 마스킹 |
| 3 | 요청 추적 | Trace ID를 요청/응답 헤더에 자동 포함 |
| 4 | 데이터 마스킹 | 이름, 전화번호, 이메일, 카드번호, 계좌, 주민번호, IP, 차량번호 마스킹 |
| 5 | 제외 경로 설정 | /actuator, /health 등 로깅 제외 경로 패턴 |
| 6 | 응답 코드별 로그 레벨 | 4xx는 WARN, 5xx는 ERROR로 자동 분류 |
| 7 | CORS 설정 | Origin, Methods, Headers, Preflight 캐시 설정 |
| 8 | 자동 Bean 등록 | 멱등성, 분산 락, 공통코드, 기능 토글, 채번, 다국어, 파일 업로드, 복원력 패턴 자동 활성화 |
| 9 | 응답 표준화 | ObjectMapper 설정 (JavaTimeModule), GlobalExceptionHandler 자동 등록 |

---

## B-2. eraf-security (보안) — 37 클래스

> JWT, API Key, OAuth2, RBAC, 감사 로깅, 봇 탐지, IP 접근 제어

### B-2-1. JWT 인증

| # | 기능 | 설명 |
|---|------|------|
| 1 | Access/Refresh Token 발급 | JWT 토큰 쌍 생성 |
| 2 | Token 유효성 검증 | 서명 검증, 만료 시간 확인 |
| 3 | 사용자 정보 확장 | userId, displayName, email 등 추가 정보 저장 |
| 4 | 권한(authorities) 포함 | 토큰에 사용자 역할/권한 자동 포함 |
| 5 | Stateless 세션 정책 | 세션 없는 인증 지원 |

### B-2-2. API Key 인증

| # | 기능 | 설명 |
|---|------|------|
| 1 | 헤더/쿼리 파라미터 인증 | API Key를 헤더 또는 쿼리 파라미터로 전달 |
| 2 | API Key별 역할 지정 | Key별로 Role(역할) 매핑 |
| 3 | IP 주소 기반 필터링 | CIDR 표기법 지원 IP 필터링 |
| 4 | URL 패턴 기반 필터링 | 특정 URL 패턴에만 API Key 인증 적용 |

### B-2-3. 보안 감사 로깅

| # | 기능 | 설명 |
|---|------|------|
| 1 | 16가지 보안 이벤트 | LOGIN_SUCCESS/FAILURE, LOGOUT, ACCESS_DENIED, TOKEN_CREATED/EXPIRED 등 |
| 2 | 자동 이벤트 캡처 | Spring Security 이벤트 자동 감지 및 기록 |
| 3 | 이벤트 리스너 | 리스너 등록/제거로 커스텀 처리 확장 |
| 4 | 컨텍스트 기록 | IP, User-Agent, 요청 URI, 타임스탬프 자동 기록 |

### B-2-4. 봇 탐지

| # | 기능 | 설명 |
|---|------|------|
| 1 | User-Agent 기반 탐지 | 50+ 알려진 봇 패턴 내장 |
| 2 | 검색 엔진 허용 | Google, Bing, Naver, Daum 등 기본 허용 |
| 3 | 악성 봇 차단 | Nmap, SQLMap, Masscan 등 자동 차단 |
| 4 | 신뢰도 기반 판단 | 0.0~1.0 신뢰도 점수 기반 판단 |
| 5 | 봇 타입 분류 | SEARCH_ENGINE_CRAWLER, SOCIAL_MEDIA_BOT, MALICIOUS_BOT 등 8가지 분류 |

### B-2-5. 3가지 보안 필터 체인

| # | 체인 | 설명 |
|---|------|------|
| 1 | JWT 기반 | Stateless, Authorization 헤더 토큰 검증 |
| 2 | API Key 기반 | API Key 헤더/쿼리 검증, IP/URL 패턴 매칭 |
| 3 | Default (Form Login) | 기본 로그인 폼, 세션 기반 인증 |

---

## B-3. eraf-session (세션 관리) — 6 클래스

> Redis 기반 분산 세션 관리 및 동시 세션 제어

| # | 기능 | 설명 |
|---|------|------|
| 1 | Redis 기반 분산 세션 | 다중 인스턴스 간 세션 공유 |
| 2 | 사용자별 세션 추적 | userId 기반 세션-사용자 매핑 |
| 3 | 동시 세션 제어 | 최대 동시 세션 수 제한 (기본 1개) |
| 4 | Kick-Old 정책 | 새 로그인 시 기존 세션 자동 종료 |
| 5 | 세션 타임아웃 | 자동 타임아웃 설정 (기본 30분) |
| 6 | JWT Token Pair | Access + Refresh 토큰 쌍 관리 |

---

## B-4. eraf-swagger (API 문서화) — 2 클래스

> SpringDoc OpenAPI 3.0 자동 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | Swagger UI 자동 활성화 | /swagger-ui.html 대화형 API 문서 |
| 2 | JWT 보안 스키마 | Bearer Token 인증 자동 추가 |
| 3 | API 그룹 설정 | 패키지/경로 패턴별 API 그룹 분리 |

---

# C. Data 카테고리

---

## C-1. eraf-data-jpa (JPA 데이터 계층) — 51 클래스

> 엔티티 관리, 멀티테넌시, 소프트 삭제, 동적 쿼리, 감사 로깅, Envers, Flyway

### C-1-1. 기본 엔티티

| # | 기능 | 설명 |
|---|------|------|
| 1 | BaseEntity | 생성/수정 감사 필드 자동 관리 (createdAt/By, updatedAt/By) |
| 2 | BaseIdEntity | Long 타입 ID + 감사 필드 포함 기본 엔티티 |

### C-1-2. 멀티테넌시

| # | 기능 | 설명 |
|---|------|------|
| 1 | 테넌트 컨텍스트 | ThreadLocal 기반 테넌트 ID 관리 |
| 2 | 테넌트 엔티티 | 테넌트 ID 자동 설정 기본 클래스 |
| 3 | 테넌트 필터 | HTTP 요청에서 테넌트 ID 자동 추출 (헤더/파라미터/서브도메인) |
| 4 | 자동 쿼리 필터 | Hibernate Filter로 모든 쿼리에 테넌트 조건 자동 추가 |

### C-1-3. 소프트 삭제

| # | 기능 | 설명 |
|---|------|------|
| 1 | Soft Delete 엔티티 | deleted, deletedAt, deletedBy 필드 자동 관리 |
| 2 | 활성/삭제 데이터 조회 | 삭제되지 않은/삭제된 데이터 선택적 조회 |
| 3 | 복원 | 소프트 삭제된 데이터 복원 |

### C-1-4. 동적 Specification 빌더

| # | 기능 | 설명 |
|---|------|------|
| 1 | 비교/문자열/컬렉션 연산 | equal, like, in, between, isNull 등 |
| 2 | 논리 조합 | or, and 조건 결합 |
| 3 | 조인 | join, fetchJoin |
| 4 | 조건부 추가 | when(), ifNotNull() 조건부 필터 |

### C-1-5. 감사 로그

| # | 기능 | 설명 |
|---|------|------|
| 1 | AuditLogEntity | 사용자, 리소스, 액션, 결과, 타임스탬프 저장 |
| 2 | AuditLogQueryService | 다양한 조건별 감사 로그 조회 |
| 3 | AuditLogRetentionPolicy | Soft Delete + Hard Delete 자동 보관 정책 |
| 4 | Auditor 자동 설정 | ErafContext에서 현재 사용자 자동 추출 |

### C-1-6. Hibernate Envers

| # | 기능 | 설명 |
|---|------|------|
| 1 | 엔티티 이력 관리 | @Audited 엔티티의 변경 이력 자동 추적 |
| 2 | 조건부 활성화 | `eraf.jpa.enversEnabled=true` 설정 시 활성화 |

### C-1-7. DataSource 라우팅

| # | 기능 | 설명 |
|---|------|------|
| 1 | 읽기/쓰기 분리 | Primary/ReadOnly DataSource 자동 라우팅 |
| 2 | Replica 설정 | 별도 DB URL/인증 정보 설정 |

### C-1-8. Flyway 마이그레이션

| # | 기능 | 설명 |
|---|------|------|
| 1 | 자동 마이그레이션 | classpath:db/migration 기본 스크립트 위치 |
| 2 | Baseline 관리 | 기존 DB에 Flyway 적용 시 baseline 설정 |

---

## C-2. eraf-data-redis (Redis 데이터 계층) — 17 클래스

> 분산 락, 시퀀스, 멱등성, Pub/Sub, Stream, 캐시 워밍

| # | 기능 | 설명 |
|---|------|------|
| 1 | RedisLockProvider | Lua 스크립트 기반 원자적 락 (재진입, 자동 해제) |
| 2 | RedisSequenceGenerator | 분산 환경 시퀀스 (일/월/년 리셋, 포맷 패턴) |
| 3 | RedisIdempotencyStore | 중복 요청 감지, 결과 캐싱, TTL 관리 |
| 4 | RedisMessagePublisher | Redis Pub/Sub 메시지 발행 |
| 5 | RedisMessageSubscriber | Redis Pub/Sub 메시지 구독 |
| 6 | RedisStreamProducer | Redis Stream 메시지 생성 |
| 7 | RedisStreamConsumer | Redis Stream 컨슈머 그룹 기반 소비 |
| 8 | CacheWarmer | 애플리케이션 시작 시 캐시 사전 로딩 |
| 9 | RedisTemplate 자동 설정 | Jackson 직렬화, JavaTimeModule 포함 |

---

## C-3. eraf-data-mongo (MongoDB 데이터 계층) — 11 클래스 🆕

> MongoDB 문서 기반 엔티티, 감사, 멀티테넌시

| # | 기능 | 설명 |
|---|------|------|
| 1 | BaseDocument | 생성/수정 감사 필드 자동 관리 (@CreatedDate/@LastModifiedDate) |
| 2 | BaseIdDocument | ID + 감사 필드 포함 기본 문서 |
| 3 | SoftDeleteDocument | 논리적 삭제 지원 (deleted, deletedAt) |
| 4 | TenantDocument | 멀티테넌시 지원 (tenantId 자동 관리) |
| 5 | MongoTenantFilter | 자동 테넌트 쿼리 필터링 |
| 6 | MongoQueryBuilder | MongoDB 쿼리 빌더 헬퍼 |
| 7 | MongoAuditingConfig | MongoDB Auditing 자동 구성 |

**설정 프리픽스**: `eraf.mongo`

---

## C-4. eraf-data-elasticsearch (Elasticsearch) — 5 클래스

> Elasticsearch 클라이언트 자동 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | 클라이언트 설정 | 호스트, 인증정보, 타임아웃 설정 |
| 2 | 자동 구성 | RestClientBuilderCustomizer 자동 등록 |

---

## C-5. eraf-data-mybatis (MyBatis) — 6 클래스

> MyBatis 설정 자동화

| # | 기능 | 설명 |
|---|------|------|
| 1 | 언더스코어→카멜케이스 | DB 컬럼명 ↔ Java 필드명 자동 변환 |
| 2 | 지연 로딩 | 지연 로딩 활성화/비활성화 |
| 3 | Mapper 위치 설정 | XML Mapper 파일 경로 패턴 설정 |

---

## C-6. eraf-data-cache (캐시) — 5 클래스

> Spring Cache 자동 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | 캐시 타입 선택 | simple, caffeine, redis 중 선택 |
| 2 | 기본 TTL 설정 | 캐시 만료 시간 설정 (기본 30분) |
| 3 | 최대 크기 제한 | 로컬 캐시 최대 엔트리 수 설정 |
| 4 | null 값 캐싱 | null 값 캐싱 허용 여부 설정 |

---

# D. Messaging 카테고리

---

## D-1. eraf-messaging-kafka (Kafka 메시징) — 13 클래스

> Apache Kafka 기반 이벤트 스트리밍

| # | 기능 | 설명 |
|---|------|------|
| 1 | 표준 이벤트 포맷 | eventId, eventType, timestamp, source, traceId, payload 포함 |
| 2 | 동기/비동기 발행 | 메시지 동기/비동기 발행 지원 |
| 3 | DLQ (Dead Letter Queue) | 처리 실패 메시지 자동 DLQ 전송 |
| 4 | 지수 백오프 재시도 | 실패 시 간격 점진적 증가 재시도 |
| 5 | Kafka 트랜잭션 | 트랜잭션 기반 원자적 메시지 발행 |
| 6 | Idempotent 프로듀서 | 중복 메시지 방지 |
| 7 | 분산 트레이싱 | TraceId 자동 전파 |

---

## D-2. eraf-messaging-rabbitmq (RabbitMQ 메시징) — 5 클래스

> RabbitMQ 기반 메시지 큐

| # | 기능 | 설명 |
|---|------|------|
| 1 | 자동 구성 | RabbitMQ 연결, Template, Listener 자동 설정 |
| 2 | DLQ 자동 구성 | Dead Letter Queue 자동 설정 |
| 3 | 지수 백오프 재시도 | 실패 시 재시도 정책 |
| 4 | 컨텍스트 전파 | 헤더를 통한 TraceId/RequestId/UserId 전파 |

---

# E. Integration 카테고리

---

## E-1. eraf-integration-ftp (FTP/SFTP) — 5 클래스

> FTP 및 SFTP 파일 전송

| # | 기능 | 설명 |
|---|------|------|
| 1 | FTP 클라이언트 | Apache Commons Net 기반 FTP 구현 |
| 2 | SFTP 클라이언트 | JSch 기반 SFTP 구현 |
| 3 | 파일 업로드/다운로드/삭제 | 원격 서버 파일 전송 및 관리 |
| 4 | SFTP 개인키 인증 | 패스프레이즈 포함 개인키 인증 |

---

## E-2. eraf-integration-tcp (TCP 통신) — 4 클래스

> Netty 기반 고성능 TCP 클라이언트

| # | 기능 | 설명 |
|---|------|------|
| 1 | 비동기/동기 메시지 송수신 | Netty 비동기 I/O 기반 통신 |
| 2 | 자동 재연결 | 연결 끊김 시 자동 재연결 |
| 3 | 연결 상태 리스너 | CONNECTED/DISCONNECTED/RECONNECTING 상태 이벤트 |

---

## E-3. eraf-integration-s3 (객체 저장소) — 5 클래스

> AWS S3, MinIO, 로컬 파일 시스템 통합

| # | 기능 | 설명 |
|---|------|------|
| 1 | 다중 저장소 지원 | 로컬/AWS S3/MinIO 설정으로 선택 |
| 2 | 파일 업로드/다운로드 | 스트림 및 바이트 배열 전송 |
| 3 | Presigned URL | 다운로드/업로드용 임시 URL 생성 (만료 시간 설정) |
| 4 | 파일 복사/이동 | 저장소 내 파일 복사 및 이동 |
| 5 | 경로 탐색 방지 | 로컬 저장소 Path Traversal 공격 방어 |

---

## E-4. eraf-integration-http (HTTP 클라이언트) — 5 클래스

> Spring Cloud Feign 기반 선언적 HTTP 클라이언트

| # | 기능 | 설명 |
|---|------|------|
| 1 | @ErafClient | 인터페이스 기반 선언적 HTTP 호출 |
| 2 | 서비스 디스커버리 | 서비스 이름으로 URL 자동 해결 |
| 3 | Circuit Breaker | 장애 서비스 자동 차단 |
| 4 | 분산 트레이싱 | TraceId, RequestId, UserId 자동 전파 |
| 5 | JWT 토큰 전파 | 서비스 간 JWT 토큰 자동 전달 |

---

## E-5. eraf-integration-grpc (gRPC 통합) — 6 클래스 🆕

> gRPC 클라이언트/서버 통합

| # | 기능 | 설명 |
|---|------|------|
| 1 | GrpcClientFactory | 채널 풀링, TLS, 타임아웃, 메시지 크기 관리 |
| 2 | LoggingInterceptor | 요청/응답 로깅, 성능 메트릭 |
| 3 | MetadataPropagationInterceptor | 분산 추적 컨텍스트 전파 (X-Trace-ID 등) |
| 4 | ClientMetadataInterceptor | 인증 토큰, 클라이언트 정보 주입 |
| 5 | TLS 지원 | PEM 인증서 기반 보안 통신 |

**설정 프리픽스**: `eraf.grpc`

---

## E-6. eraf-integration-websocket (WebSocket) — 6 클래스 🆕

> STOMP/WebSocket 실시간 통신

| # | 기능 | 설명 |
|---|------|------|
| 1 | WebSocket 메시지 브로커 | STOMP 프로토콜 기반 메시지 라우팅 |
| 2 | 토픽 구독/발행 | /topic, /queue 기반 Pub/Sub |
| 3 | 인증 통합 | WebSocket 연결 시 JWT/세션 인증 |
| 4 | 메시지 인터셉터 | 메시지 전송/수신 인터셉터 |
| 5 | 자동 구성 | STOMP 엔드포인트 및 메시지 브로커 자동 설정 |

**설정 프리픽스**: `eraf.websocket`

---

# F. Config 카테고리

---

## F-1. eraf-config (외부 설정 관리) — 9 클래스 🆕

> Spring Cloud Config, HashiCorp Vault, 동적 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | Spring Cloud Config 클라이언트 | 원격 Config Server에서 설정 조회 |
| 2 | VaultConfigService | HashiCorp Vault 시크릿 조회/저장/삭제 |
| 3 | Vault 헬스 체크 | Vault 연결 상태 확인 |
| 4 | 동적 설정 값 관리 | 런타임에 문자열/정수/Boolean 설정 값 변경 |
| 5 | 기능 토글 (레거시) | @Feature 어노테이션으로 메서드 단위 기능 활성화/비활성화 |

**설정 프리픽스**: `eraf.config.cloud`

---

# G. Processing 카테고리

---

## G-1. eraf-batch (배치 처리) — 6 클래스

> Spring Batch 기반 대용량 배치 처리

| # | 기능 | 설명 |
|---|------|------|
| 1 | 표준 Job/Step 빌더 | 잡/스텝 생성 헬퍼 |
| 2 | 청크 처리 | 메모리 효율적 대용량 데이터 처리 |
| 3 | 재시도/스킵 정책 | 설정 기반 오류 처리 |
| 4 | 병렬 처리 | 스레드 풀 기반 병렬 처리 |
| 5 | 실행 모니터링 | 잡/스텝 실행 과정 상세 로깅 |

---

## G-2. eraf-scheduler (스케줄링) — 7 클래스

> 분산 환경 스케줄 작업 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | @ErafScheduled | 선언적 스케줄링 (Cron/FixedDelay/FixedRate) |
| 2 | 분산 락 (ShedLock) | Redis/JDBC 기반 다중 인스턴스 중복 실행 방지 |
| 3 | 작업 레지스트리 | 등록된 스케줄 작업 중앙 관리 및 조회 |
| 4 | 실행 이력 | 작업별 실행 기록 추적 |
| 5 | 상태 관리 | SCHEDULED/RUNNING/PAUSED/COMPLETED/FAILED 상태 |

---

## G-3. eraf-saga (분산 트랜잭션) — 27 클래스

> 마이크로서비스 Saga 패턴 구현

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Saga 어노테이션 | 선언적 Saga 정의 (이름, 타임아웃, 최대 재시도) |
| 2 | @SagaStep 어노테이션 | 순차적 Step 정의 (순서, 보상 메서드, 타임아웃) |
| 3 | 보상 트랜잭션 | 실패 시 자동 역순 롤백 |
| 4 | Saga Orchestrator | 동기/비동기 Saga 실행 중앙 조율 |
| 5 | Step 간 데이터 공유 | SagaContext로 Step 간 데이터 전달 |
| 6 | 모니터링 REST API | Saga 상태 조회, 재시도, 복구, 취소, 통계 |
| 7 | 다중 Repository | InMemory/JPA/Redis 저장소 선택 |

---

## G-4. eraf-statemachine (상태 머신) — 17 클래스

> 상태 기반 비즈니스 로직 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | @StateMachine | 선언적 상태 머신 정의 (ID, 초기상태, 종료상태) |
| 2 | @Transition | 이벤트 기반 상태 전이 정의 |
| 3 | 가드 조건 | SpEL 표현식 기반 전이 조건 평가 |
| 4 | 액션 실행 | 전이 시 메서드 자동 실행 |
| 5 | 다중 StateStore | InMemory/JDBC/Redis 선택 |
| 6 | 상태 이력 | JDBC 저장소에서 상태 변경 이력 관리 |

---

## G-5. eraf-workflow (워크플로우 엔진) — 16 클래스 🆕

> 비즈니스 프로세스 워크플로우 관리 (결재, 승인 등)

| # | 기능 | 설명 |
|---|------|------|
| 1 | WorkflowDefinition | Builder 패턴으로 워크플로우 정의 (Step 순서/구성) |
| 2 | WorkflowStep | TASK/APPROVAL/NOTIFICATION 등 Step 타입 |
| 3 | WorkflowEngine | 워크플로우 인스턴스 생성/실행/일시중단/취소 |
| 4 | WorkflowInstance | 인스턴스 상태 추적 (CREATED/RUNNING/COMPLETED/FAILED/SUSPENDED/CANCELLED) |
| 5 | StepHandler | Step 실행 핸들러 인터페이스 |
| 6 | WorkflowContext | Step 간 데이터 공유 컨텍스트 |
| 7 | 이벤트 발행 | 워크플로우/스텝 상태 변경 이벤트 |
| 8 | 다중 저장소 | InMemory/JPA 저장소 선택 |

**설정 프리픽스**: `eraf.workflow`

---

## G-6. eraf-outbox (Outbox 패턴) — 5 클래스 🆕

> 트랜잭셔널 아웃박스 패턴으로 이벤트 발행 보장

| # | 기능 | 설명 |
|---|------|------|
| 1 | OutboxEvent 엔티티 | 이벤트를 DB 테이블에 저장 |
| 2 | OutboxPublisher | 트랜잭션 내 이벤트 저장, 별도 폴러가 발행 |
| 3 | OutboxPoller | 미발행 이벤트를 주기적으로 폴링하여 메시징 시스템 발행 |
| 4 | 멱등성 보장 | 이벤트 중복 발행 방지 |
| 5 | 자동 정리 | 발행 완료된 이벤트 자동 정리 |

**설정 프리픽스**: `eraf.outbox`

---

## G-7. eraf-feature-flag (기능 플래그) — 22 클래스 🆕

> 프로덕션 레디 기능 플래그 (DB + Redis 3-Tier 캐시)

| # | 기능 | 설명 |
|---|------|------|
| 1 | FeatureFlagService | 3-Tier 캐시: L1 (InMemory) → L2 (Redis) → L3 (DB) |
| 2 | @Feature 어노테이션 | AOP 기반 메서드 단위 기능 활성화/비활성화 |
| 3 | SIMPLE 평가기 | Boolean on/off 토글 |
| 4 | PERCENTAGE 평가기 | 비율 기반 점진적 롤아웃 |
| 5 | TIME_WINDOW 평가기 | 특정 시간대에만 활성화 |
| 6 | USER_BASED 평가기 | 사용자/그룹 기반 타겟팅 |
| 7 | FeatureToggle (호환) | 레거시 호환 Facade (메모리 오버라이드 우선) |
| 8 | Admin REST API | 플래그 CRUD, 통계 조회, 캐시 관리 |
| 9 | 기본 플래그 로더 | 시작 시 기본 플래그 자동 등록 |
| 10 | 비동기 통계 | 플래그 사용 통계 비동기 수집 |

**설정 프리픽스**: `eraf.feature-flag`

---

# H. Gateway & Observability 카테고리

---

## H-1. eraf-gateway (API 게이트웨이) — 6 클래스 🆕

> Spring Cloud Gateway 기반 API 게이트웨이

| # | 기능 | 설명 |
|---|------|------|
| 1 | RateLimitGatewayFilter | Token Bucket 기반 IP별 요청 속도 제한 |
| 2 | AuthenticationGatewayFilter | JWT/OAuth 인증 검증 및 전파 |
| 3 | GatewayRouteConfigurer | 동적 라우트 등록 및 필터 체이닝 |
| 4 | DiscoveryRouteLocator | Eureka/Consul/Nacos 서비스 디스커버리 통합 |
| 5 | HTTP 429 응답 | 요청 제한 초과 시 자동 응답 |

**설정 프리픽스**: `eraf.gateway`

---

## H-2. eraf-actuator (모니터링) — 29 클래스

> 헬스 체크, 메트릭 수집, 분산 추적

### H-2-1. 헬스 체크

| # | 기능 | 설명 |
|---|------|------|
| 1 | Database 헬스 체크 | DataSource 연결 상태 확인 |
| 2 | Redis 헬스 체크 | Redis PING 명령 기반 연결 확인 |
| 3 | Kafka 헬스 체크 | Kafka 클러스터 연결, 노드 수 확인 |

### H-2-2. 메트릭 수집

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Counted | 메서드 호출 횟수 자동 카운트 |
| 2 | @Timed | 메서드 실행 시간 자동 측정 (히스토그램, 백분위수) |
| 3 | 비즈니스 메트릭 | 카운터/게이지/타이머/분포 요약 기록 헬퍼 |

### H-2-3. 분산 추적

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Traced | 메서드에 새 Span 자동 생성 |
| 2 | TracingFilter | HTTP 요청마다 TraceContext 자동 생성 |
| 3 | MDC 통합 | SLF4J MDC에 Trace 정보 자동 포함 |
| 4 | 샘플링 | 설정 가능한 샘플링 비율 |

---

## H-3. eraf-observability (OpenTelemetry 관측성) — 8 클래스 🆕

> OpenTelemetry 기반 통합 관측성 (Trace + Metrics + Logs)

| # | 기능 | 설명 |
|---|------|------|
| 1 | OpenTelemetry 자동 설정 | OTLP Exporter 자동 구성 |
| 2 | Trace 통합 | 분산 추적 컨텍스트 자동 전파 |
| 3 | Metrics 통합 | Micrometer-OTLP 연동 |
| 4 | Log 상관관계 | TraceId/SpanId 로그 자동 주입 |
| 5 | 서비스 메타데이터 | service.name, service.version 자동 설정 |

**설정 프리픽스**: `eraf.observability`

---

# I. Notification & Report 카테고리

---

## I-1. eraf-notification (알림 발송) — 27 클래스

> 이메일, SMS, 푸시 알림 통합 관리

### I-1-1. 이메일

| # | 기능 | 설명 |
|---|------|------|
| 1 | SMTP 이메일 발송 | JavaMailSender 기반 MIME 형식 발송 |
| 2 | 첨부파일 | 파일 첨부 지원 (파일명, 콘텐츠 타입) |
| 3 | 템플릿 | 이메일 템플릿 및 변수 치환 |
| 4 | 비동기 발송 | CompletableFuture 기반 논블로킹 발송 |

### I-1-2. SMS

| # | 프로바이더 | 설명 |
|---|----------|------|
| 1 | Twilio | Twilio API 기반 SMS 발송 |
| 2 | Naver Cloud | Naver Cloud SMS (HMAC-SHA256 서명) |
| 3 | NHN Cloud | NHN Cloud SMS 발송 |
| 4 | AWS SNS | AWS Simple Notification Service 기반 SMS |
| 5 | Custom API | 자체 SMS API 연동 |

### I-1-3. 푸시 알림

| # | 프로바이더 | 설명 |
|---|----------|------|
| 1 | FCM (Firebase) | 토픽/토큰 기반, 단일/다중 발송, 이미지 지원 |
| 2 | APNs (Apple) | PKCS12 인증서 기반, 프로덕션/개발 환경 |

---

## I-2. eraf-report (리포트 생성) — 12 클래스 🆕

> 다중 포맷 리포트 생성 (CSV, HTML, PDF, Excel)

| # | 기능 | 설명 |
|---|------|------|
| 1 | ReportService | 리포트 생성 통합 서비스, 제너레이터 레지스트리 |
| 2 | ReportDefinition | 리포트 정의 (이름, 제목, 파라미터) |
| 3 | ReportData | 헤더 + 데이터 행 모델 |
| 4 | CsvReportGenerator | RFC 4180 CSV 생성 (UTF-8 BOM, 구분자 설정) |
| 5 | HtmlReportGenerator | HTML 테이블 기반 리포트 생성 |
| 6 | PdfReportGenerator | PDF 형식 리포트 생성 |
| 7 | ExcelReportGenerator | Excel XLSX 리포트 생성 |
| 8 | ReportGenerator 인터페이스 | 커스텀 포맷 제너레이터 확장점 |

**설정 프리픽스**: `eraf.report`

---

# J. Document & Media 카테고리

---

## J-1. eraf-excel (Excel 처리) — 4 클래스

> Apache POI 기반 Excel 읽기/쓰기/스트리밍

| # | 기능 | 설명 |
|---|------|------|
| 1 | Excel 읽기 | .xlsx/.xls 파일 읽기 (List 또는 Map 형식) |
| 2 | Excel 쓰기 | XLSX 형식 데이터 작성 (자동 스타일, 열 너비 조정) |
| 3 | 스트리밍 쓰기 | SXSSF 기반 100만건+ 대용량 메모리 효율 처리 |

---

## J-2. eraf-pdf (PDF 처리) — 5 클래스

> PDF 생성, 병합, 분리, 텍스트 추출

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTML→PDF 변환 | ITextRenderer 기반 HTML/CSS를 PDF로 변환 |
| 2 | PDF 병합/분리 | 여러 PDF 파일 통합, 페이지 범위 추출 |
| 3 | 텍스트 추출 | 전체/특정 페이지 텍스트 추출 |

---

## J-3. eraf-barcode (바코드/QR코드) — 4 클래스

> ZXing 기반 바코드 생성 및 읽기

| # | 기능 | 설명 |
|---|------|------|
| 1 | 바코드 생성 | Code128, Code39, EAN-13, EAN-8, UPC-A, ITF |
| 2 | QR코드 생성 | 텍스트/URL/vCard/WiFi QR코드 (에러 보정 레벨) |
| 3 | 자동 읽기 | 이미지에서 바코드/QR코드 자동 인식 |

---

## J-4. eraf-image (이미지 처리) — 2 클래스

> Thumbnailator 기반 이미지 변환

| # | 기능 | 설명 |
|---|------|------|
| 1 | 리사이즈/스케일/크롭/회전 | 비율 유지, 좌표 지정 크롭, 각도 회전 |
| 2 | 워터마크 | 텍스트/이미지 오버레이 (불투명도 설정) |
| 3 | 포맷 변환 | JPEG/PNG/GIF/BMP 변환, 품질 조정 |

---

# K. Test & BOM 카테고리

---

## K-1. eraf-test (테스트 유틸리티) — 7 클래스 🆕

> 단위/통합 테스트 지원 헬퍼

| # | 기능 | 설명 |
|---|------|------|
| 1 | TestFixtureBuilder | 테스트 데이터 생성 빌더 |
| 2 | MockErafContext | ErafContext 모킹 헬퍼 |
| 3 | TestContainerConfig | Testcontainers (Redis, PostgreSQL, Kafka) 자동 구성 |
| 4 | RestAssured 지원 | API 테스트 헬퍼 |
| 5 | 스프링 테스트 슬라이스 | @ErafWebMvcTest, @ErafDataJpaTest 커스텀 슬라이스 |

---

## K-2. eraf-bom (Bill of Materials)

> 전체 모듈의 버전 일괄 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | 버전 통합 관리 | 모든 eraf 모듈의 버전을 한 곳에서 관리 |
| 2 | 선택적 의존성 | 필요한 모듈만 선택하여 프로젝트에 포함 |
| 3 | 호환성 보장 | 모듈 간 버전 호환성 자동 보장 |

---

# 부록: 설정 프리픽스 총정리

| 모듈 | 설정 프리픽스 | 주요 설정 항목 |
|------|-------------|---------------|
| eraf-web | `eraf.web.*` | 로깅, CORS, 멱등성, 분산 락, 공통코드, 기능 토글, 채번, 다국어 |
| eraf-security | `eraf.security.*` | CSRF, JWT, API Key, CORS, 인증 경로 |
| eraf-session | `eraf.session.*` | 타임아웃, 쿠키, JWT, 동시 세션 제어 |
| eraf-swagger | `eraf.swagger.*` | API 정보, 보안, API 그룹 |
| eraf-data-jpa | `eraf.jpa.*` | 감사, SQL 로깅, 멀티테넌시, Envers, Flyway, DataSource 라우팅 |
| eraf-data-redis | `eraf.redis.*` | TTL, 키 프리픽스, 분산 락, 시퀀스, 멱등성 |
| eraf-data-mongo | `eraf.mongo.*` | Auditing, 자동 인덱스, 테넌트 필터 |
| eraf-data-elasticsearch | `eraf.elasticsearch.*` | 호스트, 인증, 타임아웃 |
| eraf-data-mybatis | `eraf.mybatis.*` | 카멜케이스, 지연 로딩, Mapper 경로 |
| eraf-data-cache | `eraf.cache.*` | 타입, TTL, 최대 크기 |
| eraf-messaging-kafka | `eraf.kafka.*` | 토픽 접두사, 재시도, DLQ, 트랜잭션 |
| eraf-messaging-rabbitmq | `eraf.rabbitmq.*` | DLQ, 재시도, 컨텍스트 전파 |
| eraf-integration-ftp | `eraf.ftp.*` | 타입(FTP/SFTP), 호스트, 인증 |
| eraf-integration-tcp | `eraf.tcp.*` | 호스트, 타임아웃, Keep-Alive |
| eraf-integration-s3 | `eraf.storage.*` | 타입(local/s3/minio), 경로, 버킷 |
| eraf-integration-http | `eraf.http.*` | 타임아웃, 재시도, Circuit Breaker |
| eraf-integration-grpc | `eraf.grpc.*` | 서버 포트, TLS, 메시지 크기, 클라이언트 타임아웃 |
| eraf-integration-websocket | `eraf.websocket.*` | 엔드포인트, 메시지 브로커, 인증 |
| eraf-config | `eraf.config.*` | Cloud Config Server, Vault(URI/Token/Backend) |
| eraf-batch | `eraf.batch.*` | 청크 크기, 재시도, 스킵, 스레드 풀 |
| eraf-scheduler | `eraf.scheduler.*` | 활성화, 분산 락, 스레드 풀 |
| eraf-saga | `eraf.saga.*` | Repository 타입, 타임아웃, 재시도, 정리 주기 |
| eraf-statemachine | `eraf.statemachine.*` | Store 타입, TTL, 이벤트 발행 |
| eraf-workflow | `eraf.workflow.*` | 활성화, 저장소 타입 |
| eraf-outbox | `eraf.outbox.*` | 폴링 주기, 배치 크기, 정리 주기 |
| eraf-feature-flag | `eraf.feature-flag.*` | L1/L2 캐시 TTL, Redis, 통계, 기본 플래그 |
| eraf-gateway | `eraf.gateway.*` | 라우트, Rate Limit, 서비스 디스커버리 |
| eraf-actuator | `eraf.actuator.*` | health, metrics, tracing 활성화 |
| eraf-observability | `eraf.observability.*` | OTLP Exporter, 서비스 메타데이터 |
| eraf-notification | `eraf.notification.*` | 이메일(SMTP), SMS(프로바이더별), 푸시(FCM/APNs) |
| eraf-report | `eraf.report.*` | 리포트 포맷, 생성 설정 |

---

# 부록: 기술 의존성

| 의존성 | 버전 | 사용 모듈 |
|--------|------|----------|
| Spring Boot | 3.2.11 | 전체 |
| Java | 21 | 전체 |
| Spring Cloud | 2023.0.3 | eraf-integration-http, eraf-config, eraf-gateway |
| JJWT | 0.12.6 | eraf-core-crypto, eraf-security, eraf-session |
| BCrypt (jBCrypt) | 0.10.2 | eraf-core-crypto, eraf-security |
| Apache POI | 5.4.0 | eraf-excel, eraf-report |
| OpenPDF (ITextRenderer) | 2.0.2 | eraf-pdf, eraf-report |
| ZXing | 3.5.3 | eraf-barcode |
| Thumbnailator | 0.4.20 | eraf-image |
| OkHttp | 4.12.0 | eraf-core-http |
| QueryDSL | 5.1.0 | eraf-data-jpa |
| Micrometer | Spring Boot 관리 | eraf-actuator, eraf-observability |
| Netty | Spring Boot 관리 | eraf-integration-tcp |
| gRPC | - | eraf-integration-grpc |
| Spring WebSocket | Spring Boot 관리 | eraf-integration-websocket |
| Spring Cloud Config | 2023.0.3 | eraf-config |
| Spring Cloud Vault | 2023.0.3 | eraf-config |
| Spring Cloud Gateway | 2023.0.3 | eraf-gateway |
| Spring Cloud OpenFeign | 2023.0.3 | eraf-integration-http |
| ShedLock | - | eraf-scheduler |
| Firebase Admin SDK | - | eraf-notification |
| AWS SDK v2 | - | eraf-integration-s3, eraf-notification |
| Apache Commons Net | - | eraf-integration-ftp |
| JSch | - | eraf-integration-ftp |
| OpenTelemetry | - | eraf-observability |
| Testcontainers | - | eraf-test |

---

# 부록: v1.0 → v2.0 변경 이력

| 항목 | v1.0 | v2.0 | 변화 |
|------|------|------|------|
| 총 모듈 수 | 25 | 47 | +22 (88% 증가) |
| 총 Java 클래스 | 432 | 578 | +146 (34% 증가) |
| Core 아키텍처 | 모놀리식 eraf-core (147) | 10개 독립 모듈 (161) | 모듈 분해 |
| 신규 카테고리 | - | Config, Workflow, Outbox, Feature Flag, Gateway, Observability, Report, Test | 8개 추가 |
| 엔터프라이즈 패턴 | 기본 | Saga + StateMachine + Workflow + Outbox + Feature Flag | 완성 |
| 데이터 계층 | JPA, Redis | +MongoDB, DataSource 라우팅, Envers | 확장 |
| 통신 | HTTP, FTP, TCP | +gRPC, WebSocket, STOMP | 확장 |
| 관측성 | Actuator | +OpenTelemetry OTLP | 표준화 |
| 테스트 | - | eraf-test (Testcontainers, MockHelper) | 추가 |

---

> **문서 버전**: v2.0
> **작성일**: 2026-02-19
> **총 기능 수**: 500+
> **총 Java 클래스**: 578개
> **총 모듈**: 47개 (BOM 포함)
