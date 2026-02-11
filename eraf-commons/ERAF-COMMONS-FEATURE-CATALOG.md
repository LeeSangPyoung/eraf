# ERAF Commons - Enterprise Reusable Asset Factory

## 기능 카탈로그 v1.0

> **기술 스택**: Spring Boot 3.2.x / Java 21 / Maven Multi-Module
> **총 모듈 수**: 25개 (BOM 1 + 기능 모듈 24)
> **총 Java 클래스**: 432개

---

# 목차

| # | 카테고리 | 모듈 | 클래스 수 |
|---|---------|------|----------|
| 1 | **Core** | eraf-core | 147 |
| 2 | **Web** | eraf-web | 4 |
| 3 | **Security** | eraf-security | 20 |
| 4 | **Session** | eraf-session | 4 |
| 5 | **API Documentation** | eraf-swagger | 2 |
| 6 | **Data - JPA** | eraf-data-jpa | 19 |
| 7 | **Data - Redis** | eraf-data-redis | 5 |
| 8 | **Data - Elasticsearch** | eraf-data-elasticsearch | 2 |
| 9 | **Data - MyBatis** | eraf-data-mybatis | 2 |
| 10 | **Data - Cache** | eraf-data-cache | 2 |
| 11 | **Messaging - Kafka** | eraf-messaging-kafka | 11 |
| 12 | **Messaging - RabbitMQ** | eraf-messaging-rabbitmq | 3 |
| 13 | **Integration - FTP** | eraf-integration-ftp | 5 |
| 14 | **Integration - TCP** | eraf-integration-tcp | 4 |
| 15 | **Integration - S3** | eraf-integration-s3 | 6 |
| 16 | **Integration - HTTP** | eraf-integration-http | 4 |
| 17 | **Batch** | eraf-batch | 6 |
| 18 | **Scheduler** | eraf-scheduler | 7 |
| 19 | **Saga** | eraf-saga | 27 |
| 20 | **State Machine** | eraf-statemachine | 17 |
| 21 | **Actuator** | eraf-actuator | 17 |
| 22 | **Notification** | eraf-notification | 15 |
| 23 | **Excel** | eraf-excel | 3 |
| 24 | **PDF** | eraf-pdf | 4 |
| 25 | **Barcode** | eraf-barcode | 3 |
| 26 | **Image** | eraf-image | 1 |

---

# A. Core 카테고리

---

## A-1. eraf-core (핵심 공통 라이브러리)

> 모든 모듈의 기반이 되는 핵심 유틸리티, 보안, 복원력, 검증 기능 제공

### A-1-1. 비동기 처리 (async)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 비동기 작업 관리 | CompletableFuture 기반 비동기 작업 생성 및 실행 |
| 2 | 작업 상태 추적 | PENDING/RUNNING/COMPLETED/FAILED/CANCELLED 상태 관리 |
| 3 | 진행률 콜백 | 비동기 작업의 진행 상황을 실시간으로 콜백 수신 |
| 4 | 타임아웃 제어 | 비동기 작업별 타임아웃 시간 설정 |

### A-1-2. 공통코드 관리 (code)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 코드 그룹 관리 | 그룹별 공통코드 등록/조회/수정/삭제 |
| 2 | 코드 유효성 검증 | @Code 어노테이션으로 입력값의 공통코드 유효성 자동 검증 |
| 3 | 코드 아이템 모델 | 그룹, 코드, 이름, 설명, 정렬순서, 활성화 여부 관리 |
| 4 | 저장소 추상화 | CodeRepository 인터페이스로 저장소 구현 교체 가능 (InMemory 기본 제공) |
| 5 | 활성 코드 필터링 | 활성화된 코드만 선택적 조회 |

### A-1-3. 동적 설정 및 기능 토글 (config)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 동적 설정 값 관리 | 런타임에 문자열/정수/Long/Boolean 설정 값 변경 |
| 2 | 기능 토글 | @Feature 어노테이션으로 메서드 단위 기능 활성화/비활성화 |
| 3 | SpEL 폴백 | 기능 비활성화 시 SpEL 표현식으로 대체 동작 지정 |
| 4 | AOP 인터셉터 | FeatureToggleAspect로 메서드 실행 전 기능 상태 자동 확인 |

### A-1-4. 요청 컨텍스트 (context)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 요청 범위 정보 저장 | ThreadLocal 기반 요청별 컨텍스트 관리 |
| 2 | 추적 ID 관리 | 요청별 고유 Trace ID 자동 생성 및 전파 |
| 3 | 사용자 정보 저장 | 현재 요청의 사용자 ID, 이름, 역할 저장 |
| 4 | 클라이언트 IP 관리 | X-Forwarded-For 등 프록시 환경의 실제 클라이언트 IP 추출 |
| 5 | 커스텀 속성 저장소 | 요청 범위에서 임의의 키-값 속성 저장/조회 |

### A-1-5. 데이터 변환 (converter)

| # | 기능 | 설명 |
|---|------|------|
| 1 | Entity-DTO 매핑 | 제네릭 기반 BaseMapper로 Entity ↔ DTO 자동 매핑 |
| 2 | JSON 변환 | JSON 문자열 ↔ 객체 변환 (Pretty Print, List/Map 지원) |
| 3 | XML 변환 | XML 문자열 ↔ 객체 변환 |
| 4 | Map 변환 | Object ↔ Map 변환 (깊은 복사, 병합) |
| 5 | Null-Safe 처리 | 모든 변환 메서드에서 null 안전성 보장 |

### A-1-6. 암호화 및 보안 (crypto)

