# ERAF OpenAPI Gateway - API Gateway Feature Catalog

## 기능 카탈로그 v1.0

> **기술 스택**: Spring Boot 3.2.x (WebFlux) / Spring Cloud Gateway / Java 21 / PostgreSQL / Redis
> **포트**: 9000
> **모듈 수**: 4개 (Core, Admin, Features, Application)
> **Frontend**: React 18 + TypeScript + Ant Design

---

# 목차

| # | 카테고리 | 설명 |
|---|---------|------|
| A | **Core Domain** | 9개 엔티티, 9개 Repository, 에러 코드 |
| B | **Admin API** | 9개 리소스, 93+ 엔드포인트, CRUD + 통계 |
| C | **Gateway Filters** | 13개 플러그인 필터, 보안/안정성/성능/변환 |
| D | **Load Balancing** | 4가지 알고리즘, 동적 라우팅 |
| E | **Application** | 인증, 검증, 모니터링, 헬스 체크, 대시보드 |
| F | **Frontend** | 11개 관리 페이지, 대시보드 |

---

# A. Core Domain (핵심 도메인)

---

## A-1. 엔티티 (9개 테이블)

### gateway_services - 업스트림 서비스 정의

| # | 필드 | 설명 |
|---|------|------|
| 1 | name (unique) | 서비스 이름 |
| 2 | protocol, host, port | 업스트림 주소 (http/https) |
| 3 | basePath | 서비스 루트 경로 |
| 4 | connectTimeout, readTimeout, writeTimeout | 타임아웃 (ms) |
| 5 | retries | 재시도 횟수 |
| 6 | loadBalancingAlgorithm | 로드 밸런싱 알고리즘 (ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH, WEIGHTED) |
| 7 | healthCheck* | 헬스 체크 설정 (경로, 간격, 타임아웃, 임계값) |
| 8 | upstream (FK) | 연결된 Upstream (고급 LB 정책) |
| 9 | enabled, metadata | 활성화 여부, 메타데이터 (JSONB) |

**관계**: 1:N → Routes, 1:N → Targets

---

### gateway_routes - 경로 기반 라우팅 규칙

| # | 필드 | 설명 |
|---|------|------|
| 1 | name (unique) | Route 이름 |
| 2 | paths (JSONB) | 매칭 경로 패턴 배열 (예: ["/api/**", "/v1/**"]) |
| 3 | methods (JSONB) | HTTP 메서드 배열 (null이면 전체 허용) |
| 4 | hosts (JSONB) | 호스트 매칭 (와일드카드 지원) |
| 5 | headers (JSONB) | 헤더 매칭 조건 |
| 6 | service (FK, 필수) | 대상 서비스 |
| 7 | priority | 우선순위 (낮을수록 먼저 매칭) |
| 8 | stripPath | 경로 제거 여부 |
| 9 | pathPrefix | 경로 접두사 추가 |
| 10 | apiVersion | API 버전 관리 (v1, v2, 날짜 등) |

---

### gateway_apis - API 엔드포인트 레지스트리

| # | 필드 그룹 | 설명 |
|---|----------|------|
| 1 | **기본**: name, path, method | API 식별 (path+method+service unique) |
| 2 | **연결**: service (FK, 필수), route (FK, 선택) | 서비스/라우트 연결 |
| 3 | **인증**: authRequired, authType, requiredRoles | JWT/API_KEY/OAUTH2/BASIC, RBAC |
| 4 | **검증**: validationEnabled, requiredHeaders, requiredQueryParams, jsonSchema, requiredFields, maxBodySize, allowedContentTypes | 요청 검증 |
| 5 | **Rate Limit**: rateLimitEnabled, rateLimitRequests, rateLimitWindowSeconds, rateLimitKey | 속도 제한 (ip/user/api_key) |
| 6 | **IP 제한**: ipRestrictionEnabled, allowedIps, blockedIps | IP 화이트리스트/블랙리스트 |
| 7 | **캐시**: cacheEnabled, cacheTtlSeconds, cacheKey | 응답 캐싱 |
| 8 | **기타**: timeoutMs, priority, tags, metadata | 타임아웃, 우선순위, 태그 |

