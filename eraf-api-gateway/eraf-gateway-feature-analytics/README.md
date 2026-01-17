# ERAF Gateway Feature - Analytics

API Gateway의 Analytics 및 Monitoring 기능을 제공하는 모듈입니다.

## 기능

- **API 호출 기록**: 모든 API 요청/응답 자동 기록
- **메트릭 수집**: 경로별 성능 및 에러 통계
- **대시보드**: 실시간 트래픽 분석
- **응답 시간 분석**: P50, P95, P99 백분위수 추적
- **에러 추적**: 에러 발생 패턴 분석
- **자동 정리**: 오래된 데이터 자동 삭제

## 포함 내용

### Domain
- `ApiCallRecord`: API 호출 기록 (요청/응답 정보)
- `ApiMetrics`: API 메트릭 (통계 데이터)

### Repository
- `AnalyticsRepository`: Analytics 저장소 인터페이스

### Service
- `AnalyticsService`: Analytics 데이터 수집 및 조회
- `DashboardSummary`: 대시보드 요약 데이터

### Filter
- `AnalyticsFilter`: HTTP 요청/응답 기록 필터 (Order: LOWEST - 10)

### Configuration
- `AnalyticsProperties`: 설정 클래스
- `AnalyticsAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-analytics</artifactId>
</dependency>
```

**주의**: Repository 구현체는 별도로 제공해야 합니다.
- `eraf-gateway-store-memory` 또는
- `eraf-gateway-store-jpa`

## 설정 예시

```yaml
eraf:
  gateway:
    analytics:
      enabled: true
      retention-days: 30
      exclude-patterns:
        - /actuator/**
        - /health/**
      async-recording: true
      batch-size: 100
```

## 수집 데이터

### API 호출 기록 (ApiCallRecord)
- 요청 경로 및 메서드
- 클라이언트 IP 및 User-Agent
- API Key (인증된 경우)
- 응답 상태 코드 및 시간
- 요청/응답 크기
- Trace ID
- 에러 정보

### API 메트릭 (ApiMetrics)
- 총 요청 수 및 성공/에러 수
- 평균/최소/최대 응답 시간
- 백분위수 응답 시간 (P50, P95, P99)
- 기간별 통계

## 대시보드 요약

```java
DashboardSummary summary = analyticsService.getDashboardSummary(from, to);
```

제공 정보:
- 총 요청 수 및 에러율
- 평균 응답 시간
- 상위 트래픽 경로 (Top 10)
- 상위 에러 경로 (Top 10)
- 가장 느린 경로 (Top 10)

## 필터 순서

Analytics 필터는 **가장 마지막**에 실행됩니다 (Order: LOWEST - 10).
이를 통해 다른 모든 필터의 처리 시간까지 포함하여 정확한 응답 시간을 측정합니다.

## 자동 정리

설정된 보관 기간(retention-days)이 지난 데이터는 자동으로 삭제됩니다.

```java
@Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
public void cleanup() {
    analyticsService.cleanup(retentionDays);
}
```

## 비동기 처리

`async-recording: true` 설정 시 Analytics 기록이 비동기로 처리되어 API 응답 성능에 영향을 최소화합니다.

## 사용 방법

1. 모듈 의존성 추가
2. Repository 구현체 선택 (Memory 또는 JPA)
3. 설정 파일에 analytics 설정 추가
4. AnalyticsService를 주입받아 메트릭 조회

## 메트릭 조회 예시

```java
@Autowired
private AnalyticsService analyticsService;

// 경로별 메트릭
ApiMetrics metrics = analyticsService.getMetrics("/api/users", from, to);

// 전체 메트릭
List<ApiMetrics> allMetrics = analyticsService.getAllMetrics(from, to);

// 대시보드 요약
DashboardSummary summary = analyticsService.getDashboardSummary(from, to);
```

## 빌드

```bash
mvn clean install
```