| # | 기능 | 설명 |
|---|------|------|
| 1 | AES-256-GCM 대칭키 암호화 | IV 자동 생성 포함 대칭키 암/복호화 |
| 2 | SHA-256 해싱 | Hex 또는 Base64 출력 형식 선택 가능 |
| 3 | HMAC-SHA256 서명 | 메시지 무결성 검증용 HMAC 서명 생성 및 검증 |
| 4 | JWT 토큰 | HS256 기반 JWT 생성/검증, 만료 처리, SpEL 클레임 추출 |
| 5 | 비밀번호 해싱 | bcrypt 기반 비밀번호 해싱 (cost factor 조정 가능) |
| 6 | RSA-2048 비대칭 서명 | 공개키/개인키 기반 디지털 서명 생성 및 검증 |

### A-1-7. 이벤트 기반 아키텍처 (event)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 도메인 이벤트 | DomainEvent 베이스 클래스 (eventId, timestamp 자동 생성) |
| 2 | 동기 이벤트 핸들러 | @EventHandler 어노테이션으로 동기 이벤트 처리 |
| 3 | 비동기 이벤트 핸들러 | @AsyncEventHandler 어노테이션으로 비동기 이벤트 처리 |
| 4 | 트랜잭션 후 이벤트 | @AfterCommit 어노테이션으로 트랜잭션 커밋 후 이벤트 발행 |
| 5 | 이벤트 로깅 | 이벤트 발행 기록 (상태 추적, 재시도 횟수) |
| 6 | 배치 이벤트 발행 | 여러 이벤트를 한 번에 발행 |

### A-1-8. 예외 처리 (exception)

| # | 기능 | 설명 |
|---|------|------|
| 1 | BusinessException | ErrorCode 기반 비즈니스 예외 (메시지 포맷팅 지원) |
| 2 | SystemException | 시스템 레벨 예외 처리 |
| 3 | ValidationException | 필드별 오류 정보 포함 검증 예외 (Builder 패턴) |
| 4 | ErrorCode 인터페이스 | 프로젝트별 에러 코드 정의 인터페이스 |
| 5 | CommonErrorCode | BAD_REQUEST, UNAUTHORIZED, FORBIDDEN 등 공통 에러 코드 |
| 6 | GlobalExceptionHandler | 전역 예외 핸들러 (HTTP 상태 코드 매핑, 표준 응답 변환) |

### A-1-9. 파일 관리 (file)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 파일 업로드 | 멀티파트 파일 업로드 처리 |
| 2 | 파일 다운로드 | 브라우저 호환성 보장, Content-Type 자동 감지 |
| 3 | 파일 타입 감지 | 매직 넘버 기반 + 확장자 기반 파일 타입 식별 |
| 4 | 파일 검증 | 파일 크기, 확장자, 타입 검증 |
| 5 | 로컬 파일 저장소 | 경로 관리, 저장 파일 정보 모델 (크기, 체크섬, URL) |
| 6 | ZIP 압축 | ZIP 파일 생성 및 추출 |

### A-1-10. HTTP 통신 (http)

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTTP 클라이언트 | OkHttp 기반 GET/POST/PUT/DELETE/PATCH 요청 |
| 2 | 파일 업로드/다운로드 | HTTP를 통한 파일 전송 |
| 3 | 스트리밍 | 대용량 응답 스트리밍 처리 |
| 4 | 재시도 | 요청 실패 시 자동 재시도 |
| 5 | 인터셉터 | 요청/응답 인터셉터 지원 |
| 6 | 쿠키 관리 | 쿠키 조회/설정/삭제 (상세 옵션 지원) |

### A-1-11. 다국어 지원 (i18n)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 로케일 리졸버 | 요청별 로케일 자동 결정 |
| 2 | 메시지 서비스 | Spring MessageSource 통합 다국어 메시지 조회 |
| 3 | @Message 어노테이션 | AOP 기반 메시지 국제화 자동 처리 |
| 4 | 메시지 존재 확인 | 메시지 코드 존재 여부 확인, 기본값 지원 |

### A-1-12. 멱등성 보장 (idempotent)

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Idempotent 어노테이션 | SpEL 키 기반 멱등성 보장 |
| 2 | 중복 요청 감지 | 동일 요청 중복 실행 방지 |
| 3 | 이전 결과 반환 | 중복 요청 시 기존 처리 결과 반환 |
| 4 | TTL 관리 | 멱등성 키 자동 만료 |
| 5 | 저장소 추상화 | InMemory 기본 제공, Redis 확장 가능 |

### A-1-13. 분산 락 (lock)

| # | 기능 | 설명 |
|---|------|------|
| 1 | @DistributedLock 어노테이션 | SpEL 키 기반 분산 락 |
| 2 | 대기/유지 시간 설정 | 락 획득 대기 시간, 최대 유지 시간 설정 |
| 3 | 데드락 방지 | 타임아웃 기반 자동 해제 |
| 4 | @OptimisticRetry 어노테이션 | 낙관적 잠금 충돌 시 자동 재시도 |
| 5 | 저장소 추상화 | InMemory 기본 제공, Redis 확장 가능 |

### A-1-14. 로깅 및 감사 (logging)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 감사 로거 | 사용자 활동 기록 (액션, 리소스, 결과 등) |
| 2 | 감사 로그 저장소 | AuditLogStore 인터페이스로 저장소 교체 가능 |
| 3 | 구조화 로깅 | JSON 형식의 구조화된 로그 출력 |
| 4 | 추적 컨텍스트 | Trace ID, Span ID 기반 요청 추적 |

