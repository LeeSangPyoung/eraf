# ERAF Sample Project - UI 시연 기능 명세서

> **작성일**: 2026-02-05
> **버전**: 1.1
> **목적**: ERAF 프레임워크의 모든 기능을 UI에서 시연하기 위한 샘플 프로젝트 기능 명세

---

## 1. 개요

ERAF(Enterprise Reusable Asset Factory) 프레임워크는 26개의 모듈로 구성된 엔터프라이즈 프레임워크입니다.
본 문서는 샘플 프로젝트에서 UI를 통해 시연 가능한 **67개 기능** 전체를 정의합니다.

### 1.0 인프라 환경
| 인프라 | 제공 여부 | 비고 |
|--------|----------|------|
| Redis | ✅ 제공 | 세션, 캐시, 분산락, Pub/Sub |
| Kafka | ✅ 제공 | 메시징 |
| SMTP | ⚠️ Mock | 콘솔 로그로 발송 확인 |
| SMS API | ⚠️ Mock | 콘솔 로그로 발송 확인 |
| Push (FCM/APNs) | ⚠️ Mock | 콘솔 로그로 발송 확인 |
| Zipkin/Jaeger | ⚠️ Mock | 로그 기반 트레이스 조회 |

> **Note**: 모든 67개 기능을 구현합니다. 외부 인프라가 없는 경우 Mock 구현으로 대체합니다.

### 1.1 기술 스택
| 항목 | 버전/기술 |
|------|-----------|
| Java | 21 LTS |
| Spring Boot | 3.2.11 |
| Build Tool | Gradle |
| UI Framework | Thymeleaf + HTMX (권장) |

### 1.2 난이도 범례
| 표시 | 설명 |
|------|------|
| ⭐ | 간단 - 1~2시간 내 구현 |
| ⭐⭐ | 보통 - 반나절 소요 |
| ⭐⭐⭐ | 복잡 - 1일 이상 소요 |

---

## 2. 기능 목록 (67개)

### 2.1 문서/미디어 생성 (12개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 1 | Excel 다운로드 | eraf-excel | 데이터를 Excel 파일로 변환하여 다운로드 | ⭐ |
| 2 | Excel 업로드/파싱 | eraf-excel | Excel 파일 업로드 후 데이터 파싱 및 표시 | ⭐ |
| 3 | Excel 템플릿 기반 생성 | eraf-excel | 템플릿에 데이터 바인딩하여 Excel 생성 | ⭐⭐ |
| 4 | 대용량 Excel 스트리밍 | eraf-excel | SXSSF를 이용한 대용량 Excel 생성 | ⭐⭐ |
| 5 | PDF 생성 | eraf-pdf | 데이터를 PDF 문서로 변환 | ⭐ |
| 6 | PDF 템플릿 렌더링 | eraf-pdf | 템플릿 기반 PDF 생성 | ⭐⭐ |
| 7 | PDF 워터마크 | eraf-pdf | PDF에 텍스트/이미지 워터마크 추가 | ⭐ |
| 8 | Barcode 생성 | eraf-barcode | 다양한 형식의 바코드 생성 (Code128, EAN 등) | ⭐ |
| 9 | QR Code 생성 | eraf-barcode | QR 코드 생성 및 다운로드 | ⭐ |
| 10 | 이미지 리사이즈 | eraf-image | 이미지 크기 조정 | ⭐ |
| 11 | 썸네일 생성 | eraf-image | 이미지 썸네일 자동 생성 | ⭐ |
| 12 | 이미지 워터마크 | eraf-image | 이미지에 워터마크 추가 | ⭐ |

### 2.2 인증/보안 (8개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 13 | JWT 로그인 | eraf-security | JWT 기반 인증 처리 | ⭐⭐ |
| 14 | JWT 토큰 갱신 | eraf-security | Refresh Token을 이용한 Access Token 갱신 | ⭐ |
| 15 | 권한별 메뉴 제어 | eraf-security | 사용자 권한에 따른 UI 메뉴 표시 제어 | ⭐⭐ |
| 16 | Bot 탐지 | eraf-security | 봇 접근 탐지 및 차단 시연 | ⭐⭐ |
| 17 | 세션 관리 | eraf-session | 사용자 세션 목록 조회 및 만료 처리 | ⭐⭐ |
| 18 | 동시 세션 제한 | eraf-session | 동일 계정 동시 접속 제한 | ⭐⭐ |
| 19 | 세션 클러스터링 | eraf-session | Redis 기반 분산 세션 확인 | ⭐⭐⭐ |
| 20 | 감사 로그 | eraf-security | 사용자 행위 감사 로그 조회 | ⭐⭐ |

