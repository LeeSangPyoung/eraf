package com.eraf.gateway.analytics.service;

import com.eraf.gateway.analytics.domain.ApiCallRecord;
import com.eraf.gateway.analytics.domain.ApiMetrics;
import com.eraf.gateway.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * API Analytics 서비스
 */
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repository;

    /**
     * API 호출 기록
     */
    public void recordApiCall(String path, String method, String clientIp, String apiKey,
                               int statusCode, long responseTimeMs, long requestSize,
                               long responseSize, String userAgent, String traceId,
                               String errorCode, String errorMessage) {

        ApiCallRecord record = ApiCallRecord.builder()
                .id(UUID.randomUUID().toString())
                .path(path)
                .method(method)
                .clientIp(clientIp)
                .apiKey(apiKey)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .requestSize(requestSize)
                .responseSize(responseSize)
                .userAgent(userAgent)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();

        try {
            repository.save(record);
        } catch (Exception e) {
            log.warn("Failed to save API call record: {}", e.getMessage());
        }
    }

    /**
     * 경로별 메트릭 조회
     */
    public ApiMetrics getMetrics(String path, LocalDateTime from, LocalDateTime to) {
        return repository.getMetricsByPath(path, from, to);
    }

    /**
     * 전체 메트릭 조회
     */
    public List<ApiMetrics> getAllMetrics(LocalDateTime from, LocalDateTime to) {
        return repository.getAllMetrics(from, to);
    }

    /**
     * 대시보드용 요약 데이터
     */
    public DashboardSummary getDashboardSummary(LocalDateTime from, LocalDateTime to) {
        List<ApiMetrics> allMetrics = repository.getAllMetrics(from, to);

        long totalRequests = allMetrics.stream().mapToLong(ApiMetrics::getTotalRequests).sum();
        long totalErrors = allMetrics.stream().mapToLong(ApiMetrics::getErrorCount).sum();
        double avgResponseTime = allMetrics.stream()
                .mapToLong(m -> m.getAvgResponseTimeMs() * m.getTotalRequests())
                .sum() / (double) Math.max(totalRequests, 1);

        return DashboardSummary.builder()
                .totalRequests(totalRequests)
                .totalErrors(totalErrors)
                .errorRate(totalRequests > 0 ? (double) totalErrors / totalRequests * 100 : 0)
                .avgResponseTimeMs((long) avgResponseTime)
                .topPaths(repository.getTopPaths(10, from, to))
                .topErrorPaths(repository.getTopErrorPaths(10, from, to))
                .slowestPaths(repository.getSlowestPaths(10, from, to))
                .periodStart(from)
                .periodEnd(to)
                .build();
    }

    /**
     * 오래된 데이터 정리
     */
    public void cleanup(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        repository.deleteOlderThan(threshold);
        log.info("Cleaned up analytics data older than {} days", retentionDays);
    }

    @lombok.Builder
    @lombok.Getter
    public static class DashboardSummary {
        private final long totalRequests;
        private final long totalErrors;
        private final double errorRate;
        private final long avgResponseTimeMs;
        private final List<ApiMetrics> topPaths;
        private final List<ApiMetrics> topErrorPaths;
        private final List<ApiMetrics> slowestPaths;
        private final LocalDateTime periodStart;
        private final LocalDateTime periodEnd;
    }
}