### A-1-15. 데이터 마스킹 (masking)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 이름 마스킹 | 한글/영문 이름 가운데 글자 마스킹 |
| 2 | 전화번호 마스킹 | 전화번호 중간 자리 마스킹 |
| 3 | 이메일 마스킹 | 이메일 아이디 부분 마스킹 |
| 4 | 카드번호 마스킹 | 신용카드 중간 번호 마스킹 |
| 5 | 계좌번호 마스킹 | 은행 계좌번호 마스킹 |
| 6 | 주소 마스킹 | 상세주소 마스킹 |
| 7 | IP 주소 마스킹 | IP 주소 후반부 마스킹 |
| 8 | 차량번호 마스킹 | 차량번호 일부 마스킹 |
| 9 | 주민등록번호 마스킹 | 주민번호 뒷자리 마스킹 |
| 10 | 커스텀 마스킹 | 앞/뒤 표시 글자 수 지정 커스텀 마스킹 |

### A-1-16. 복원력 패턴 (resilience)

| # | 기능 | 설명 |
|---|------|------|
| 1 | Circuit Breaker | CLOSED/OPEN/HALF_OPEN 상태 전이, 장애 자동 감지 및 차단 |
| 2 | Circuit Breaker 레지스트리 | 여러 Circuit Breaker 인스턴스 중앙 관리 |
| 3 | Rate Limiter | Token Bucket 알고리즘 기반 요청 속도 제한 |
| 4 | Retry | 지수 백오프, 재시도 대상 예외 지정, 최대 재시도 횟수 설정 |
| 5 | Timeout | 메서드 실행 시간 제한, 초과 시 예외 발생 |
| 6 | Bulkhead | 스레드풀 격리, 동시 실행 수 제한으로 리소스 보호 |
| 7 | AOP 기반 적용 | 모든 패턴을 어노테이션으로 선언적 적용 |

### A-1-17. API 응답 표준화 (response)

| # | 기능 | 설명 |
|---|------|------|
| 1 | ApiResponse | 성공/실패 통일 응답 형식 (타임스탬프 자동 포함) |
| 2 | ErrorResponse | 필드별 오류 정보 포함 에러 응답 |
| 3 | PageResponse | 페이징 응답 (페이지 정보, 총 건수, 데이터) |

### A-1-18. 시퀀스 생성 (sequence)

| # | 기능 | 설명 |
|---|------|------|
| 1 | @GenerateSequence 어노테이션 | 시퀀스 값 자동 생성 및 필드 주입 |
| 2 | 시퀀스 리셋 | 일/월/년 단위 자동 리셋 |
| 3 | 저장소 추상화 | SequenceGenerator 인터페이스로 구현 교체 가능 |

### A-1-19. 유틸리티 (utils) - 22개 클래스

| # | 유틸리티 | 주요 기능 |
|---|---------|----------|
| 1 | StringUtils | 270+ 메서드: null/empty 체크, 패딩, 케이스 변환(camelCase/snake_case/kebab-case), 부분 문자열, 분할/조인, 유효성 검사 |
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

### A-1-20. 입력값 검증 (validation)

| # | 검증 어노테이션 | 설명 |
|---|---------------|------|
| 1 | @BusinessNo | 사업자번호 형식 및 검증 규칙 검증 |
| 2 | @Email | RFC 표준 이메일 형식 검증 |
| 3 | @Phone | 전화번호 형식 검증 (국가별 형식 지원) |
| 4 | @Password | 비밀번호 강도 검증 (길이, 정규표현식 제약) |
| 5 | @FileExtension | 파일 확장자 화이트리스트 검증 |
| 6 | @NoXss | XSS(Cross-Site Scripting) 공격 방지 |
| 7 | @NoSqlInjection | SQL 인젝션 공격 방지 |
| 8 | @NoPathTraversal | 경로 탐색(Path Traversal) 공격 방지 |

### A-1-21. 메시징 (messaging)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 메시지 모델 | 표준 메시지 포맷 (ErafMessage) |
| 2 | 메시지 발행 | MessagePublisher 인터페이스로 발행 추상화 |
| 3 | 메시지 수신 | MessageListener 인터페이스로 수신 추상화 |
| 4 | 비동기 통신 | 발행/구독 패턴 기반 비동기 메시지 처리 |

### A-1-22. 템플릿 엔진 (template)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 템플릿 처리 | 동적 문자열 생성, 플레이스홀더 치환, 변수 주입 |

---

# B. Web 카테고리

---

## B-1. eraf-web (웹 애플리케이션 공통)

> eraf-core의 기능들을 웹 환경에서 자동 구성하는 Spring Boot Starter

### 주요 기능

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTTP 요청/응답 자동 로깅 | 모든 HTTP 요청/응답 자동 기록, Trace ID/Request ID 자동 생성 |
| 2 | 민감 정보 마스킹 | password, token, secret, creditCard 등 로그 내 자동 마스킹 |
| 3 | 요청 추적 | Trace ID를 요청/응답 헤더에 자동 포함 |
| 4 | 제외 경로 설정 | /actuator, /health 등 로깅 제외 경로 패턴 |
| 5 | 응답 코드별 로그 레벨 | 4xx는 WARN, 5xx는 ERROR로 자동 분류 |
| 6 | CORS 설정 | Origin, Methods, Headers, Preflight 캐시 설정 |
| 7 | 자동 Bean 등록 | 멱등성, 분산 락, 공통코드, 기능 토글, 채번, 다국어, 파일 업로드, 복원력 패턴 자동 활성화 |
| 8 | 응답 표준화 | ObjectMapper 설정 (JavaTimeModule), GlobalExceptionHandler 자동 등록 |

---

## B-2. eraf-security (보안)

> JWT, API Key, 감사 로깅, 봇 탐지 등 엔터프라이즈 보안 기능

### B-2-1. JWT 인증