### 2.3 Resilience 패턴 (8개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 21 | Circuit Breaker 상태 | eraf-core | Circuit Breaker 상태 조회 및 시각화 | ⭐⭐ |
| 22 | Circuit Breaker 시뮬레이션 | eraf-core | 의도적 실패로 Circuit Open 상태 시연 | ⭐⭐ |
| 23 | Rate Limiter 설정 | eraf-core | API별 Rate Limit 설정 및 확인 | ⭐⭐ |
| 24 | Rate Limiter 초과 시연 | eraf-core | Rate Limit 초과 시 429 응답 확인 | ⭐ |
| 25 | Retry 동작 시연 | eraf-core | 자동 재시도 동작 로그 확인 | ⭐⭐ |
| 26 | Timeout 설정 | eraf-core | 타임아웃 설정 및 시연 | ⭐ |
| 27 | Bulkhead 격리 | eraf-core | 동시 요청 제한 시연 | ⭐⭐ |
| 28 | Resilience 대시보드 | eraf-core | 전체 Resilience 패턴 상태 통합 조회 | ⭐⭐⭐ |

### 2.4 상태 관리 (6개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 29 | State Machine 정의 조회 | eraf-statemachine | 등록된 State Machine 목록 및 정의 조회 | ⭐ |
| 30 | State Machine 시각화 | eraf-statemachine | 상태 전이도 시각화 (Mermaid/D3.js) | ⭐⭐⭐ |
| 31 | 상태 전이 실행 | eraf-statemachine | 이벤트 발생을 통한 상태 전이 시연 | ⭐⭐ |
| 32 | 상태 이력 조회 | eraf-statemachine | 엔티티별 상태 변경 이력 조회 | ⭐⭐ |
| 33 | 허용/불허 전이 표시 | eraf-statemachine | 현재 상태에서 가능한 전이 표시 | ⭐ |
| 34 | 주문 워크플로우 예시 | eraf-statemachine | 주문 상태 관리 실제 예시 | ⭐⭐ |

### 2.5 분산 트랜잭션 (8개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 35 | Saga 정의 조회 | eraf-saga | 등록된 Saga 정의 목록 조회 | ⭐ |
| 36 | Saga 실행 현황 | eraf-saga | 진행 중/완료/실패 Saga 목록 조회 | ⭐⭐ |
| 37 | Saga 상세 조회 | eraf-saga | 개별 Saga 실행 상세 및 Step 상태 조회 | ⭐⭐ |
| 38 | Saga Step 시각화 | eraf-saga | Saga Step 진행 상태 시각화 | ⭐⭐ |
| 39 | Saga 보상 트랜잭션 시연 | eraf-saga | 실패 시 보상 트랜잭션 동작 확인 | ⭐⭐⭐ |
| 40 | Saga 수동 복구 | eraf-saga | 실패한 Saga 수동 복구 실행 | ⭐⭐ |
| 41 | Saga 취소 | eraf-saga | 진행 중 Saga 취소 처리 | ⭐ |
| 42 | Saga 재시도 | eraf-saga | 실패한 Saga 재시도 실행 | ⭐ |

### 2.6 유틸리티 (7개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 43 | 분산 락 획득/해제 | eraf-core | 분산 락 획득 및 해제 시연 | ⭐⭐ |
| 44 | 분산 락 모니터링 | eraf-data-redis | 현재 활성 락 목록 조회 | ⭐⭐ |
| 45 | 시퀀스 생성 | eraf-core/redis | 분산 환경 시퀀스 생성 | ⭐ |
| 46 | 멱등성 키 관리 | eraf-core | 멱등성 키 등록 및 조회 | ⭐⭐ |
| 47 | 공통 코드 관리 | eraf-core | 공통 코드 CRUD | ⭐ |
| 48 | JSON 변환 | eraf-core | Object ↔ JSON 변환 테스트 | ⭐ |
| 49 | 파일 유효성 검사 | eraf-core | 파일 확장자, 크기, 시그니처 검증 | ⭐ |