**도메인 로직**: `matchesPath()` - Ant-style 패턴 매칭 (*, ** 지원)

---

### gateway_targets - 로드 밸런싱 타겟

| # | 필드 | 설명 |
|---|------|------|
| 1 | service (FK, 필수) | 소속 서비스 |
| 2 | upstream (FK, 선택) | 소속 Upstream |
| 3 | host, port | 타겟 주소 |
| 4 | weight | 가중치 (1~1000, 기본 100) |
| 5 | healthStatus | HEALTHY / UNHEALTHY / UNKNOWN |
| 6 | consecutiveFailures | 연속 실패 횟수 |
| 7 | lastHealthCheckAt | 마지막 헬스 체크 시간 |

---

### gateway_upstreams - 고급 로드 밸런싱 정책

| # | 필드 그룹 | 설명 |
|---|----------|------|
| 1 | **알고리즘**: algorithm | ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH, CONSISTENT_HASH, WEIGHTED |
| 2 | **Hash**: hashOn, hashOnHeader, hashFallback | Consistent Hash 설정 (CONSUMER/IP/HEADER/COOKIE) |
| 3 | **Slots**: slots | Ring-balancer 슬롯 수 (기본 10000) |
| 4 | **Active HC**: path, interval, timeout, healthySuccesses, unhealthyFailures, healthyStatuses, unhealthyStatuses | Active Health Check |
| 5 | **Passive HC**: unhealthyFailures, unhealthyTimeouts | Passive Health Check |

**관계**: 1:N → Targets

---

### gateway_plugins - 플러그인 설정

| # | 필드 | 설명 |
|---|------|------|
| 1 | name | 플러그인 유형 (15종 지원) |
| 2 | scope | GLOBAL / SERVICE / ROUTE / CONSUMER_GROUP |
| 3 | route, service, consumerGroup (FK) | Scope에 따른 대상 연결 |
| 4 | config (JSONB) | 플러그인 설정 (JSON) |
| 5 | priority | 실행 우선순위 |

**지원 플러그인 (15종)**:
rate-limit, rate-limit-advanced, api-key, jwt-auth, oauth2, ip-restriction, circuit-breaker, response-cache, bot-detection, cors, request-transformer, response-transformer, analytics, retry, timeout

---

### gateway_consumers - API 소비자 (클라이언트)

| # | 필드 | 설명 |
|---|------|------|
| 1 | username (unique) | 소비자명 |
| 2 | apiKey (unique, 자동 생성) | API Key ("ck_" + UUID) |
| 3 | rateLimit, rateLimitWindowSeconds | 개별 속도 제한 |
| 4 | customId | 외부 시스템 연동용 ID |

**관계**: N:M → Consumer Groups

---

### gateway_consumer_groups - 소비자 그룹

| # | 필드 | 설명 |
|---|------|------|
| 1 | name (unique) | 그룹 이름 |
| 2 | rateLimit, rateLimitWindowSeconds | 그룹 레벨 속도 제한 |
| 3 | consumers (ManyToMany) | 소속 소비자 목록 |

**조인 테이블**: gateway_consumer_group_members

---

### gateway_certificates - SSL/TLS 인증서

| # | 필드 | 설명 |
|---|------|------|
| 1 | cert, privateKey, certChain | PEM 형식 인증서, 개인키, 체인 |
| 2 | snis (JSONB) | SNI 도메인 목록 (와일드카드 지원) |
| 3 | issuer, subject | 발급자, 주체 |
| 4 | validFrom, expiresAt | 유효 기간 |
| 5 | certType | STANDARD / WILDCARD / SAN / SELF_SIGNED |

---

## A-2. 에러 코드 (22종)

