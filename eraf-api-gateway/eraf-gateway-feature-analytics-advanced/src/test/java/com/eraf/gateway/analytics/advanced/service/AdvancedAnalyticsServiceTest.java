package com.eraf.gateway.analytics.advanced.service;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.metrics.ErrorRateMetrics;
import com.eraf.gateway.analytics.advanced.metrics.LatencyPercentiles;
import com.eraf.gateway.analytics.advanced.metrics.ThroughputMetrics;
import com.eraf.gateway.analytics.advanced.metrics.TopNMetrics;
import com.eraf.gateway.analytics.advanced.repository.InMemoryTimeSeriesRepository;
import com.eraf.gateway.analytics.advanced.repository.TimeSeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdvancedAnalyticsService 테스트
 */
class AdvancedAnalyticsServiceTest {

    private TimeSeriesRepository repository;
    private AdvancedAnalyticsService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTimeSeriesRepository(10000);
        service = new AdvancedAnalyticsService(repository, 100, 2);
    }

    @Test
    void testRecordWithDimensions() {
        // Given
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("region", "us-east-1");
        dimensions.put("client_type", "mobile");

        AdvancedApiCall apiCall = AdvancedApiCall.builder()
                .id("test-1")
                .path("/api/users")
                .method("GET")
                .clientIp("192.168.1.1")
                .statusCode(200)
                .totalLatencyMs(100L)
                .upstreamLatencyMs(80L)
                .gatewayLatencyMs(20L)
                .requestSize(1024L)
                .responseSize(2048L)
                .cacheHit(false)
                .authMethod("API_KEY")
                .consumerIdentifier("api-key-123")
                .region("us-east-1")
                .clientType("mobile")
                .customDimensions(dimensions)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        service.recordWithDimensions(apiCall);

        // Then
        List<AdvancedApiCall> calls = repository.findByTimeRange(
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(1)
        );
        assertEquals(1, calls.size());
        assertEquals("/api/users", calls.get(0).getPath());
    }

    @Test
    void testCalculatePercentiles() {
        // Given - 레이턴시가 다른 여러 API 호출 생성
        for (int i = 1; i <= 100; i++) {
            AdvancedApiCall call = createApiCall("/api/test", "GET", i * 10L);
            repository.save(call);
        }

        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        // When
        LatencyPercentiles percentiles = service.calculatePercentiles("/api/test", start, end);

        // Then
        assertNotNull(percentiles);
        assertEquals(100, percentiles.getSampleCount());
        assertTrue(percentiles.getP50() > 0);
        assertTrue(percentiles.getP95() > percentiles.getP50());
        assertTrue(percentiles.getP99() > percentiles.getP95());
    }

    @Test
    void testCalculateErrorRate() {
        // Given - 성공 80개, 4xx 에러 15개, 5xx 에러 5개
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 80; i++) {
            repository.save(createApiCall("/api/test", "GET", 100L, 200));
        }
        for (int i = 0; i < 15; i++) {
            repository.save(createApiCall("/api/test", "GET", 100L, 404));
        }
        for (int i = 0; i < 5; i++) {
            repository.save(createApiCall("/api/test", "GET", 100L, 500));
        }

        LocalDateTime start = now.minusMinutes(5);
        LocalDateTime end = now.plusMinutes(1);

        // When
        ErrorRateMetrics errorRate = service.calculateErrorRate(start, end);

        // Then
        assertNotNull(errorRate);
        assertEquals(100, errorRate.getTotalRequests());
        assertEquals(80, errorRate.getSuccessCount());
        assertEquals(15, errorRate.getClientErrorCount());
        assertEquals(5, errorRate.getServerErrorCount());
        assertEquals(20.0, errorRate.getErrorRate(), 0.1);
        assertEquals(15.0, errorRate.getClientErrorRate(), 0.1);
        assertEquals(5.0, errorRate.getServerErrorRate(), 0.1);
    }

    @Test
    void testCalculateThroughput() {
        // Given - 60초 동안 120개의 요청
        LocalDateTime start = LocalDateTime.now().minusSeconds(60);
        LocalDateTime end = LocalDateTime.now();

        for (int i = 0; i < 120; i++) {
            AdvancedApiCall call = createApiCall("/api/test", "GET", 100L);
            repository.save(call);
        }

        // When
        ThroughputMetrics throughput = service.calculateThroughput(start, end);

        // Then
        assertNotNull(throughput);
        assertEquals(120, throughput.getTotalRequests());
        assertTrue(throughput.getRequestsPerSecond() > 0);
        assertTrue(throughput.getTotalBytesIn() > 0);
        assertTrue(throughput.getTotalBytesOut() > 0);
    }

    @Test
    void testCalculateTopN() {
        // Given - 다양한 consumer와 경로의 API 호출
        LocalDateTime now = LocalDateTime.now();

        // Consumer A: 50 requests
        for (int i = 0; i < 50; i++) {
            repository.save(createApiCallWithConsumer("/api/test", "GET", 100L, 200, "consumer-a"));
        }

        // Consumer B: 30 requests with some errors
        for (int i = 0; i < 25; i++) {
            repository.save(createApiCallWithConsumer("/api/test", "GET", 100L, 200, "consumer-b"));
        }
        for (int i = 0; i < 5; i++) {
            repository.save(createApiCallWithConsumer("/api/test", "GET", 100L, 500, "consumer-b"));
        }

        // Consumer C: 20 requests
        for (int i = 0; i < 20; i++) {
            repository.save(createApiCallWithConsumer("/api/test", "GET", 100L, 200, "consumer-c"));
        }

        LocalDateTime start = now.minusMinutes(5);
        LocalDateTime end = now.plusMinutes(1);

        // When
        TopNMetrics topN = service.calculateTopN(start, end, 10);

        // Then
        assertNotNull(topN);
        List<TopNMetrics.ConsumerMetric> topConsumers = topN.getTopConsumers();
        assertNotNull(topConsumers);
        assertTrue(topConsumers.size() >= 3);
        assertEquals("consumer-a", topConsumers.get(0).getConsumerIdentifier());
        assertEquals(50, topConsumers.get(0).getRequestCount());
    }

    private AdvancedApiCall createApiCall(String path, String method, long latencyMs) {
        return createApiCall(path, method, latencyMs, 200);
    }

    private AdvancedApiCall createApiCall(String path, String method, long latencyMs, int statusCode) {
        return AdvancedApiCall.builder()
                .id(java.util.UUID.randomUUID().toString())
                .path(path)
                .method(method)
                .clientIp("192.168.1.1")
                .statusCode(statusCode)
                .totalLatencyMs(latencyMs)
                .upstreamLatencyMs(latencyMs - 20)
                .gatewayLatencyMs(20L)
                .requestSize(1024L)
                .responseSize(2048L)
                .cacheHit(false)
                .authMethod("API_KEY")
                .consumerIdentifier("anonymous")
                .region("us-east-1")
                .clientType("web")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdvancedApiCall createApiCallWithConsumer(String path, String method, long latencyMs,
                                                       int statusCode, String consumer) {
        return AdvancedApiCall.builder()
                .id(java.util.UUID.randomUUID().toString())
                .path(path)
                .method(method)
                .clientIp("192.168.1.1")
                .statusCode(statusCode)
                .totalLatencyMs(latencyMs)
                .upstreamLatencyMs(latencyMs - 20)
                .gatewayLatencyMs(20L)
                .requestSize(1024L)
                .responseSize(2048L)
                .cacheHit(false)
                .authMethod("API_KEY")
                .consumerIdentifier(consumer)
                .region("us-east-1")
                .clientType("web")
                .timestamp(LocalDateTime.now())
                .errorCode(statusCode >= 400 ? "ERROR_" + statusCode : null)
                .build();
    }
}