### 2.7 알림/메시징 (6개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 50 | 이메일 발송 | eraf-notification | 이메일 발송 및 결과 확인 | ⭐⭐ |
| 51 | SMS 발송 | eraf-notification | SMS 발송 (Mock) | ⭐ |
| 52 | 푸시 알림 | eraf-notification | 푸시 알림 발송 (Mock) | ⭐ |
| 53 | 알림 이력 조회 | eraf-notification | 발송 이력 조회 | ⭐ |
| 54 | Kafka 메시지 발행 | eraf-messaging-kafka | Kafka 토픽에 메시지 발행 | ⭐⭐ |
| 55 | Kafka 메시지 조회 | eraf-messaging-kafka | 발행된 메시지 목록 확인 | ⭐⭐ |

### 2.8 스케줄러/배치 (4개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 56 | 스케줄러 작업 목록 | eraf-scheduler | 등록된 스케줄러 작업 조회 | ⭐ |
| 57 | 스케줄러 수동 실행 | eraf-scheduler | 스케줄러 작업 즉시 실행 | ⭐ |
| 58 | 배치 Job 목록 | eraf-batch | 등록된 배치 Job 조회 | ⭐ |
| 59 | 배치 Job 실행/모니터링 | eraf-batch | 배치 Job 실행 및 진행 상태 모니터링 | ⭐⭐ |

### 2.9 캐시/Redis (4개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 60 | 캐시 조회 | eraf-data-cache | 캐시된 데이터 목록 조회 | ⭐ |
| 61 | 캐시 무효화 | eraf-data-cache | 특정 캐시 삭제 | ⭐ |
| 62 | Redis 데이터 조회 | eraf-data-redis | Redis 키/값 조회 | ⭐⭐ |
| 63 | Redis Pub/Sub 시연 | eraf-data-redis | 메시지 발행/구독 시연 | ⭐⭐ |

### 2.10 모니터링/관측성 (3개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 64 | Health Check | eraf-actuator | 시스템 헬스 상태 조회 | ⭐ |
| 65 | Metrics 대시보드 | eraf-actuator | 주요 메트릭 시각화 | ⭐⭐ |
| 66 | 분산 추적 조회 | eraf-actuator | Trace ID 기반 요청 추적 | ⭐⭐⭐ |

### 2.11 API 문서 (1개)

| # | 기능명 | 모듈 | 설명 | 난이도 |
|---|--------|------|------|--------|
| 67 | Swagger UI 연동 | eraf-swagger | API 문서 조회 및 테스트 | ⭐ |

---

## 3. 권장 UI 메뉴 구조