| 카테고리 | 에러 코드 | HTTP | 설명 |
|---------|----------|------|------|
| **타임아웃** | GATEWAY_TIMEOUT | 504 | 게이트웨이 타임아웃 |
| **재시도** | RETRY_EXHAUSTED | 502 | 모든 재시도 소진 |
| **속도 제한** | RATE_LIMIT_EXCEEDED | 429 | 속도 제한 초과 |
| **인증** | API_KEY_INVALID, API_KEY_MISSING | 401 | API Key 관련 |
| **인증** | JWT_INVALID, JWT_EXPIRED | 401 | JWT 관련 |
| **인가** | IP_RESTRICTED | 403 | IP 제한 |
| **인가** | API_DISABLED | 403 | API 비활성화 |
| **라우팅** | ROUTE_NOT_FOUND, API_NOT_FOUND | 404 | 매칭 실패 |
| **서비스** | SERVICE_UNAVAILABLE, NO_HEALTHY_TARGET | 503 | 서비스 불가 |
| **검증** | VALIDATION_FAILED, INVALID_REQUEST_BODY, MISSING_REQUIRED_HEADER | 400 | 요청 검증 |
| **복원력** | CIRCUIT_BREAKER_OPEN | 503 | Circuit Breaker |
| **인증서** | CERTIFICATE_EXPIRED, CERTIFICATE_INVALID | 400 | 인증서 |
| **Consumer** | CONSUMER_ALREADY_IN_GROUP, CONSUMER_NOT_IN_GROUP | 409/404 | 그룹 관리 |
| **일반** | RESOURCE_NOT_FOUND, DUPLICATE_RESOURCE | 404/409 | CRUD |

---

# B. Admin API (관리 API)

---

## B-1. 9개 리소스 CRUD 엔드포인트

### 공통 패턴 (모든 리소스)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/admin/{resource}` | 전체 조회 (enabledOnly 파라미터) |
| GET | `/admin/{resource}/{id}` | 상세 조회 |
| GET | `/admin/{resource}/stats` | 통계 조회 |
| POST | `/admin/{resource}` | 생성 (201 Created) |
| PUT | `/admin/{resource}/{id}` | 수정 |
| DELETE | `/admin/{resource}/{id}` | 삭제 |
| PATCH | `/admin/{resource}/{id}/toggle` | 활성화/비활성화 토글 |

### 리소스별 추가 엔드포인트

| 리소스 | 추가 엔드포인트 | 설명 |
|--------|---------------|------|
| **routes** | GET /service/{serviceId} | 서비스별 조회 |
| **services** | GET /name/{name} | 이름으로 조회 |
| **targets** | GET /service/{serviceId}?healthyOnly | 서비스별 + 건강한 타겟만 |
| **apis** | GET /lookup?path=&method= | Path+Method 조회 |
| **apis** | GET /service/{serviceId}, /route/{routeId} | 서비스/라우트별 조회 |
| **plugins** | GET /route/{routeId}, /service/{serviceId}, /global, /name/{name} | 다양한 조건 조회 |
| **consumers** | GET /username/{username} | Username 조회 |
| **consumers** | POST /{id}/regenerate-key | API Key 재생성 |
| **certificates** | GET /expiring-soon, /expired | 만료 관리 |
| **certificates** | GET /name/{name} | 이름으로 조회 |
| **consumer-groups** | POST /{groupId}/consumers/{consumerId} | 멤버 추가 |
| **consumer-groups** | DELETE /{groupId}/consumers/{consumerId} | 멤버 제거 |
| **consumer-groups** | GET /by-consumer/{consumerId} | Consumer가 속한 그룹 |
| **upstreams** | GET /name/{name} | 이름으로 조회 |

**총 API 엔드포인트: 93+개**

---

## B-2. 응답 형식

모든 응답은 `ApiResponse` 래퍼로 통일:
```
{ "status": "SUCCESS|FAIL", "message": "...", "data": {...}, "timestamp": "..." }
```

---

# C. Gateway Filters (게이트웨이 필터)