| # | 기능 | 설명 |
|---|------|------|
| 1 | Access/Refresh Token 발급 | JWT 토큰 쌍 생성 |
| 2 | Token 유효성 검증 | 서명 검증, 만료 시간 확인 |
| 3 | 사용자 정보 확장 | userId, displayName, email 등 추가 정보 저장 |
| 4 | 권한(authorities) 포함 | 토큰에 사용자 역할/권한 자동 포함 |
| 5 | Stateless 세션 정책 | 세션 없는 인증 지원 |
| 6 | 인증 실패 처리 | 401 Unauthorized JSON 응답 |
| 7 | 권한 부족 처리 | 403 Forbidden JSON 응답 |
| 8 | 스킵 패턴 | 인증 제외 URL 패턴 설정 |

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
| 3 | 소셜 미디어 봇 허용 | Facebook, Twitter, LinkedIn 등 허용 |
| 4 | 악성 봇 차단 | Nmap, SQLMap, Masscan 등 자동 차단 |
| 5 | 자동화 도구 구분 | curl, wget, Postman, Insomnia 등 식별 |
| 6 | 웹 스크래퍼 감지 | Scrapy, Selenium, Puppeteer 등 감지 |
| 7 | 신뢰도 기반 판단 | 0.0~1.0 신뢰도 점수 기반 판단 |
| 8 | 봇 타입 분류 | SEARCH_ENGINE_CRAWLER, SOCIAL_MEDIA_BOT, MALICIOUS_BOT 등 8가지 분류 |

### B-2-5. 3가지 보안 필터 체인

| # | 체인 | 설명 |
|---|------|------|
| 1 | JWT 기반 | Stateless, Authorization 헤더 토큰 검증 |
| 2 | API Key 기반 | API Key 헤더/쿼리 검증, IP/URL 패턴 매칭 |
| 3 | Default (Form Login) | 기본 로그인 폼, 세션 기반 인증 |

---

## B-3. eraf-session (세션 관리)

> Redis 기반 분산 세션 관리 및 동시 세션 제어

| # | 기능 | 설명 |
|---|------|------|
| 1 | Redis 기반 분산 세션 | 다중 인스턴스 간 세션 공유 |
| 2 | 사용자별 세션 추적 | userId 기반 세션-사용자 매핑 |
| 3 | 동시 세션 제어 | 최대 동시 세션 수 제한 (기본 1개) |
| 4 | Kick-Old 정책 | 새 로그인 시 기존 세션 자동 종료 |
| 5 | 세션 타임아웃 | 자동 타임아웃 설정 (기본 30분) |
| 6 | 세션 연장 | 활성 세션 타임아웃 연장 |
| 7 | 전체 세션 무효화 | 사용자의 모든 세션 한 번에 종료 (로그아웃) |
| 8 | JWT Token Pair | Access + Refresh 토큰 쌍 관리 |
| 9 | 보안 쿠키 | Secure, HttpOnly 플래그, 경로/이름 커스터마이징 |

---

## B-4. eraf-swagger (API 문서화)

> SpringDoc OpenAPI 3.0 자동 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | Swagger UI 자동 활성화 | /swagger-ui.html 대화형 API 문서 |
| 2 | OpenAPI 3.0 스키마 | /v3/api-docs JSON 스키마 자동 제공 |
| 3 | API 정보 설정 | 제목, 설명, 버전, 약관 설정 |
| 4 | JWT 보안 스키마 | Bearer Token 인증 자동 추가 |
| 5 | API 그룹 설정 | 패키지/경로 패턴별 API 그룹 분리 |
| 6 | 연락처/라이센스 | 개발자 연락처, 라이센스 정보 설정 |

---

# C. Data 카테고리

---

## C-1. eraf-data-jpa (JPA 데이터 계층)

> 엔티티 관리, 멀티테넌시, 소프트 삭제, 동적 쿼리, 감사 로깅

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
| 2 | 활성 데이터 조회 | 삭제되지 않은 데이터만 자동 조회 |
| 3 | 삭제 데이터 조회 | 삭제된 데이터만 선택적 조회 |
| 4 | 복원 | 소프트 삭제된 데이터 복원 |

### C-1-4. 동적 Specification 빌더

| # | 기능 | 설명 |
|---|------|------|
| 1 | 비교 연산 | equal, notEqual, greaterThan, lessThan, between 등 |
| 2 | 문자열 연산 | like, likeIgnoreCase, startsWith, endsWith |
| 3 | 컬렉션 연산 | in, notIn |
| 4 | NULL 체크 | isNull, isNotNull |
| 5 | 불린 체크 | isTrue, isFalse |
| 6 | 논리 조합 | or, and 조건 결합 |
| 7 | 조인 | join, fetchJoin |
| 8 | 조건부 추가 | when(), ifNotNull() 조건부 필터 |
| 9 | 날짜 범위 | dateBetween, dateTimeBetween |

### C-1-5. 공통코드 JPA 저장소

| # | 기능 | 설명 |
|---|------|------|
| 1 | JPA 엔티티 | 공통코드 DB 테이블 매핑 |
| 2 | 그룹별 조회 | 코드 그룹 기반 조회 (정렬순서 지원) |
| 3 | eraf-core 통합 | CodeRepository 인터페이스 JPA 구현체 |

### C-1-6. 감사 로그 JPA 저장소

| # | 기능 | 설명 |
|---|------|------|
| 1 | 감사 로그 엔티티 | 사용자, 리소스, 액션, 결과, 타임스탬프 저장 |
| 2 | 사용자별/리소스별 조회 | 감사 로그 다양한 조건 조회 |
| 3 | 기간별 조회 | 시작일~종료일 감사 로그 조회 |
| 4 | Auditor 자동 설정 | ErafContext에서 현재 사용자 자동 추출 |

---

## C-2. eraf-data-redis (Redis 데이터 계층)