```
📁 ERAF Sample Application
│
├── 🏠 대시보드
│   ├── 시스템 상태 (Health)
│   ├── 주요 지표 (Metrics)
│   └── 최근 활동
│
├── 📄 문서 생성
│   ├── Excel
│   │   ├── 다운로드
│   │   ├── 업로드/파싱
│   │   ├── 템플릿 기반 생성
│   │   └── 대용량 스트리밍
│   ├── PDF
│   │   ├── 생성
│   │   ├── 템플릿 렌더링
│   │   └── 워터마크
│   ├── 바코드/QR
│   │   ├── Barcode 생성
│   │   └── QR Code 생성
│   └── 이미지
│       ├── 리사이즈
│       ├── 썸네일
│       └── 워터마크
│
├── 🔐 인증/보안
│   ├── 로그인 (JWT)
│   ├── 토큰 관리
│   ├── 권한 설정
│   ├── Bot 탐지
│   ├── 세션 관리
│   └── 감사 로그
│
├── 🛡️ Resilience
│   ├── Circuit Breaker
│   │   ├── 상태 조회
│   │   └── 시뮬레이션
│   ├── Rate Limiter
│   │   ├── 설정
│   │   └── 초과 시연
│   ├── Retry
│   ├── Timeout
│   ├── Bulkhead
│   └── 통합 대시보드
│
├── 🔄 상태 관리 (State Machine)
│   ├── 정의 조회
│   ├── 상태도 시각화
│   ├── 상태 전이 실행
│   ├── 이력 조회
│   └── 주문 워크플로우 예시
│
├── 💳 분산 트랜잭션 (Saga)
│   ├── Saga 정의
│   ├── 실행 현황
│   ├── 상세 조회
│   ├── Step 시각화
│   ├── 보상 트랜잭션 시연
│   └── 복구/취소/재시도
│
├── 🔧 유틸리티
│   ├── 분산 락
│   ├── 시퀀스 생성
│   ├── 멱등성 키
│   ├── 공통 코드 관리
│   ├── JSON 변환
│   └── 파일 검증
│
├── 📢 알림/메시징
│   ├── 이메일 발송
│   ├── SMS 발송
│   ├── 푸시 알림
│   ├── 알림 이력
│   └── Kafka 메시지
│
├── ⏰ 스케줄러/배치
│   ├── 스케줄러 작업
│   └── 배치 Job
│
├── 💾 캐시/Redis
│   ├── 캐시 관리
│   └── Redis 데이터
│
├── 📊 모니터링
│   ├── Health Check
│   ├── Metrics
│   └── 분산 추적
│
└── 📚 API 문서
    └── Swagger UI
```

---

## 4. 구현 우선순위 권장