---

## C-1. 필터 실행 순서 (13개)

| 순번 | 필터 | Order | 카테고리 | 설명 |
|------|------|-------|---------|------|
| 1 | **CORS** | 0 | 요청/응답 | Cross-Origin 허용 설정 |
| 2 | **IP Restriction** | +50 | 보안 | IP 화이트리스트/블랙리스트 |
| 3 | **Timeout** | +50 | 안정성 | 요청별 타임아웃 |
| 4 | **Rate Limit** | +100 | 안정성 | Redis 기반 분산 속도 제한 |
| 5 | **Bot Detection** | +150 | 보안 | User-Agent 기반 봇 탐지/차단 |
| 6 | **Request Transform** | +150 | 변환 | 헤더/쿼리/경로 변환 |
| 7 | **API Key Auth** | +200 | 보안 | API Key 인증 |
| 8 | **JWT Auth** | +200 | 보안 | JWT 토큰 인증 |
| 9 | **Cache** | +300 | 성능 | Redis 기반 응답 캐싱 |
| 10 | **Retry** | +400 | 안정성 | 지수 백오프 자동 재시도 |
| 11 | **Circuit Breaker** | +500 | 안정성 | Resilience4j Circuit Breaker |
| 12 | **Response Transform** | -50 | 변환 | 응답 헤더/상태 변환 |
| 13 | **Analytics** | -100 | 모니터링 | 요청/응답 메트릭 수집 |

---

## C-2. 보안 필터 상세

### CORS 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 허용 Origin | 와일드카드(*) 및 도메인 지정 |
| 2 | 허용 Methods | GET, POST, PUT, DELETE, OPTIONS 등 |
| 3 | Preflight 캐시 | max_age_seconds 설정 |
| 4 | 자격증명 | allow_credentials 설정 |

### IP Restriction 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Whitelist 모드 | 목록에 있는 IP만 허용 |
| 2 | Blacklist 모드 | 목록에 있는 IP 차단 |
| 3 | 와일드카드 | 192.168.1.* 패턴 지원 |
| 4 | 감사 로깅 | 차단 시 AuditLogger 기록 |

### API Key 인증 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 헤더 추출 | X-API-Key 헤더에서 Key 추출 |
| 2 | Redis 검증 | Redis에서 Key 유효성 확인 |
| 3 | 제외 경로 | 인증 불필요 경로 설정 |
| 4 | 클라이언트 ID | 검증 성공 시 X-Client-Id 헤더 추가 |

### JWT 인증 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Bearer 토큰 | Authorization 헤더에서 토큰 추출 |
| 2 | 토큰 검증 | 서명, 만료 확인 |
| 3 | 제외 경로 | 인증 불필요 경로 설정 |
| 4 | 사용자 ID | 검증 성공 시 X-User-Id 헤더 추가 |

### 봇 탐지 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Good Bot 허용 | Googlebot, Bingbot, Slackbot, Facebook 허용 |
| 2 | Bad Bot 차단 | curl, wget, Scrapy, Selenium 등 차단 |
| 3 | 커스텀 패턴 | 추가 봇 패턴 등록 |
| 4 | 차단/감지 모드 | block(차단) 또는 allow(감지만) |

---

## C-3. 안정성 필터 상세

### Rate Limit 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Redis 기반 | 분산 환경 지원 |
| 2 | 클라이언트 IP 키 | IP 기반 속도 제한 |
| 3 | 응답 헤더 | X-RateLimit-Limit, X-RateLimit-Remaining |
| 4 | Fail-Open | Redis 오류 시 요청 통과 |

### Retry 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 지수 백오프 | 대기 시간 점진적 증가 (multiplier 설정) |
| 2 | 멱등성 메서드 | GET, PUT, DELETE만 재시도 (기본) |
| 3 | 재시도 대상 상태 | 502, 503, 504 (기본) |
| 4 | Jitter | 충돌 방지용 랜덤 지연 |