> Redis 기반 분산 락, 시퀀스, 멱등성 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | 분산 락 | Lua 스크립트 기반 원자적 락 (재진입 가능, 자동 해제) |
| 2 | 분산 시퀀스 | 일/월/년 단위 리셋, 포맷 패턴 지원 (예: "ORD-20240115-00001") |
| 3 | 멱등성 저장소 | 중복 요청 감지, 처리 결과 캐싱, TTL 관리 |
| 4 | RedisTemplate 자동 설정 | Jackson 직렬화, JavaTimeModule 포함 |

---

## C-3. eraf-data-elasticsearch (Elasticsearch)

> Elasticsearch 클라이언트 자동 설정

| # | 기능 | 설명 |
|---|------|------|
| 1 | 클라이언트 설정 | 호스트, 인증정보, 타임아웃 설정 |
| 2 | 자동 구성 | RestClientBuilderCustomizer 자동 등록 |

---

## C-4. eraf-data-mybatis (MyBatis)

> MyBatis 설정 자동화

| # | 기능 | 설명 |
|---|------|------|
| 1 | 언더스코어→카멜케이스 | DB 컬럼명 ↔ Java 필드명 자동 변환 |
| 2 | 지연 로딩 | 지연 로딩 활성화/비활성화 |
| 3 | Mapper 위치 설정 | XML Mapper 파일 경로 패턴 설정 |
| 4 | 타입 별칭 | 패키지 스캔 기반 타입 별칭 자동 등록 |

---

## C-5. eraf-data-cache (캐시)

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

## D-1. eraf-messaging-kafka (Kafka 메시징)

> Apache Kafka 기반 이벤트 스트리밍

| # | 기능 | 설명 |
|---|------|------|
| 1 | 표준 이벤트 포맷 | eventId, eventType, timestamp, source, traceId, payload 포함 |
| 2 | 동기/비동기 발행 | 메시지 동기/비동기 발행 지원 |
| 3 | DLQ (Dead Letter Queue) | 처리 실패 메시지 자동 DLQ 전송 |
| 4 | 지수 백오프 재시도 | 실패 시 간격 점진적 증가 재시도 |
| 5 | Kafka 트랜잭션 | 트랜잭션 기반 원자적 메시지 발행 |
| 6 | Idempotent 프로듀서 | 중복 메시지 방지 |
| 7 | 메시지 압축 | 프로듀서 레벨 메시지 압축 설정 |
| 8 | 분산 트레이싱 | TraceId 자동 전파 |
| 9 | 전송 콜백 | 성공/실패 콜백 처리 |

---

## D-2. eraf-messaging-rabbitmq (RabbitMQ 메시징)

> RabbitMQ 기반 메시지 큐

| # | 기능 | 설명 |
|---|------|------|
| 1 | 자동 구성 | RabbitMQ 연결, Template, Listener 자동 설정 |
| 2 | DLQ 자동 구성 | Dead Letter Queue 자동 설정 |
| 3 | 지수 백오프 재시도 | 실패 시 재시도 정책 |
| 4 | 컨텍스트 전파 | 헤더를 통한 TraceId/RequestId/UserId 전파 |
| 5 | JSON 직렬화 | Jackson2 기반 메시지 직렬화/역직렬화 |
| 6 | 자동 컨텍스트 복원 | 메시지 수신 시 컨텍스트 자동 복원 |

---

# E. Integration 카테고리

---

## E-1. eraf-integration-ftp (FTP/SFTP)

> FTP 및 SFTP 파일 전송

| # | 기능 | 설명 |
|---|------|------|
| 1 | FTP 클라이언트 | Apache Commons Net 기반 FTP 구현 |
| 2 | SFTP 클라이언트 | JSch 기반 SFTP 구현 |
| 3 | 파일 업로드/다운로드 | 원격 서버 파일 전송 |
| 4 | 파일 삭제/이름 변경 | 원격 파일 관리 |
| 5 | 디렉토리 관리 | 원격 디렉토리 생성 및 목록 조회 |
| 6 | SFTP 개인키 인증 | 패스프레이즈 포함 개인키 인증 |
| 7 | 이진/패시브 모드 | FTP 전송 모드 설정 |

---

## E-2. eraf-integration-tcp (TCP 통신)

> Netty 기반 고성능 TCP 클라이언트

| # | 기능 | 설명 |
|---|------|------|
| 1 | 비동기 메시지 송수신 | Netty 비동기 I/O 기반 통신 |
| 2 | 동기 송수신 | 타임아웃 지원 동기 통신 |
| 3 | 자동 재연결 | 연결 끊김 시 자동 재연결 (재시도 횟수/간격 설정) |
| 4 | 연결 상태 리스너 | CONNECTED/DISCONNECTED/RECONNECTING 상태 이벤트 |
| 5 | Keep-Alive/NoDelay | TCP 소켓 옵션 설정 |
| 6 | 읽기 타임아웃 | Netty ReadTimeoutHandler 기반 타임아웃 |

---

## E-3. eraf-integration-s3 (객체 저장소)

> AWS S3, MinIO, 로컬 파일 시스템 통합

| # | 기능 | 설명 |
|---|------|------|
| 1 | 다중 저장소 지원 | 로컬/AWS S3/MinIO 설정으로 선택 |
| 2 | 파일 업로드/다운로드 | 스트림 및 바이트 배열 전송 |
| 3 | Presigned URL | 다운로드/업로드용 임시 URL 생성 (만료 시간 설정) |
| 4 | 파일 복사/이동 | 저장소 내 파일 복사 및 이동 |
| 5 | 목록 조회 | 접두사 기반 파일 목록 조회 |
| 6 | 경로 탐색 방지 | 로컬 저장소 Path Traversal 공격 방어 |
| 7 | 메타데이터 관리 | 파일 메타데이터 설정/조회 |

