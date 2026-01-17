package com.eraf.gateway.analytics.advanced.controller;

import com.eraf.gateway.analytics.advanced.metrics.*;
import com.eraf.gateway.analytics.advanced.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Analytics Dashboard REST API
 * 실시간 메트릭 및 대시보드 데이터 제공
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsDashboardController {

    private final DashboardService dashboardService;

    /**
     * 전체 대시보드 스냅샷
     * GET /api/v1/analytics/dashboard?timeWindow=30
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardService.DashboardSnapshot> getDashboard(
            @RequestParam(defaultValue = "30") int timeWindow) {

        DashboardService.DashboardSnapshot snapshot = dashboardService.getDashboardSnapshot(timeWindow);
        return ResponseEntity.ok(snapshot);
    }

    /**
     * 현재 RPS (Requests Per Second)
     * GET /api/v1/analytics/metrics/rps
     */
    @GetMapping("/metrics/rps")
    public ResponseEntity<Map<String, Object>> getCurrentRPS() {
        double rps = dashboardService.getCurrentRPS();
        return ResponseEntity.ok(Map.of(
                "rps", rps,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Latency 백분위수
     * GET /api/v1/analytics/metrics/latency?path=/api/users&timeWindow=60
     */
    @GetMapping("/metrics/latency")
    public ResponseEntity<LatencyPercentiles> getLatencyMetrics(
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "60") int timeWindow) {

        LatencyPercentiles percentiles = path != null
                ? dashboardService.getLatencyPercentiles(path, timeWindow)
                : dashboardService.getLatencyPercentiles(timeWindow);

        return ResponseEntity.ok(percentiles);
    }

    /**
     * 에러율
     * GET /api/v1/analytics/metrics/error-rate?timeWindow=30
     */
    @GetMapping("/metrics/error-rate")
    public ResponseEntity<ErrorRateMetrics> getErrorRate(
            @RequestParam(defaultValue = "30") int timeWindow) {

        ErrorRateMetrics errorRate = dashboardService.getErrorRate(timeWindow);
        return ResponseEntity.ok(errorRate);
    }

    /**
     * 처리량 (Throughput)
     * GET /api/v1/analytics/metrics/throughput?timeWindow=30
     */
    @GetMapping("/metrics/throughput")
    public ResponseEntity<ThroughputMetrics> getThroughput(
            @RequestParam(defaultValue = "30") int timeWindow) {

        ThroughputMetrics throughput = dashboardService.getThroughput(timeWindow);
        return ResponseEntity.ok(throughput);
    }

    /**
     * Top Consumers
     * GET /api/v1/analytics/top/consumers?limit=10&timeWindow=60
     */
    @GetMapping("/top/consumers")
    public ResponseEntity<List<TopNMetrics.ConsumerMetric>> getTopConsumers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int timeWindow) {

        List<TopNMetrics.ConsumerMetric> topConsumers = dashboardService.getTopConsumers(limit, timeWindow);
        return ResponseEntity.ok(topConsumers);
    }

    /**
     * Top APIs
     * GET /api/v1/analytics/top/apis?limit=10&timeWindow=60
     */
    @GetMapping("/top/apis")
    public ResponseEntity<Map<String, Object>> getTopApis(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int timeWindow) {

        // 구현 필요 (DashboardService에 추가)
        return ResponseEntity.ok(Map.of("message", "Not implemented yet"));
    }

    /**
     * Top Errors
     * GET /api/v1/analytics/top/errors?limit=10&timeWindow=60
     */
    @GetMapping("/top/errors")
    public ResponseEntity<List<TopNMetrics.ErrorMetric>> getTopErrors(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int timeWindow) {

        List<TopNMetrics.ErrorMetric> topErrors = dashboardService.getTopErrors(limit, timeWindow);
        return ResponseEntity.ok(topErrors);
    }

    /**
     * Slow Endpoints
     * GET /api/v1/analytics/top/slow-endpoints?limit=10&timeWindow=60
     */
    @GetMapping("/top/slow-endpoints")
    public ResponseEntity<List<TopNMetrics.SlowEndpointMetric>> getSlowEndpoints(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "60") int timeWindow) {

        List<TopNMetrics.SlowEndpointMetric> slowEndpoints = dashboardService.getSlowEndpoints(limit, timeWindow);
        return ResponseEntity.ok(slowEndpoints);
    }

    /**
     * Health Status
     * GET /api/v1/analytics/health
     */
    @GetMapping("/health")
    public ResponseEntity<DashboardService.HealthStatus> getHealthStatus() {
        DashboardService.HealthStatus health = dashboardService.getHealthStatus();
        return ResponseEntity.ok(health);
    }
}