### Circuit Breaker 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Resilience4j 통합 | 실패율 기반 상태 전이 |
| 2 | 3가지 상태 | CLOSED → OPEN → HALF_OPEN |
| 3 | 느린 호출 감지 | 설정된 시간 초과 호출 감지 |
| 4 | 자동 복구 | Open → Half-Open 자동 전환 |

### Timeout 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 요청별 타임아웃 | API/Route별 커스텀 타임아웃 |
| 2 | 504 응답 | 타임아웃 시 GATEWAY_TIMEOUT 반환 |
| 3 | 응답 헤더 | X-Timeout-Exceeded 헤더 추가 |

---

## C-4. 성능/변환/모니터링 필터

### 캐시 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Redis 기반 | 분산 캐싱 |
| 2 | GET 요청만 | 안전한 메서드만 캐싱 |
| 3 | TTL 설정 | 자동 만료 |
| 4 | 응답 헤더 | X-Cache-Status: HIT/MISS |

### Request Transform 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 헤더 추가/삭제/이름변경 | 요청 헤더 변환 |
| 2 | 쿼리 파라미터 추가/삭제 | 쿼리 스트링 변환 |
| 3 | 경로 재작성 | 정규식 기반 경로 변환 |

### Response Transform 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | 헤더 추가/삭제/이름변경 | 응답 헤더 변환 |
| 2 | 상태 코드 오버라이드 | 응답 상태 코드 변경 |
| 3 | 변수 치환 | ${elapsed} 등 동적 값 |

### Analytics 필터
| # | 기능 | 설명 |
|---|------|------|
| 1 | Redis 저장 | 요청/응답 분석 데이터 |
| 2 | 메트릭 카운터 | Method + Path별 호출 횟수 |
| 3 | 비동기 수집 | 요청 처리에 영향 없음 |

---

## C-5. 플러그인 팩토리

| # | 기능 | 설명 |
|---|------|------|
| 1 | DB 기반 동적 필터 | 플러그인 설정을 DB에서 로드하여 필터 생성 |
| 2 | 3레벨 필터 조합 | Global + Service + Route 필터 결합 |
| 3 | 우선순위 정렬 | priority 순으로 필터 실행 |
| 4 | 확장 가능 | PluginGatewayFilter 인터페이스로 커스텀 필터 추가 |

---

# D. Load Balancing (로드 밸런싱)

---

## D-1. 4가지 로드 밸런싱 알고리즘

| # | 알고리즘 | 설명 |
|---|---------|------|
| 1 | **Round Robin** | AtomicInteger 카운터 순차 분배 |
| 2 | **Weighted Round Robin** | weight 비율 기반 확률적 분배 |
| 3 | **Least Connections** | ConcurrentHashMap 기반 최소 연결 수 선택 |
| 4 | **IP Hash** | SHA-256 해싱 기반 세션 고정 (Session Stickiness) |

---

## D-2. 동적 라우팅 (DynamicRouteLocator)

| # | 기능 | 설명 |
|---|------|------|
| 1 | DB 기반 라우트 | DB에서 활성 Route를 동적으로 로드 |
| 2 | Path/Method/Host 매칭 | 다중 조건 기반 라우트 선택 |
| 3 | 건강한 타겟 선택 | Health Status가 HEALTHY인 타겟만 사용 |
| 4 | Path Strip/Prefix | 경로 변환 (stripPath, pathPrefix) |
| 5 | Target URI 생성 | {protocol}://{host}:{port}{basePath} |
| 6 | 라우트 리프레시 | RefreshRoutesEvent로 동적 갱신 |

---

# E. Application (애플리케이션)

---

## E-1. 인증 및 보안

| # | 기능 | 설명 |
|---|------|------|
| 1 | JWT 인증 필터 | Access/Refresh Token 검증, 권한 확인 |
| 2 | API 레지스트리 검증 | 등록된 API만 통과, 미등록 → 404 |
| 3 | Spring Security (WebFlux) | CSRF 비활성화, CORS 설정, 경로별 접근 제어 |
| 4 | Admin API 권한 분리 | ROLE_ADMIN (CRUD), ROLE_VIEWER (조회만) |