---

## E-4. eraf-integration-http (HTTP 클라이언트)

> Spring Cloud Feign 기반 선언적 HTTP 클라이언트

| # | 기능 | 설명 |
|---|------|------|
| 1 | 선언적 API 클라이언트 | @ErafClient 어노테이션으로 인터페이스 기반 HTTP 호출 |
| 2 | 서비스 디스커버리 | 서비스 이름으로 URL 자동 해결 |
| 3 | Circuit Breaker | 장애 서비스 자동 차단 |
| 4 | 자동 재시도 | 요청 실패 시 재시도 |
| 5 | 분산 트레이싱 | TraceId, RequestId, UserId 자동 전파 |
| 6 | JWT 토큰 전파 | 서비스 간 JWT 토큰 자동 전달 |

---

# F. Processing 카테고리

---

## F-1. eraf-batch (배치 처리)

> Spring Batch 기반 대용량 배치 처리

| # | 기능 | 설명 |
|---|------|------|
| 1 | 표준 Job/Step 빌더 | 잡/스텝 생성 복잡성 감소 헬퍼 |
| 2 | 청크 처리 | 메모리 효율적 대용량 데이터 처리 |
| 3 | 재시도/스킵 정책 | 설정 기반 오류 처리 |
| 4 | 병렬 처리 | 스레드 풀 기반 병렬 처리 |
| 5 | 실행 모니터링 | 잡/스텝 실행 과정 상세 로깅 및 메트릭 |
| 6 | 범용 프로세서 | 함수형 인터페이스 기반 데이터 변환 |

---

## F-2. eraf-scheduler (스케줄링)

> 분산 환경 스케줄 작업 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | @ErafScheduled | 선언적 스케줄링 (Cron/FixedDelay/FixedRate) |
| 2 | 분산 락 (ShedLock) | Redis/JDBC 기반 다중 인스턴스 중복 실행 방지 |
| 3 | 작업 레지스트리 | 등록된 스케줄 작업 중앙 관리 및 조회 |
| 4 | 실행 이력 | 작업별 실행 기록 추적 (시작/완료 시간, 결과) |
| 5 | 상태 관리 | SCHEDULED/RUNNING/PAUSED/COMPLETED/FAILED 상태 |

---

## F-3. eraf-saga (분산 트랜잭션)

> 마이크로서비스 Saga 패턴 구현

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Saga 어노테이션 | 선언적 Saga 정의 (이름, 타임아웃, 최대 재시도) |
| 2 | @SagaStep 어노테이션 | 순차적 Step 정의 (순서, 보상 메서드, 타임아웃) |
| 3 | 보상 트랜잭션 | 실패 시 자동 역순 롤백 |
| 4 | Saga Orchestrator | 동기/비동기 Saga 실행 중앙 조율 |
| 5 | Step 간 데이터 공유 | SagaContext로 Step 간 데이터 전달 |
| 6 | 이벤트 발행 | Saga/Step 상태 변경 이벤트 (로컬/분산) |
| 7 | 자동 복구 | 타임아웃 Saga 자동 복구 |
| 8 | 모니터링 REST API | Saga 상태 조회, 재시도, 복구, 취소, 통계 |
| 9 | 다중 Repository | InMemory/JPA/Redis 저장소 선택 |
| 10 | 완료 Saga 정리 | 주기적 자동 정리 스케줄러 |

---

## F-4. eraf-statemachine (상태 머신)

> 상태 기반 비즈니스 로직 관리

| # | 기능 | 설명 |
|---|------|------|
| 1 | @StateMachine 어노테이션 | 선언적 상태 머신 정의 (ID, 초기상태, 종료상태) |
| 2 | @Transition 어노테이션 | 이벤트 기반 상태 전이 정의 |
| 3 | 가드 조건 | SpEL 표현식 기반 전이 조건 평가 |
| 4 | 액션 실행 | 전이 시 메서드 자동 실행 |
| 5 | 컨텍스트 관리 | 상태별 데이터 저장/조회 |
| 6 | 다중 StateStore | InMemory/JDBC/Redis 선택 |
| 7 | 상태 강제 변경 | 관리자용 상태 직접 변경 |
| 8 | 이벤트 발행 | 상태 변경 이벤트 Spring Event 발행 |
| 9 | 상태 쿼리 | 가능한 이벤트 조회, 전이 가능 여부 확인 |
| 10 | 상태 이력 | JDBC 저장소에서 상태 변경 이력 관리 |

---

# G. Observability 카테고리

---

## G-1. eraf-actuator (모니터링)

> 헬스 체크, 메트릭 수집, 분산 추적

### G-1-1. 헬스 체크

| # | 기능 | 설명 |
|---|------|------|
| 1 | Database 헬스 체크 | DataSource 연결 상태 및 DB 메타정보 확인 |
| 2 | Redis 헬스 체크 | Redis PING 명령 기반 연결 확인 |
| 3 | Kafka 헬스 체크 | Kafka 클러스터 연결, 노드 수 확인 |

### G-1-2. 메트릭 수집

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Counted 어노테이션 | 메서드 호출 횟수 자동 카운트 |
| 2 | @Timed 어노테이션 | 메서드 실행 시간 자동 측정 (히스토그램, 백분위수) |
| 3 | 비즈니스 메트릭 | 카운터/게이지/타이머/분포 요약 기록 헬퍼 |
| 4 | 성공/실패 태그 | 결과별 자동 태그 추가 |
| 5 | 메트릭 캐싱 | 반복 호출 시 성능 최적화 |

### G-1-3. 분산 추적

