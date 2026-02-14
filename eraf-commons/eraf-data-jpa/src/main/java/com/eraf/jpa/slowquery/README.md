# Slow Query Detection (슬로우 쿼리 감지)

## 📋 개요

데이터베이스 쿼리 실행 시간을 모니터링하여 임계값을 초과하는 쿼리를 자동으로 감지하고 로깅하는 기능입니다.

**목적**:
- 성능 문제의 원인이 되는 느린 쿼리 조기 발견
- 프로덕션 환경에서 쿼리 성능 모니터링
- 데이터베이스 병목 지점 파악 및 개선

## 🚀 사용 방법

### 1. 기본 활성화

`application.yml`에서 Slow Query 감지를 활성화:

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true                # Slow Query 감지 활성화
      threshold-ms: 1000           # 1초 이상 걸리는 쿼리 경고
      critical-threshold-ms: 5000  # 5초 이상은 무조건 ERROR 레벨
      log-as-error: false          # WARN 레벨로 로깅 (false), ERROR 레벨 (true)
      log-stack-trace: true        # 스택 트레이스 포함 여부
      stack-trace-depth: 10        # 스택 트레이스 깊이 (0이면 전체)
```

### 2. DataSource Wrapper 방식 (권장)

`application.yml`의 DataSource 설정에 Wrapper 적용:

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("realDataSource") DataSource realDataSource,
            SlowQueryProperties slowQueryProperties) {

        // 실제 DataSource를 SlowQueryLoggingDataSource로 감싸기
        return new SlowQueryLoggingDataSource(realDataSource, slowQueryProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource realDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

### 3. Hibernate StatementInspector 방식 (Hibernate 전용)

Hibernate를 사용하는 경우 `application.yml`에 설정:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        session_factory:
          statement_inspector: com.eraf.jpa.slowquery.SlowQueryDetector
```

> ⚠️ StatementInspector는 실행 시간 측정이 제한적이므로 DataSource Wrapper 방식을 권장합니다.

## 📊 로그 출력 예시

### WARN 레벨 (기본)

```log
2026-02-11 13:45:23.456 WARN  [SlowQueryLoggingDataSource] [SLOW QUERY] Execution time: 1234ms | SQL: SELECT * FROM users WHERE email = ?
```

### ERROR 레벨 (Critical Threshold 초과)

```log
2026-02-11 13:45:23.456 ERROR [SlowQueryLoggingDataSource] [SLOW QUERY] Execution time: 5678ms (CRITICAL!) | SQL: SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE u.created_at > ?
```

### Stack Trace 포함 (log-stack-trace: true)

```log
2026-02-11 13:45:23.456 ERROR [SlowQueryLoggingDataSource] [SLOW QUERY] Execution time: 2345ms | SQL: SELECT * FROM products WHERE category = ?
  at com.example.service.ProductService.findByCategory(ProductService.java:45)
  at com.example.controller.ProductController.getProducts(ProductController.java:32)
  at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
```

## ⚙️ 설정 옵션 상세

### enabled
- **타입**: `boolean`
- **기본값**: `false`
- **설명**: Slow Query 감지 기능 활성화/비활성화

### threshold-ms
- **타입**: `long`
- **기본값**: `1000` (1초)
- **설명**: Slow Query 판단 기준 시간 (밀리초)
- **권장값**:
  - 개발 환경: `500ms` (더 엄격하게)
  - 프로덕션: `1000ms` ~ `2000ms`
  - 분석/조회 위주: `3000ms` ~ `5000ms`

### critical-threshold-ms
- **타입**: `long`
- **기본값**: `5000` (5초)
- **설명**: 매우 느린 쿼리 기준 시간 (무조건 ERROR 레벨)
- **권장값**: threshold-ms의 3~5배

### log-as-error
- **타입**: `boolean`
- **기본값**: `false`
- **설명**:
  - `false`: WARN 레벨로 로깅 (기본)
  - `true`: ERROR 레벨로 로깅
  - Critical threshold는 항상 ERROR

### log-stack-trace
- **타입**: `boolean`
- **기본값**: `false`
- **설명**: 스택 트레이스 포함 여부
- **주의**: 로그 양이 매우 많아질 수 있으므로 디버깅 시에만 활성화 권장