---

## E-2. 모니터링 및 대시보드

### 대시보드 (/admin/dashboard)

| # | 엔드포인트 | 설명 |
|---|----------|------|
| 1 | /overview | 총 요청수, 성공률, 에러율, 평균 응답시간, Top 5 API |
| 2 | /traffic | 최근 24시간 시간대별 트래픽, 메서드/상태 분포 |
| 3 | /top-apis | 호출 횟수 Top N API |
| 4 | /error-prone-apis | 에러율 Top N API |
| 5 | /api-stats | API별 상세 통계 |

### 요청 로그 (/admin/request-logs)

| # | 기능 | 설명 |
|---|------|------|
| 1 | 인메모리 저장 | 최대 1000개 로그 보관 |
| 2 | 로깅 정보 | 시간, 메서드, 경로, IP, 상태코드, 지속시간, User-Agent |
| 3 | 통계 | 성공률, 에러율, 메서드별/상태별 분류 |

---

## E-3. 헬스 체크

| # | 기능 | 설명 |
|---|------|------|
| 1 | 자동 스케줄 | 30초마다 모든 활성 Target 헬스 체크 |
| 2 | 수동 실행 | POST /admin/health-checks/check/{targetId} |
| 3 | 서비스별 조회 | GET /admin/health-checks/service/{serviceId} |
| 4 | 통계 | 전체/정상/비정상/미확인 Target 수, 정상률 |
| 5 | 상태 업데이트 | HEALTHY/UNHEALTHY/UNKNOWN, 연속 실패 추적 |

---

## E-4. 설정

| 항목 | 설정 |
|------|------|
| 서버 포트 | 9000 |
| DB | PostgreSQL (eraf_gateway), HikariCP (max: 10) |
| Redis | localhost:6379, Lettuce (max: 8) |
| JWT | HS256, Access: 1h, Refresh: 7d |
| Circuit Breaker | Window: 10, Min calls: 5, Failure: 50%, Wait: 60s |
| HTTP Client | Connect: 5s, Response: 30s, Pool: elastic (max 100) |
| Actuator | health, info, metrics, prometheus, gateway |
| 로그 | DEBUG (com.eraf), 파일: 10MB, 30일 보관 |

---

# F. Frontend (프론트엔드)

---

## F-1. 11개 관리 페이지

| # | 페이지 | 경로 | 주요 기능 |
|---|--------|------|----------|
| 1 | **Routes** | /gateway/routes | 라우팅 규칙 CRUD, Path/Method/Host 매칭 설정 |
| 2 | **APIs** | /gateway/apis | API 레지스트리 CRUD, 4탭 (Basic/Auth/Validation/Advanced) |
| 3 | **Services** | /gateway/services | 업스트림 서비스 CRUD, 타임아웃/LB/헬스체크 설정 |
| 4 | **Targets** | /gateway/targets | LB 타겟 CRUD, 가중치/헬스 상태 관리 |
| 5 | **Plugins** | /gateway/plugins | 14종 플러그인 CRUD, 4 Scope, JSON 설정 템플릿 |
| 6 | **Request Logs** | /gateway/request-logs | 요청 로그 조회, 통계 카드, 로그 삭제 |
| 7 | **Consumers** | /gateway/consumers | API Key CRUD, Key 자동생성/재생성/복사 |
| 8 | **Health Checks** | /gateway/health-checks | 타겟 상태 모니터링, 30초 자동 갱신, 수동 체크 |
| 9 | **Upstreams** | /gateway/upstreams | LB 정책 CRUD, Active/Passive HC 설정 |
| 10 | **Certificates** | /gateway/certificates | SSL/TLS 인증서 CRUD, 만료 알림, SNI 관리 |
| 11 | **Consumer Groups** | /gateway/consumer-groups | 그룹 CRUD, 멤버 추가/제거 모달 |

