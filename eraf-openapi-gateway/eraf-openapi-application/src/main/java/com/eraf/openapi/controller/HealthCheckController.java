package com.eraf.openapi.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.health.HealthCheckResult;
import com.eraf.openapi.health.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Health Check 관리 REST API
 */
@RestController
@RequestMapping("/admin/health-checks")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    /**
     * 모든 Target의 Health Check 결과 조회
     */
    @GetMapping
    public ApiResponse<List<HealthCheckResult>> getAllHealthChecks() {
        return ApiResponse.success(healthCheckService.getAllHealthCheckResults());
    }

    /**
     * Service별 Health Check 결과 조회
     */
    @GetMapping("/service/{serviceId}")
    public ApiResponse<List<HealthCheckResult>> getHealthChecksByService(@PathVariable Long serviceId) {
        return ApiResponse.success(healthCheckService.getHealthCheckResultsByService(serviceId));
    }

    /**
     * 특정 Target에 대해 즉시 Health Check 수행
     */
    @PostMapping("/check/{targetId}")
    public ApiResponse<HealthCheckResult> performHealthCheck(@PathVariable Long targetId) {
        return ApiResponse.success(healthCheckService.performHealthCheck(targetId));
    }

    /**
     * Health Check 통계
     */
    @GetMapping("/stats")
    public ApiResponse<HealthCheckService.HealthCheckStats> getStats() {
        return ApiResponse.success(healthCheckService.getStats());
    }
}
