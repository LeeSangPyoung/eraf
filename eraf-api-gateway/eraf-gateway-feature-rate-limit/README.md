# ERAF Gateway Feature - Rate Limit

API Gateway의 Rate Limiting 기능을 제공하는 모듈입니다.

## 기능

- **IP 기반 Rate Limiting**: 클라이언트 IP별 요청 속도 제한
- **슬라이딩 윈도우**: 정확한 시간 윈도우 기반 요청 제한
- **경로별 규칙**: PathMatcher를 사용한 경로 패턴 매칭
- **응답 헤더**: X-RateLimit-* 헤더로 클라이언트에게 정보 제공
- **Retry-After**: 429 응답 시 재시도 가능 시간 제공

## 포함 내용

### Domain
- `RateLimitRule`: Rate Limit 규칙 정의
- `RateLimitRecord`: 요청 기록 (슬라이딩 윈도우)

### Repository
- `RateLimitRuleRepository`: 규칙 저장소 인터페이스
- `RateLimitRecordRepository`: 요청 기록 저장소 인터페이스

### Service
- `RateLimitService`: Rate Limit 체크 및 규칙 관리

### Filter
- `RateLimitFilter`: HTTP 요청 필터 (Order: HIGHEST + 10)

### Exception
- `RateLimitExceededException`: Rate Limit 초과 예외

### Configuration
- `RateLimitProperties`: 설정 클래스
- `RateLimitAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-rate-limit</artifactId>
</dependency>
```

**주의**: Repository 구현체는 별도로 제공해야 합니다.
- `eraf-gateway-store-memory` 또는
- `eraf-gateway-store-jpa`

## 설정 예시

```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      default-limit-per-second: 100
      default-window-seconds: 60
      burst-allowed: false
      burst-multiplier: 1.5
      exclude-patterns:
        - /actuator/**
        - /health/**
```

## 응답 헤더

### 정상 요청
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 45
```

### 제한 초과 (429)
```
Retry-After: 45
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
```

## Rate Limit 타입

- **IP**: IP 주소 기반
- **API_KEY**: API Key 기반
- **USER**: 사용자 ID 기반
- **GLOBAL**: 전체 요청 기반

## 사용 방법

1. 모듈 의존성 추가
2. Repository 구현체 선택 (Memory 또는 JPA)
3. 설정 파일에 rate-limit 설정 추가
4. 필요시 RateLimitRule 동적 생성/수정

## 빌드

```bash
mvn clean install
```