### Phase 1: 기본 인프라 (1주차)
- 프로젝트 구조 설정
- 기본 UI 레이아웃
- 인증/로그인 (#13, #14)
- Health Check (#64)
- Swagger UI 연동 (#67)

### Phase 2: 문서 생성 기능 (2주차)
- Excel 다운로드/업로드 (#1, #2)
- PDF 생성 (#5)
- Barcode/QR 생성 (#8, #9)
- 이미지 처리 (#10, #11, #12)

### Phase 3: 핵심 비즈니스 기능 (3주차)
- State Machine 시각화 (#29~#34)
- Saga 관리 (#35~#42)
- 공통 코드 관리 (#47)

### Phase 4: Resilience 패턴 (4주차)
- Circuit Breaker (#21, #22)
- Rate Limiter (#23, #24)
- Retry/Timeout/Bulkhead (#25, #26, #27)
- 통합 대시보드 (#28)

### Phase 5: 고급 기능 (5주차)
- 분산 락/시퀀스 (#43~#46)
- 알림 시스템 (#50~#55)
- 스케줄러/배치 (#56~#59)
- 캐시/Redis (#60~#63)
- 모니터링 (#65, #66)

---

## 5. 기술 구현 가이드

### 5.1 프로젝트 구조 (권장)

```
eraf-sample/
├── src/main/java/com/eraf/sample/
│   ├── SampleApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/
│   │   ├── DashboardController.java
│   │   ├── DocumentController.java
│   │   ├── AuthController.java
│   │   ├── ResilienceController.java
│   │   ├── StateMachineController.java
│   │   ├── SagaController.java
│   │   ├── UtilityController.java
│   │   ├── NotificationController.java
│   │   ├── SchedulerController.java
│   │   ├── CacheController.java
│   │   └── MonitoringController.java
│   ├── service/
│   │   └── ... (각 도메인별 서비스)
│   ├── dto/
│   │   └── ... (요청/응답 DTO)
│   └── domain/
│       └── ... (샘플 도메인 엔티티)
├── src/main/resources/
│   ├── application.yml
│   ├── templates/
│   │   ├── layout/
│   │   │   ├── default.html
│   │   │   ├── header.html
│   │   │   └── sidebar.html
│   │   ├── dashboard/
│   │   ├── document/
│   │   ├── auth/
│   │   ├── resilience/
│   │   ├── statemachine/
│   │   ├── saga/
│   │   ├── utility/
│   │   ├── notification/
│   │   ├── scheduler/
│   │   ├── cache/
│   │   └── monitoring/
│   └── static/
│       ├── css/
│       ├── js/
│       └── images/
└── build.gradle
```

### 5.2 의존성 설정 (build.gradle)

```groovy
dependencies {
    // ERAF Commons
    implementation project(':eraf-commons:eraf-core')
    implementation project(':eraf-commons:eraf-web')
    implementation project(':eraf-commons:eraf-data-jpa')
    implementation project(':eraf-commons:eraf-data-redis')
    implementation project(':eraf-commons:eraf-data-cache')
    implementation project(':eraf-commons:eraf-security')
    implementation project(':eraf-commons:eraf-session')
    implementation project(':eraf-commons:eraf-saga')
    implementation project(':eraf-commons:eraf-statemachine')
    implementation project(':eraf-commons:eraf-excel')
    implementation project(':eraf-commons:eraf-pdf')
    implementation project(':eraf-commons:eraf-barcode')
    implementation project(':eraf-commons:eraf-image')
    implementation project(':eraf-commons:eraf-notification')
    implementation project(':eraf-commons:eraf-scheduler')
    implementation project(':eraf-commons:eraf-batch')
    implementation project(':eraf-commons:eraf-actuator')
    implementation project(':eraf-commons:eraf-swagger')
    implementation project(':eraf-commons:eraf-messaging-kafka')

    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Webjars for UI
    implementation 'org.webjars:bootstrap:5.3.2'
    implementation 'org.webjars.npm:htmx.org:1.9.10'
    implementation 'org.webjars:font-awesome:6.5.1'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 5.3 설정 파일 (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: eraf-sample
  thymeleaf:
    cache: false

eraf:
  # Core 설정
  core:
    enabled: true
    lock:
      type: redis
    sequence:
      type: redis

  # Security 설정
  security:
    enabled: true
    jwt:
      secret: ${JWT_SECRET:your-secret-key}
      expiration: 3600

  # Session 설정
  session:
    enabled: true
    type: redis
    max-sessions: 3

  # Saga 설정
  saga:
    enabled: true
    repository-type: jpa
    api-enabled: true
    api-path: /api/saga

  # State Machine 설정
  statemachine:
    enabled: true
    persist: true

  # Resilience 설정
  resilience:
    circuit-breaker:
      enabled: true
    rate-limiter:
      enabled: true

  # 문서 생성 설정
  excel:
    enabled: true
  pdf:
    enabled: true
  barcode:
    enabled: true
  image:
    enabled: true

  # 알림 설정
  notification:
    enabled: true
    email:
      enabled: true
    sms:
      enabled: false
    push:
      enabled: false

  # 스케줄러/배치 설정
  scheduler:
    enabled: true
  batch:
    enabled: true

# Actuator 설정
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

---

## 6. 체크리스트

### 6.1 구현 완료 체크리스트

| # | 기능 | 완료 | 비고 |
|---|------|:----:|------|
| 1 | Excel 다운로드 | ☐ | |
| 2 | Excel 업로드/파싱 | ☐ | |
| 3 | Excel 템플릿 기반 생성 | ☐ | |
| 4 | 대용량 Excel 스트리밍 | ☐ | |
| 5 | PDF 생성 | ☐ | |
| 6 | PDF 템플릿 렌더링 | ☐ | |
| 7 | PDF 워터마크 | ☐ | |
| 8 | Barcode 생성 | ☐ | |
| 9 | QR Code 생성 | ☐ | |
| 10 | 이미지 리사이즈 | ☐ | |
| 11 | 썸네일 생성 | ☐ | |
| 12 | 이미지 워터마크 | ☐ | |
| 13 | JWT 로그인 | ☐ | |
| 14 | JWT 토큰 갱신 | ☐ | |
| 15 | 권한별 메뉴 제어 | ☐ | |
| 16 | Bot 탐지 | ☐ | |
| 17 | 세션 관리 | ☐ | |
| 18 | 동시 세션 제한 | ☐ | |
| 19 | 세션 클러스터링 | ☐ | |
| 20 | 감사 로그 | ☐ | |
| 21 | Circuit Breaker 상태 | ☐ | |
| 22 | Circuit Breaker 시뮬레이션 | ☐ | |
| 23 | Rate Limiter 설정 | ☐ | |
| 24 | Rate Limiter 초과 시연 | ☐ | |
| 25 | Retry 동작 시연 | ☐ | |
| 26 | Timeout 설정 | ☐ | |
| 27 | Bulkhead 격리 | ☐ | |
| 28 | Resilience 대시보드 | ☐ | |
| 29 | State Machine 정의 조회 | ☐ | |
| 30 | State Machine 시각화 | ☐ | |
| 31 | 상태 전이 실행 | ☐ | |
| 32 | 상태 이력 조회 | ☐ | |
| 33 | 허용/불허 전이 표시 | ☐ | |
| 34 | 주문 워크플로우 예시 | ☐ | |
| 35 | Saga 정의 조회 | ☐ | |
| 36 | Saga 실행 현황 | ☐ | |
| 37 | Saga 상세 조회 | ☐ | |
| 38 | Saga Step 시각화 | ☐ | |
| 39 | Saga 보상 트랜잭션 시연 | ☐ | |
| 40 | Saga 수동 복구 | ☐ | |
| 41 | Saga 취소 | ☐ | |
| 42 | Saga 재시도 | ☐ | |
| 43 | 분산 락 획득/해제 | ☐ | |
| 44 | 분산 락 모니터링 | ☐ | |
| 45 | 시퀀스 생성 | ☐ | |
| 46 | 멱등성 키 관리 | ☐ | |
| 47 | 공통 코드 관리 | ☐ | |
| 48 | JSON 변환 | ☐ | |
| 49 | 파일 유효성 검사 | ☐ | |
| 50 | 이메일 발송 | ☐ | |
| 51 | SMS 발송 | ☐ | |
| 52 | 푸시 알림 | ☐ | |
| 53 | 알림 이력 조회 | ☐ | |
| 54 | Kafka 메시지 발행 | ☐ | |
| 55 | Kafka 메시지 조회 | ☐ | |
| 56 | 스케줄러 작업 목록 | ☐ | |
| 57 | 스케줄러 수동 실행 | ☐ | |
| 58 | 배치 Job 목록 | ☐ | |
| 59 | 배치 Job 실행/모니터링 | ☐ | |
| 60 | 캐시 조회 | ☐ | |
| 61 | 캐시 무효화 | ☐ | |
| 62 | Redis 데이터 조회 | ☐ | |
| 63 | Redis Pub/Sub 시연 | ☐ | |
| 64 | Health Check | ☐ | |
| 65 | Metrics 대시보드 | ☐ | |
| 66 | 분산 추적 조회 | ☐ | |
| 67 | Swagger UI 연동 | ☐ | |

---

## 7. 부록

### 7.1 모듈별 기능 매핑

| 모듈 | 기능 번호 |
|------|-----------|
| eraf-core | #21~#28, #43, #45~#49 |
| eraf-web | (기반 인프라) |
| eraf-data-jpa | (데이터 저장소) |
| eraf-data-redis | #44, #62, #63 |
| eraf-data-cache | #60, #61 |
| eraf-security | #13~#16, #20 |
| eraf-session | #17~#19 |
| eraf-saga | #35~#42 |
| eraf-statemachine | #29~#34 |
| eraf-excel | #1~#4 |
| eraf-pdf | #5~#7 |
| eraf-barcode | #8, #9 |
| eraf-image | #10~#12 |
| eraf-notification | #50~#53 |
| eraf-messaging-kafka | #54, #55 |
| eraf-scheduler | #56, #57 |
| eraf-batch | #58, #59 |
| eraf-actuator | #64~#66 |
| eraf-swagger | #67 |

### 7.2 난이도별 기능 분류

| 난이도 | 개수 | 기능 번호 |
|--------|------|-----------|
| ⭐ (간단) | 24개 | #1, #2, #5, #7, #8, #9, #10, #11, #12, #14, #24, #26, #29, #33, #35, #41, #42, #45, #47, #48, #49, #51, #56, #58, #60, #61, #64, #67 |
| ⭐⭐ (보통) | 35개 | #3, #4, #6, #13, #15, #16, #17, #18, #20, #21, #22, #23, #25, #27, #31, #32, #34, #36, #37, #38, #40, #43, #44, #46, #50, #52, #53, #54, #55, #57, #59, #62, #63, #65 |
| ⭐⭐⭐ (복잡) | 8개 | #19, #28, #30, #39, #66 |

---

**문서 끝**
