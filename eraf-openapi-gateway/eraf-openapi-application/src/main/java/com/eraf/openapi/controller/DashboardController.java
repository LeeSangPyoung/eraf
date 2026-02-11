package com.eraf.openapi.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dashboard Controller
 * 대시보드용 통계 및 분석 데이터 제공
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;

    /**
     * 대시보드 전체 개요
     */
    @GetMapping("/overview")
    public ApiResponse<AnalyticsService.DashboardOverview> getOverview() {
        return ApiResponse.success(analyticsService.getOverview());
    }

    /**
     * 시간대별 트래픽 통계
     */
    @GetMapping("/traffic")
    public ApiResponse<AnalyticsService.TrafficStats> getTrafficStats() {
        return ApiResponse.success(analyticsService.getTrafficStats());
    }

    /**
     * Top APIs (호출 횟수 기준)
     */
    @GetMapping("/top-apis")
    public ApiResponse<List<AnalyticsService.ApiStats>> getTopApis(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(analyticsService.getTopApis(limit));
    }

    /**
     * 에러가 많은 APIs
     */
    @GetMapping("/error-prone-apis")
    public ApiResponse<List<AnalyticsService.ApiStats>> getErrorProneApis(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(analyticsService.getErrorProneApis(limit));
    }

    /**
     * API별 상세 통계 (Method + Path)
     */
    @GetMapping("/api-stats")
    public ApiResponse<List<AnalyticsService.ApiStats>> getApiStats() {
        return ApiResponse.success(analyticsService.getApiDetailStats());
    }
}
