package com.eraf.gateway.analytics.advanced.service;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.metrics.*;
import com.eraf.gateway.analytics.advanced.repository.TimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 실시간 대시보드 서비스
 * 현재 시스템 상태와 실시간 메트릭 제공
 */
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final TimeSeriesRepository repository;
    private final AdvancedAnalyticsService analyticsService;
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());

    /**
     * 현재 RPS (Requests Per Second)
     * 실시간으로 계산
     */
    public double getCurrentRPS() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime now = LocalDateTime.now();

        List<AdvancedApiCall> recentCalls = repository.findByTimeRange(oneMinuteAgo, now);
        return recentCalls.size() / 60.0;
    }

    /**
     * 특정 경로의 레이턴시 백분위수
     */
    public LatencyPercentiles getLatencyPercentiles(String path, int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        return analyticsService.calculatePercentiles(path, start, end);
    }

    /**
     * 전체 시스템의 레이턴시 백분위수
     */
    public LatencyPercentiles getLatencyPercentiles(int timeWindowMinutes) {
        return getLatencyPercentiles(null, timeWindowMinutes);
    }

    /**
     * 에러율 (시간 윈도우)
     */
    public ErrorRateMetrics getErrorRate(int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        return analyticsService.calculateErrorRate(start, end);
    }

    /**
     * Top N 소비자
     */
    public List<TopNMetrics.ConsumerMetric> getTopConsumers(int limit, int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        TopNMetrics metrics = analyticsService.calculateTopN(start, end, limit);
        return metrics.getTopConsumers();
    }

    /**
     * 느린 엔드포인트 Top N
     */
    public List<TopNMetrics.SlowEndpointMetric> getSlowEndpoints(int limit, int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        TopNMetrics metrics = analyticsService.calculateTopN(start, end, limit);
        return metrics.getSlowestEndpoints();
    }

    /**
     * Top 에러들
     */
    public List<TopNMetrics.ErrorMetric> getTopErrors(int limit, int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        TopNMetrics metrics = analyticsService.calculateTopN(start, end, limit);
        return metrics.getTopErrors();
    }

    /**
     * 처리량 메트릭
     */
    public ThroughputMetrics getThroughput(int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        return analyticsService.calculateThroughput(start, end);
    }

    /**
     * 전체 대시보드 스냅샷
     */
    public DashboardSnapshot getDashboardSnapshot(int timeWindowMinutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(timeWindowMinutes);

        return DashboardSnapshot.builder()
                .currentRPS(getCurrentRPS())
                .latencyPercentiles(getLatencyPercentiles(timeWindowMinutes))
                .errorRate(getErrorRate(timeWindowMinutes))
                .throughput(getThroughput(timeWindowMinutes))
                .topConsumers(getTopConsumers(5, timeWindowMinutes))
                .slowEndpoints(getSlowEndpoints(5, timeWindowMinutes))
                .topErrors(getTopErrors(5, timeWindowMinutes))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 대시보드 스냅샷 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class DashboardSnapshot {
        private final double currentRPS;
        private final LatencyPercentiles latencyPercentiles;
        private final ErrorRateMetrics errorRate;
        private final ThroughputMetrics throughput;
        private final List<TopNMetrics.ConsumerMetric> topConsumers;
        private final List<TopNMetrics.SlowEndpointMetric> slowEndpoints;
        private final List<TopNMetrics.ErrorMetric> topErrors;
        private final long timestamp;
    }

    /**
     * 헬스 체크 - 시스템 상태 확인
     */
    public HealthStatus getHealthStatus() {
        double rps = getCurrentRPS();
        ErrorRateMetrics errorRate = getErrorRate(5);

        boolean healthy = true;
        String message = "System is healthy";

        // 에러율이 10% 이상이면 unhealthy
        if (errorRate.getErrorRate() > 10.0) {
            healthy = false;
            message = String.format("High error rate: %.2f%%", errorRate.getErrorRate());
        }

        // RPS가 설정된 임계값을 초과하면 warning
        double maxRPS = 1000.0; // 설정 가능
        if (rps > maxRPS) {
            message = String.format("High load: %.2f RPS (max: %.2f)", rps, maxRPS);
        }

        return HealthStatus.builder()
                .healthy(healthy)
                .message(message)
                .currentRPS(rps)
                .errorRate(errorRate.getErrorRate())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @lombok.Getter
    @lombok.Builder
    public static class HealthStatus {
        private final boolean healthy;
        private final String message;
        private final double currentRPS;
        private final double errorRate;
        private final long timestamp;
    }
}
