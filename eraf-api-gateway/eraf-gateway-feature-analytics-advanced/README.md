# ERAF Gateway Feature - Advanced Analytics

Kong Vitals / AWS API Gateway Analytics 스타일의 고급 메트릭 및 실시간 대시보드를 제공하는 API Gateway 분석 모듈입니다.

## Features

### 1. Advanced Metrics
- **Latency Percentiles**: p50, p75, p95, p99, p999 백분위수 추적
- **Error Rate**: 4xx, 5xx 에러율을 경로별로 추적
- **Throughput**: RPS (requests/sec), MB/sec 처리량 측정
- **Top N Metrics**: 상위 소비자, API, 에러, 느린 엔드포인트 추적

### 2. Enhanced Data Collection
- Upstream vs Gateway latency 분리
- Cache hit/miss 추적
- Authentication method 기록
- Consumer/API key identifier 추적
- Custom dimensions (region, client type, version)

### 3. Time Series Data
- 1분, 5분, 1시간, 1일 단위 자동 집계
- Automatic downsampling
- 설정 가능한 데이터 보존 정책

### 4. Real-time Dashboard
- 현재 RPS 실시간 조회
- Latency percentiles by endpoint
- Error rate monitoring
- Top consumers 분석
- Slow endpoints 식별

### 5. Export Integration
- **Prometheus**: Micrometer를 통한 메트릭 노출
- **Datadog**: StatsD 프로토콜로 메트릭 전송
- **Elasticsearch**: 구조화된 로그 인덱싱 (선택사항)

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-analytics-advanced</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configuration

```yaml
eraf:
  gateway:
    analytics-advanced:
      enabled: true
      real-time-dashboard: true
      async-recording: true
      batch-size: 100

      # Prometheus 익스포트
      prometheus:
        enabled: true
        metrics-prefix: eraf_gateway

      # Datadog 익스포트
      datadog:
        enabled: false
        host: localhost
        port: 8125
```

### 3. Access Dashboard

```bash
# 전체 대시보드 스냅샷
curl http://localhost:8080/api/v1/analytics/dashboard?timeWindow=30

# 현재 RPS
curl http://localhost:8080/api/v1/analytics/metrics/rps

# Latency 백분위수
curl http://localhost:8080/api/v1/analytics/metrics/latency?timeWindow=60

# 에러율
curl http://localhost:8080/api/v1/analytics/metrics/error-rate?timeWindow=30
```

## API Endpoints

### Dashboard API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/analytics/dashboard` | GET | 전체 대시보드 스냅샷 |
| `/api/v1/analytics/metrics/rps` | GET | 현재 RPS |
| `/api/v1/analytics/metrics/latency` | GET | Latency 백분위수 |
| `/api/v1/analytics/metrics/error-rate` | GET | 에러율 |
| `/api/v1/analytics/metrics/throughput` | GET | 처리량 |
| `/api/v1/analytics/top/consumers` | GET | Top N consumers |
| `/api/v1/analytics/top/errors` | GET | Top N errors |
| `/api/v1/analytics/top/slow-endpoints` | GET | Slow endpoints |
| `/api/v1/analytics/health` | GET | 시스템 헬스 상태 |

### Query Parameters

- `timeWindow`: 시간 범위 (분 단위, 기본값: 30)
- `path`: 특정 경로 필터 (선택사항)
- `limit`: Top N 결과 개수 (기본값: 10)

## Dashboard Response Example

```json
{
  "currentRPS": 125.5,
  "latencyPercentiles": {
    "path": "/api/users",
    "p50": 25.0,
    "p75": 45.0,
    "p95": 120.0,
    "p99": 250.0,
    "p999": 500.0,
    "mean": 35.2,
    "sampleCount": 7500
  },
  "errorRate": {
    "totalRequests": 10000,
    "errorRate": 2.5,
    "clientErrorRate": 1.8,
    "serverErrorRate": 0.7,
    "successRate": 97.5
  },
  "throughput": {
    "requestsPerSecond": 125.5,
    "requestsPerMinute": 7530.0,
    "megabytesPerSecond": 5.2
  },
  "topConsumers": [
    {
      "consumerIdentifier": "api-key-xyz",
      "requestCount": 5000,
      "errorRate": 1.2,
      "avgLatencyMs": 32.5
    }
  ]
}
```

## Custom Dimensions

Custom dimensions를 사용하여 메트릭을 세분화할 수 있습니다:

```java
// HTTP 헤더를 통한 dimension 전송
X-Region: us-east-1
X-Client-Type: mobile
X-App-Name: my-app
X-Environment: production
```

이러한 dimension은 자동으로 수집되어 필터링 및 그룹핑에 사용됩니다.

## Prometheus Integration

### 1. Enable Prometheus Export

```yaml
eraf:
  gateway:
    analytics-advanced:
      prometheus:
        enabled: true
        metrics-prefix: eraf_gateway
```

### 2. Add Prometheus Dependency

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 3. Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'eraf-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### 4. Available Metrics

```
# Request counter by path, method, status
eraf_gateway_requests_total{path="/api/users",method="GET",status="200"} 1000

# Request duration (histogram)
eraf_gateway_request_duration_seconds{path="/api/users",method="GET",quantile="0.95"} 0.120

# Upstream duration
eraf_gateway_upstream_duration_seconds{path="/api/users",method="GET",quantile="0.95"} 0.080

# Error counter
eraf_gateway_errors_total{path="/api/users",method="GET",status="500",error_code="INTERNAL_ERROR"} 10

# Cache metrics
eraf_gateway_cache_hits{path="/api/users"} 850
eraf_gateway_cache_misses{path="/api/users"} 150

# Throughput
eraf_gateway_throughput_rps 125.5
eraf_gateway_throughput_mbps 5.2

# Error rate
eraf_gateway_error_rate_total 2.5
eraf_gateway_error_rate_client 1.8
eraf_gateway_error_rate_server 0.7
```

### 5. Grafana Dashboard

Prometheus 메트릭을 Grafana에서 시각화:

```promql
# P95 Latency by endpoint
histogram_quantile(0.95,
  rate(eraf_gateway_request_duration_seconds_bucket[5m]))

# Error rate
sum(rate(eraf_gateway_errors_total[5m])) /
sum(rate(eraf_gateway_requests_total[5m])) * 100

# RPS by path
sum(rate(eraf_gateway_requests_total[1m])) by (path)

# Cache hit rate
sum(rate(eraf_gateway_cache_hits[5m])) /
(sum(rate(eraf_gateway_cache_hits[5m])) + sum(rate(eraf_gateway_cache_misses[5m]))) * 100
```

## Datadog Integration

### 1. Enable Datadog Export

```yaml
eraf:
  gateway:
    analytics-advanced:
      datadog:
        enabled: true
        host: localhost
        port: 8125
        metrics-prefix: eraf.gateway
        environment: production
```

### 2. Datadog Agent Configuration

```yaml
# datadog.yaml
dogstatsd_port: 8125
```

### 3. Available Metrics

StatsD 포맷으로 전송되는 메트릭:

```
# Counter
eraf.gateway.requests.total:1|c|#path:/api/users,method:GET,status:200,env:production

# Timing
eraf.gateway.request.duration:125|ms|#path:/api/users,method:GET,env:production
eraf.gateway.upstream.duration:80|ms|#path:/api/users,method:GET,env:production

# Gauge
eraf.gateway.throughput.rps:125.5|g|#env:production
eraf.gateway.error_rate.total:2.5|g|#env:production
```

### 4. Datadog Dashboard

Datadog에서 대시보드 생성:

```
# Latency P95
p95:eraf.gateway.request.duration{*} by {path}

# Error rate
sum:eraf.gateway.errors.total{*}.as_rate() / sum:eraf.gateway.requests.total{*}.as_rate() * 100

# Top endpoints by traffic
top(sum:eraf.gateway.requests.total{*}.as_rate() by {path}, 10, 'mean', 'desc')
```

## Performance Tuning

### 1. Async Recording

비동기 기록으로 성능 향상:

```yaml
eraf:
  gateway:
    analytics-advanced:
      async-recording: true
      batch-size: 100
      async-threads: 2
```

### 2. Storage Size

메모리 사용량 조절:

```yaml
eraf:
  gateway:
    analytics-advanced:
      max-storage-size: 10000  # 최대 API 호출 기록 수
```

### 3. Retention Policy

오래된 데이터 자동 삭제:

```yaml
eraf:
  gateway:
    analytics-advanced:
      retention-policy:
        raw-data-retention-hours: 24
        one-minute-retention-days: 7
        five-minute-retention-days: 30
```

### 4. Aggregation Intervals

필요한 집계 간격만 활성화:

```yaml
eraf:
  gateway:
    analytics-advanced:
      aggregation-intervals:
        - ONE_MINUTE
        - FIVE_MINUTES
        # ONE_HOUR, ONE_DAY는 필요시 활성화
```

