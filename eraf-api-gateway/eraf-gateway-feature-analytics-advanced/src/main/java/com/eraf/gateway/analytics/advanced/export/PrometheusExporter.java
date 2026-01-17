package com.eraf.gateway.analytics.advanced.export;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.metrics.ErrorRateMetrics;
import com.eraf.gateway.analytics.advanced.metrics.LatencyPercentiles;
import com.eraf.gateway.analytics.advanced.metrics.ThroughputMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Prometheus 메트릭 익스포터
 * Micrometer를 사용하여 Prometheus 포맷으로 메트릭 노출
 */
@Slf4j
@RequiredArgsConstructor
public class PrometheusExporter {

    private final MeterRegistry meterRegistry;
    private final String metricsPrefix;

    /**
     * API 호출 기록을 Prometheus 메트릭으로 변환
     */
    public void recordApiCall(AdvancedApiCall apiCall) {
        // Request counter
        Counter.builder(metricsPrefix + ".requests.total")
                .tag("path", apiCall.getPath())
                .tag("method", apiCall.getMethod())
                .tag("status", String.valueOf(apiCall.getStatusCode()))
                .tag("consumer", apiCall.getConsumerIdentifier() != null ? apiCall.getConsumerIdentifier() : "anonymous")
                .register(meterRegistry)
                .increment();

        // Latency timer
        Timer.builder(metricsPrefix + ".request.duration")
                .tag("path", apiCall.getPath())
                .tag("method", apiCall.getMethod())
                .register(meterRegistry)
                .record(Duration.ofMillis(apiCall.getTotalLatencyMs()));

        // Upstream latency
        Timer.builder(metricsPrefix + ".upstream.duration")
                .tag("path", apiCall.getPath())
                .tag("method", apiCall.getMethod())
                .register(meterRegistry)
                .record(Duration.ofMillis(apiCall.getUpstreamLatencyMs()));

        // Error counter
        if (apiCall.getStatusCode() >= 400) {
            Counter.builder(metricsPrefix + ".errors.total")
                    .tag("path", apiCall.getPath())
                    .tag("method", apiCall.getMethod())
                    .tag("status", String.valueOf(apiCall.getStatusCode()))
                    .tag("error_code", apiCall.getErrorCode() != null ? apiCall.getErrorCode() : "UNKNOWN")
                    .register(meterRegistry)
                    .increment();
        }

        // Cache hit/miss
        Counter.builder(metricsPrefix + ".cache." + (apiCall.isCacheHit() ? "hits" : "misses"))
                .tag("path", apiCall.getPath())
                .register(meterRegistry)
                .increment();

        // Request/Response size
        Counter.builder(metricsPrefix + ".request.bytes")
                .tag("path", apiCall.getPath())
                .register(meterRegistry)
                .increment(apiCall.getRequestSize());

        Counter.builder(metricsPrefix + ".response.bytes")
                .tag("path", apiCall.getPath())
                .register(meterRegistry)
                .increment(apiCall.getResponseSize());
    }

    /**
     * Latency 백분위수를 Prometheus Gauge로 노출
     */
    public void exportLatencyPercentiles(LatencyPercentiles percentiles) {
        String path = percentiles.getPath() != null ? percentiles.getPath() : "all";

        meterRegistry.gauge(metricsPrefix + ".latency.p50", percentiles.getP50());
        meterRegistry.gauge(metricsPrefix + ".latency.p75", percentiles.getP75());
        meterRegistry.gauge(metricsPrefix + ".latency.p95", percentiles.getP95());
        meterRegistry.gauge(metricsPrefix + ".latency.p99", percentiles.getP99());
        meterRegistry.gauge(metricsPrefix + ".latency.p999", percentiles.getP999());
    }

    /**
     * 에러율을 Prometheus Gauge로 노출
     */
    public void exportErrorRate(ErrorRateMetrics errorRate) {
        meterRegistry.gauge(metricsPrefix + ".error_rate.total", errorRate.getErrorRate());
        meterRegistry.gauge(metricsPrefix + ".error_rate.client", errorRate.getClientErrorRate());
        meterRegistry.gauge(metricsPrefix + ".error_rate.server", errorRate.getServerErrorRate());
        meterRegistry.gauge(metricsPrefix + ".success_rate", errorRate.getSuccessRate());
    }

    /**
     * 처리량을 Prometheus Gauge로 노출
     */
    public void exportThroughput(ThroughputMetrics throughput) {
        meterRegistry.gauge(metricsPrefix + ".throughput.rps", throughput.getRequestsPerSecond());
        meterRegistry.gauge(metricsPrefix + ".throughput.rpm", throughput.getRequestsPerMinute());
        meterRegistry.gauge(metricsPrefix + ".throughput.mbps", throughput.getMegabytesPerSecond());
    }

    /**
     * Prometheus 포맷으로 메트릭 문자열 생성
     * (Micrometer 없이 직접 생성하는 경우)
     */
    public String toPrometheusFormat(String metricName, double value, String... labels) {
        StringBuilder sb = new StringBuilder();
        sb.append(metricsPrefix).append(".").append(metricName);

        if (labels.length > 0) {
            sb.append("{");
            for (int i = 0; i < labels.length; i += 2) {
                if (i > 0) sb.append(",");
                sb.append(labels[i]).append("=\"").append(labels[i + 1]).append("\"");
            }
            sb.append("}");
        }

        sb.append(" ").append(value).append(" ").append(System.currentTimeMillis());
        return sb.toString();
    }
}