| # | 기능 | 설명 |
|---|------|------|
| 1 | @Traced 어노테이션 | 메서드에 새 Span 자동 생성 |
| 2 | Trace/Span ID 전파 | 부모-자식 Span 관계 자동 관리 |
| 3 | TracingFilter | HTTP 요청마다 TraceContext 자동 생성 |
| 4 | MDC 통합 | SLF4J MDC에 Trace 정보 자동 포함 |
| 5 | 샘플링 | 설정 가능한 샘플링 비율 |
| 6 | URL 제외 패턴 | 특정 URL 추적 제외 |
| 7 | Baggage | 추가 메타데이터 전파 |

---

## G-2. eraf-notification (알림 발송)

> 이메일, SMS, 푸시 알림 통합 관리

### G-2-1. 이메일

| # | 기능 | 설명 |
|---|------|------|
| 1 | SMTP 이메일 발송 | JavaMailSender 기반 MIME 형식 발송 |
| 2 | 첨부파일 | 파일 첨부 지원 (파일명, 콘텐츠 타입) |
| 3 | 참조/숨은참조 | CC, BCC 지원 |
| 4 | 템플릿 | 이메일 템플릿 및 변수 치환 |
| 5 | 비동기 발송 | CompletableFuture 기반 논블로킹 발송 |

### G-2-2. SMS

| # | 프로바이더 | 설명 |
|---|----------|------|
| 1 | Twilio | Twilio API 기반 SMS 발송 |
| 2 | Naver Cloud | Naver Cloud SMS (HMAC-SHA256 서명) |
| 3 | NHN Cloud | NHN Cloud SMS 발송 |
| 4 | AWS SNS | AWS Simple Notification Service 기반 SMS |
| 5 | Custom API | 자체 SMS API 연동 (URL, API Key 동적 설정) |

### G-2-3. 푸시 알림

| # | 프로바이더 | 설명 |
|---|----------|------|
| 1 | FCM (Firebase) | 토픽/토큰 기반, 단일/다중 발송, 이미지 지원 |
| 2 | APNs (Apple) | PKCS12 인증서 기반, 프로덕션/개발 환경 |

### G-2-4. 통합 기능

| # | 기능 | 설명 |
|---|------|------|
| 1 | 통합 인터페이스 | NotificationService로 이메일/SMS/푸시 통합 호출 |
| 2 | 비동기 발송 | 모든 채널 비동기 발송 지원 |
| 3 | 조건부 활성화 | 사용하는 프로바이더만 선택적 등록 |
| 4 | 플랫폼 선택 | 푸시 발송 시 FCM/APNs/ALL 선택 |

---

# H. Document & Media 카테고리

---

## H-1. eraf-excel (Excel 처리)

> Apache POI 기반 Excel 읽기/쓰기/스트리밍

| # | 기능 | 설명 |
|---|------|------|
| 1 | Excel 읽기 | .xlsx/.xls 파일 읽기 (List 또는 Map 형식) |
| 2 | 시트 관리 | 다중 시트 생성, 시트명/개수 조회 |
| 3 | Map 형식 변환 | 첫 행을 헤더로 하는 Map<String, Object> 변환 |
| 4 | Excel 쓰기 | XLSX 형식 데이터 작성 (자동 스타일, 열 너비 조정) |
| 5 | 스트리밍 쓰기 | SXSSF 기반 100만건+ 대용량 메모리 효율 처리 |
| 6 | 3가지 스타일 | 헤더/데이터/날짜 기본 스타일 자동 적용 |
| 7 | 다양한 출력 | 파일 저장 또는 OutputStream 출력 |

---

## H-2. eraf-pdf (PDF 처리)

> PDF 생성, 병합, 분리, 텍스트 추출

| # | 기능 | 설명 |
|---|------|------|
| 1 | HTML→PDF 변환 | ITextRenderer 기반 HTML/CSS를 PDF로 변환 |
| 2 | 커스텀 폰트 | 한글 등 커스텀 폰트 적용 |
| 3 | PDF 병합 | 여러 PDF 파일을 하나로 통합 |
| 4 | PDF 분리 | 페이지 범위 추출, 전체 페이지 개별 분리 |
| 5 | 텍스트 추출 | 전체/특정 페이지/범위 텍스트 추출 |
| 6 | 빈 PDF 생성 | 지정 페이지 수의 빈 PDF 생성 |

---

## H-3. eraf-barcode (바코드/QR코드)

> ZXing 기반 바코드 생성 및 읽기

### H-3-1. 바코드 생성

| # | 형식 | 설명 |
|---|------|------|
| 1 | Code128 | 범용 1D 바코드 |
| 2 | Code39 | 영숫자 1D 바코드 |
| 3 | EAN-13 | 13자리 국제 상품 바코드 |
| 4 | EAN-8 | 8자리 축약 상품 바코드 |
| 5 | UPC-A | 12자리 미국/캐나다 상품 바코드 |
| 6 | ITF | 짝수 자릿수 1D 바코드 |

### H-3-2. QR코드 생성

| # | 기능 | 설명 |
|---|------|------|
| 1 | 기본 QR코드 | 텍스트 데이터 QR코드 생성 |
| 2 | 에러 보정 레벨 | L(7%)/M(15%)/Q(25%)/H(30%) 에러 보정 |
| 3 | URL QR코드 | URL 전용 QR코드 |
| 4 | vCard QR코드 | 연락처 정보 QR코드 |
| 5 | WiFi QR코드 | WiFi SSID/비밀번호/암호화 방식 QR코드 |

### H-3-3. 바코드/QR코드 읽기