## Production Considerations

### 1. TimescaleDB / InfluxDB 사용

프로덕션 환경에서는 전용 시계열 DB 사용 권장:

```java
@Bean
public TimeSeriesRepository timeSeriesRepository() {
    return new TimescaleDBRepository(dataSource);
    // or
    // return new InfluxDBRepository(influxDBClient);
}
```

### 2. Elasticsearch 인덱싱

대용량 로그 분석을 위해 Elasticsearch 사용:

```yaml
eraf:
  gateway:
    analytics-advanced:
      elasticsearch:
        enabled: true
        host: elasticsearch.example.com
        port: 9200
        index-prefix: eraf-gateway
        bulk-batch-size: 1000
```

### 3. Monitoring & Alerting

Prometheus Alertmanager 설정:

```yaml
# alerts.yml
groups:
  - name: eraf_gateway
    rules:
      - alert: HighErrorRate
        expr: eraf_gateway_error_rate_total > 5
        for: 5m
        annotations:
          summary: "High error rate detected"

      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(eraf_gateway_request_duration_seconds_bucket[5m])) > 1
        for: 5m
        annotations:
          summary: "P95 latency above 1 second"
```

## Architecture

```
┌─────────────────────────────────────────────┐
│         AdvancedAnalyticsFilter             │
│  (LOWEST_PRECEDENCE - 10, same as basic)    │
└─────────────────┬───────────────────────────┘
                  │
                  ├─> Collect detailed metrics
                  │   (latency breakdown, cache, auth)
                  │
                  v
┌─────────────────────────────────────────────┐
│       AdvancedAnalyticsService              │
│  - Async batch recording                    │
│  - Calculate percentiles                    │
│  - Aggregate time series                    │
└─────────────────┬───────────────────────────┘
                  │
    ┌─────────────┼─────────────┬─────────────┐
    │             │             │             │
    v             v             v             v
┌─────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐
│TimeSeris│ │Prometheus│ │ Datadog  │ │Elasticse─│
│Repository│ │ Exporter│ │ Exporter │ │arch Exp. │
└─────────┘ └─────────┘ └──────────┘ └──────────┘
    │
    v
┌─────────────────────────────────────────────┐
│          DashboardService                   │
│  - Real-time RPS                            │
│  - Latency percentiles                      │
│  - Error rates                              │
│  - Top N metrics                            │
└─────────────────┬───────────────────────────┘
                  │
                  v
┌─────────────────────────────────────────────┐
│      AnalyticsDashboardController           │
│  REST API for dashboard queries             │
└─────────────────────────────────────────────┘
```

## Query Examples

### 1. 특정 경로의 레이턴시 분석

```bash
curl "http://localhost:8080/api/v1/analytics/metrics/latency?path=/api/users&timeWindow=60"
```

### 2. 에러율이 높은 경로 찾기

```bash
curl "http://localhost:8080/api/v1/analytics/metrics/error-rate?timeWindow=30" | jq '.errorsByEndpoint'
```

### 3. Top 10 consumers

```bash
curl "http://localhost:8080/api/v1/analytics/top/consumers?limit=10&timeWindow=120"
```

### 4. Slow endpoints 식별

```bash
curl "http://localhost:8080/api/v1/analytics/top/slow-endpoints?limit=5&timeWindow=60"
```

### 5. 시스템 헬스 체크

```bash
curl "http://localhost:8080/api/v1/analytics/health"
```

## Comparison with Base Analytics

| Feature | Base Analytics | Advanced Analytics |
|---------|---------------|-------------------|
| Request counting | ✅ | ✅ |
| Response time | ✅ Average | ✅ Percentiles (p50-p999) |
| Error tracking | ✅ Basic | ✅ Detailed by code |
| Throughput | ❌ | ✅ RPS, MB/s |
| Cache metrics | ❌ | ✅ Hit/miss rates |
| Latency breakdown | ❌ | ✅ Gateway vs Upstream |
| Custom dimensions | ❌ | ✅ Region, client type, etc. |
| Time series aggregation | ❌ | ✅ 1m, 5m, 1h, 1d |
| Real-time dashboard | ❌ | ✅ REST API |
| Prometheus export | ❌ | ✅ Via Micrometer |
| Datadog export | ❌ | ✅ StatsD protocol |
| Top N metrics | ❌ | ✅ Consumers, APIs, Errors |

## License

Copyright (c) 2024 ERAF Project