### stack-trace-depth
- **타입**: `int`
- **기본값**: `10`
- **설명**: 스택 트레이스 출력 깊이 (0이면 전체)

## 🎯 활용 사례

### 1. 개발 환경 - 쿼리 최적화

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true
      threshold-ms: 500           # 500ms 이상 쿼리 감지
      log-stack-trace: true       # 어디서 호출되는지 파악
      stack-trace-depth: 15
```

**목적**: N+1 문제, 인덱스 누락 등을 조기에 발견

### 2. 스테이징 환경 - 성능 테스트

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true
      threshold-ms: 1000
      critical-threshold-ms: 3000
      log-as-error: true          # 모든 느린 쿼리를 ERROR로
```

**목적**: 성능 테스트 결과 분석 및 개선 포인트 도출

### 3. 프로덕션 환경 - 모니터링

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true
      threshold-ms: 2000          # 너무 민감하지 않게
      critical-threshold-ms: 5000
      log-as-error: false         # WARN 레벨 (로그 양 조절)
      log-stack-trace: false      # 성능 부담 최소화
```

**목적**: 실시간 쿼리 성능 모니터링 및 알림

### 4. 장애 분석 모드

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true
      threshold-ms: 100           # 매우 낮은 임계값
      log-stack-trace: true       # 상세 추적
      stack-trace-depth: 0        # 전체 스택
```

**목적**: 성능 이슈 발생 시 원인 파악을 위한 상세 로깅

## 🔍 문제 해결

### Q1: 로그가 너무 많이 출력됩니다

**해결**:
```yaml
eraf.jpa.slow-query.threshold-ms: 2000  # 임계값을 높임
eraf.jpa.slow-query.log-stack-trace: false  # 스택 트레이스 비활성화
```

또는 로그 레벨 조정:
```yaml
logging:
  level:
    com.eraf.jpa.slowquery: WARN  # ERROR만 보고 싶다면
```

### Q2: Stack Trace가 너무 깁니다

**해결**:
```yaml
eraf.jpa.slow-query.stack-trace-depth: 5  # 상위 5개만 출력
```

### Q3: Hibernate 내부 스택이 너무 많이 포함됩니다

**해결**: 코드 레벨에서 이미 필터링되어 있습니다 (org.hibernate.* 제외)

### Q4: 특정 쿼리만 제외하고 싶습니다

**현재**: 지원하지 않음
**대안**: Custom Filter 구현 또는 로거 레벨 조정

```java
// Custom SlowQueryLoggingDataSource 상속하여 구현
if (sql.contains("SELECT 1")) {
    return;  // Health check 쿼리 제외
}
```

## 📈 성능 영향

### DataSource Wrapper 방식
- **오버헤드**: 매우 낮음 (< 1ms per query)
- **메모리**: 거의 없음
- **추천**: ✅ 프로덕션 사용 가능

### StatementInspector 방식
- **오버헤드**: 낮음
- **제한**: 정확한 실행 시간 측정 어려움
- **추천**: ⚠️ 개발/테스트 환경 권장

## 🎓 Best Practices

1. **개발 시 항상 활성화**
   - threshold-ms: 500ms
   - log-stack-trace: true

2. **프로덕션은 신중하게**
   - threshold-ms: 1000ms ~ 2000ms
   - log-stack-trace: false

3. **Critical Threshold 설정**
   - 비즈니스 요구사항에 맞게 조정
   - SLA 기준의 80% 수준 권장

4. **로그 모니터링 연동**
   - ELK Stack, Datadog 등과 연동
   - 슬로우 쿼리 발생 시 알림 설정

5. **주기적 분석**
   - 주 단위로 슬로우 쿼리 로그 분석
   - 인덱스 추가, 쿼리 최적화 진행

## 🔗 관련 클래스

- `SlowQueryProperties` - 설정 Properties
- `SlowQueryLoggingDataSource` - DataSource Wrapper (권장)
- `SlowQueryDetector` - Hibernate StatementInspector
- `SlowQueryAutoConfiguration` - Auto Configuration

## 📚 참고 자료

- [Hibernate StatementInspector](https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/resource/jdbc/spi/StatementInspector.html)
- [Spring JDBC Datasource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/package-summary.html)
- [Query Performance Optimization](https://use-the-index-luke.com/)