| # | 기능 | 설명 |
|---|------|------|
| 1 | 자동 형식 감지 | 이미지에서 바코드 자동 읽기 |
| 2 | QR코드 전용 읽기 | QR코드만 선택적 읽기 |
| 3 | 형식 정보 반환 | 바코드 내용 + 형식(BarcodeFormat) 반환 |
| 4 | 다양한 입력 | 파일/InputStream/BufferedImage 지원 |

---

## H-4. eraf-image (이미지 처리)

> Thumbnailator 기반 이미지 변환

| # | 기능 | 설명 |
|---|------|------|
| 1 | 리사이즈 | 비율 유지 또는 강제 리사이즈 |
| 2 | 스케일 조정 | 비율 기반 크기 조정 |
| 3 | 크롭 | 중앙 크롭 또는 좌표 지정 크롭 |
| 4 | 회전 | 각도 지정 회전 |
| 5 | 텍스트 워터마크 | 텍스트 오버레이 (불투명도 설정) |
| 6 | 이미지 워터마크 | 이미지 오버레이 (우하단 배치) |
| 7 | 썸네일 생성 | 정사각형 썸네일 생성 |
| 8 | 포맷 변환 | JPEG/PNG/GIF/BMP 등 형식 변환 |
| 9 | 품질/압축 | JPEG 품질 조정 (0.0~1.0) |
| 10 | 이미지 정보 | 너비/높이/포맷 메타데이터 조회 |

---

# I. BOM (의존성 관리)

---

## I-1. eraf-bom (Bill of Materials)

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
| eraf-web | `eraf.web.*` | 로깅, CORS, 멱등성, 분산 락, 공통코드, 기능 토글, 채번, 다국어, 파일 업로드, 복원력 |
| eraf-security | `eraf.security.*` | CSRF, 인증 경로, JWT(비밀키/만료/헤더), API Key(헤더/역할/IP), CORS |
| eraf-session | `eraf.session.*` | 타임아웃, 쿠키, JWT, 동시 세션 제어, Redis 네임스페이스 |
| eraf-swagger | `eraf.swagger.*` | API 정보, 연락처, 라이센스, 보안, API 그룹 |
| eraf-data-jpa | `eraf.jpa.*` | 감사, SQL 로깅, 공통코드, 감사 로그, 낙관적 락, 멀티테넌시 |
| eraf-data-redis | `eraf.redis.*` | TTL, 키 프리픽스, 분산 락, 시퀀스, 멱등성 |
| eraf-data-elasticsearch | `eraf.elasticsearch.*` | 호스트, 인증, 타임아웃 |
| eraf-data-mybatis | `eraf.mybatis.*` | 카멜케이스, 지연 로딩, Mapper 경로 |
| eraf-data-cache | `eraf.cache.*` | 타입, TTL, 최대 크기, null 허용 |
| eraf-messaging-kafka | `eraf.kafka.*` | 토픽 접두사, 재시도, DLQ, 트랜잭션, 프로듀서 |
| eraf-messaging-rabbitmq | `eraf.rabbitmq.*` | DLQ, 재시도, 컨텍스트 전파 |
| eraf-integration-ftp | `eraf.ftp.*` | 타입(FTP/SFTP), 호스트, 인증, 타임아웃 |
| eraf-integration-tcp | `eraf.tcp.*` | 호스트, 타임아웃, Keep-Alive, 자동 재연결 |
| eraf-integration-s3 | `eraf.storage.*` | 타입(local/s3/minio), 경로, 버킷, 리전 |
| eraf-integration-http | `eraf.http.*` | 타임아웃, 재시도, Circuit Breaker, 컨텍스트 전파 |
| eraf-batch | `eraf.batch.*` | 청크 크기, 재시도, 스킵, 스레드 풀 |
| eraf-scheduler | `eraf.scheduler.*` | 활성화, 분산 락, 스레드 풀, 락 시간 |
| eraf-saga | `eraf.saga.*` | 활성화, Repository 타입, 타임아웃, 재시도, 정리 주기 |
| eraf-statemachine | `eraf.statemachine.*` | 활성화, Store 타입, TTL, 이벤트 발행 |
| eraf-actuator | `eraf.actuator.*` | health, metrics, tracing 활성화 |
| eraf-notification | `eraf.notification.*` | 이메일(SMTP), SMS(프로바이더별), 푸시(FCM/APNs) |

---

# 부록: 기술 의존성

| 의존성 | 버전 | 사용 모듈 |
|--------|------|----------|
| Spring Boot | 3.2.11 | 전체 |
| Java | 21 | 전체 |
| JJWT | 0.12.6 | eraf-core, eraf-security, eraf-session |
| BCrypt (jBCrypt) | 0.10.2 | eraf-core, eraf-security |
| Apache POI | 5.4.0 | eraf-excel |
| OpenPDF (ITextRenderer) | 2.0.2 | eraf-pdf |
| ZXing | 3.5.3 | eraf-barcode |
| Thumbnailator | 0.4.20 | eraf-image |
| OkHttp | 4.12.0 | eraf-core |
| QueryDSL | 5.1.0 | eraf-data-jpa |
| Micrometer | Spring Boot 관리 | eraf-actuator |
| Netty | Spring Boot 관리 | eraf-integration-tcp |
| Apache Commons Net | - | eraf-integration-ftp |
| JSch | - | eraf-integration-ftp |
| AWS SDK v2 | - | eraf-integration-s3, eraf-notification |
| Spring Cloud OpenFeign | - | eraf-integration-http |
| ShedLock | - | eraf-scheduler |
| Firebase Admin SDK | - | eraf-notification |

---

> **문서 버전**: v1.0
> **작성일**: 2026-02-10
> **총 기능 수**: 350+
> **총 Java 클래스**: 432개
> **총 모듈**: 25개 (BOM 포함)