---

## F-2. 공통 UI 패턴

| # | 패턴 | 설명 |
|---|------|------|
| 1 | 통계 카드 | 상단에 Row+Col 그리드로 핵심 수치 표시 |
| 2 | 필터/검색 | 이름 검색, 상태 필터, 서비스 필터 등 |
| 3 | CRUD 모달 | 600~800px 폭, 탭 분리 (복잡한 설정) |
| 4 | 토글 스위치 | 테이블 내 직접 활성화/비활성화 전환 |
| 5 | 메서드 색상 태그 | GET(파랑), POST(초록), PUT(주황), DELETE(빨강), PATCH(자주) |
| 6 | 상태 색상 | Enabled(초록), Disabled(회색), Healthy(초록), Unhealthy(빨강) |
| 7 | 응답 인터셉터 | ApiResponse 자동 언래핑 |

---

## F-3. 특수 기능

| 페이지 | 기능 | 설명 |
|--------|------|------|
| APIs | 기능 아이콘 | Auth/Validation/RateLimit/Cache 활성화 아이콘 |
| Consumers | API Key 복사 | 클립보드 복사 + 마스킹 표시 |
| Consumers | Key 재생성 | 확인 다이얼로그 후 새 Key 발급 |
| Health Checks | 자동 갱신 | 30초마다 자동 새로고침 |
| Health Checks | 수동 체크 | 개별 타겟 즉시 헬스 체크 |
| Certificates | 만료 알림 | 30일 이내 만료 인증서 Alert 표시 |
| Consumer Groups | 멤버 관리 | 별도 모달로 멤버 추가/제거 |
| Plugins | JSON 템플릿 | 플러그인 타입별 기본 설정 템플릿 자동 제공 |

---

# 부록: Redis 키 구조

| 용도 | 키 형식 | TTL |
|------|--------|-----|
| API Key | `api_key:{apiKey}` | - |
| Rate Limit | `rate_limit:{clientIp}` | window_seconds |
| Cache | `cache:{method}:{path}[?query]` | ttl_seconds |
| Analytics | `analytics:{timestamp}:{method}:{path}` | - |
| Metrics | `metrics:count:{method}:{path}` | - |

---

# 부록: 감사 로깅 이벤트

| 필터 | 이벤트 | 설명 |
|------|--------|------|
| IP Restriction | IP_RESTRICTED | IP 차단 시 |
| Rate Limit | RATE_LIMIT_EXCEEDED | 속도 제한 초과 시 |
| Circuit Breaker | CIRCUIT_BREAKER_OPEN | CB 열림 시 |
| Bot Detection | BOT_BLOCKED | 봇 차단 시 |
| Timeout | GATEWAY_TIMEOUT | 타임아웃 발생 시 |
| Retry | RETRY_EXHAUSTED | 재시도 소진 시 |

---

# 부록: 응답 헤더

| 헤더 | 필터 | 값 |
|------|------|-----|
| X-Cache-Status | Cache | HIT / MISS |
| X-RateLimit-Limit | Rate Limit | 제한값 |
| X-RateLimit-Remaining | Rate Limit | 남은 횟수 |
| X-Timeout-Exceeded | Timeout | true |
| X-Retry-Exhausted | Retry | true |
| X-Circuit-Breaker-Status | Circuit Breaker | CLOSED / OPEN / HALF_OPEN |
| X-Bot-Detected | Bot Detection | true |
| X-Client-Id | API Key Auth | 클라이언트 ID |
| X-User-Id | JWT Auth | 사용자 ID |

---

> **문서 버전**: v1.0
> **작성일**: 2026-02-10
> **총 엔티티**: 9개 (10 테이블)
> **총 Admin API 엔드포인트**: 93+개
> **총 Gateway 필터**: 13개
> **총 LB 알고리즘**: 4개
> **총 Frontend 페이지**: 11개
