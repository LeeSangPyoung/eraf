package com.eraf.gateway.analytics.advanced.export;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.metrics.ErrorRateMetrics;
import com.eraf.gateway.analytics.advanced.metrics.LatencyPercentiles;
import com.eraf.gateway.analytics.advanced.metrics.ThroughputMetrics;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Datadog StatsD 익스포터
 * Datadog Agent의 StatsD 프로토콜을 사용하여 메트릭 전송
 */
@Slf4j
public class DatadogExporter {

    private final String host;
    private final int port;
    private final String metricsPrefix;
    private final String environment;
    private DatagramSocket socket;

    public DatadogExporter(String host, int port, String metricsPrefix, String environment) {
        this.host = host;
        this.port = port;
        this.metricsPrefix = metricsPrefix;
        this.environment = environment;
        try {
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            log.error("Failed to create DatagramSocket for Datadog", e);
        }
    }

    /**
     * API 호출을 Datadog 메트릭으로 전송
     */
    public void recordApiCall(AdvancedApiCall apiCall) {
        List<String> metrics = new ArrayList<>();

        // Request counter
        metrics.add(counter(
                "requests.total",
                1,
                tag("path", apiCall.getPath()),
                tag("method", apiCall.getMethod()),
                tag("status", String.valueOf(apiCall.getStatusCode())),
                tag("env", environment)
        ));

        // Latency timer
        metrics.add(timing(
                "request.duration",
                apiCall.getTotalLatencyMs(),
                tag("path", apiCall.getPath()),
                tag("method", apiCall.getMethod()),
                tag("env", environment)
        ));

        // Upstream latency
        metrics.add(timing(
                "upstream.duration",
                apiCall.getUpstreamLatencyMs(),
                tag("path", apiCall.getPath()),
                tag("method", apiCall.getMethod()),
                tag("env", environment)
        ));

        // Gateway latency
        metrics.add(timing(
                "gateway.duration",
                apiCall.getGatewayLatencyMs(),
                tag("path", apiCall.getPath()),
                tag("method", apiCall.getMethod()),
                tag("env", environment)
        ));

        // Error counter
        if (apiCall.getStatusCode() >= 400) {
            metrics.add(counter(
                    "errors.total",
                    1,
                    tag("path", apiCall.getPath()),
                    tag("method", apiCall.getMethod()),
                    tag("status", String.valueOf(apiCall.getStatusCode())),
                    tag("error_code", apiCall.getErrorCode() != null ? apiCall.getErrorCode() : "UNKNOWN"),
                    tag("env", environment)
            ));
        }

        // Cache metrics
        metrics.add(counter(
                "cache." + (apiCall.isCacheHit() ? "hits" : "misses"),
                1,
                tag("path", apiCall.getPath()),
                tag("env", environment)
        ));

        // Request/Response size
        metrics.add(counter("request.bytes", apiCall.getRequestSize(), tag("path", apiCall.getPath()), tag("env", environment)));
        metrics.add(counter("response.bytes", apiCall.getResponseSize(), tag("path", apiCall.getPath()), tag("env", environment)));

        // Send all metrics
        metrics.forEach(this::send);
    }

    /**
     * Latency 백분위수 전송
     */
    public void exportLatencyPercentiles(LatencyPercentiles percentiles) {
        String path = percentiles.getPath() != null ? percentiles.getPath() : "all";

        send(gauge("latency.p50", percentiles.getP50(), tag("path", path), tag("env", environment)));
        send(gauge("latency.p75", percentiles.getP75(), tag("path", path), tag("env", environment)));
        send(gauge("latency.p95", percentiles.getP95(), tag("path", path), tag("env", environment)));
        send(gauge("latency.p99", percentiles.getP99(), tag("path", path), tag("env", environment)));
        send(gauge("latency.p999", percentiles.getP999(), tag("path", path), tag("env", environment)));
    }

    /**
     * 에러율 전송
     */
    public void exportErrorRate(ErrorRateMetrics errorRate) {
        send(gauge("error_rate.total", errorRate.getErrorRate(), tag("env", environment)));
        send(gauge("error_rate.client", errorRate.getClientErrorRate(), tag("env", environment)));
        send(gauge("error_rate.server", errorRate.getServerErrorRate(), tag("env", environment)));
        send(gauge("success_rate", errorRate.getSuccessRate(), tag("env", environment)));
    }

    /**
     * 처리량 전송
     */
    public void exportThroughput(ThroughputMetrics throughput) {
        send(gauge("throughput.rps", throughput.getRequestsPerSecond(), tag("env", environment)));
        send(gauge("throughput.rpm", throughput.getRequestsPerMinute(), tag("env", environment)));
        send(gauge("throughput.mbps", throughput.getMegabytesPerSecond(), tag("env", environment)));
    }

    /**
     * StatsD counter 포맷
     */
    private String counter(String metric, long value, String... tags) {
        return String.format("%s.%s:%d|c|%s", metricsPrefix, metric, value, joinTags(tags));
    }

    /**
     * StatsD gauge 포맷
     */
    private String gauge(String metric, double value, String... tags) {
        return String.format("%s.%s:%.2f|g|%s", metricsPrefix, metric, value, joinTags(tags));
    }

    /**
     * StatsD timing 포맷
     */
    private String timing(String metric, long value, String... tags) {
        return String.format("%s.%s:%d|ms|%s", metricsPrefix, metric, value, joinTags(tags));
    }

    /**
     * Tag 생성
     */
    private String tag(String key, String value) {
        return key + ":" + value;
    }

    /**
     * Tag 배열을 문자열로 결합
     */
    private String joinTags(String... tags) {
        if (tags.length == 0) return "";
        return "#" + String.join(",", tags);
    }

    /**
     * UDP로 메트릭 전송
     */
    private void send(String metric) {
        if (socket == null) {
            log.warn("DatagramSocket is not initialized. Skipping metric: {}", metric);
            return;
        }

        try {
            byte[] data = metric.getBytes();
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            log.debug("Sent metric to Datadog: {}", metric);
        } catch (IOException e) {
            log.error("Failed to send metric to Datadog: {}", metric, e);
        }
    }

    /**
     * 소켓 종료
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            log.info("Datadog exporter socket closed");
        }
    }
}
