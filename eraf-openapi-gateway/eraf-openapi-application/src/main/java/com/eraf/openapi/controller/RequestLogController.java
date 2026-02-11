package com.eraf.openapi.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.filter.RequestLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing request logs
 */
@RestController
@RequestMapping("/admin/request-logs")
@RequiredArgsConstructor
public class RequestLogController {

    @GetMapping
    public ApiResponse<List<RequestLoggingFilter.RequestLog>> getRequestLogs(
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(RequestLoggingFilter.getRequestLogs(limit));
    }

    @DeleteMapping("/clear")
    public ApiResponse<Map<String, Object>> clearLogs() {
        RequestLoggingFilter.clearLogs();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request logs cleared");
        return ApiResponse.success(response);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(1000);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", logs.size());
        stats.put("successRate", calculateSuccessRate(logs));
        stats.put("avgDuration", calculateAvgDuration(logs));
        stats.put("requestsByMethod", groupByMethod(logs));
        stats.put("requestsByStatus", groupByStatus(logs));

        return ApiResponse.success(stats);
    }

    private double calculateSuccessRate(List<RequestLoggingFilter.RequestLog> logs) {
        if (logs.isEmpty()) return 0.0;
        long successCount = logs.stream()
            .filter(log -> log.getStatusCode() >= 200 && log.getStatusCode() < 300)
            .count();
        return (double) successCount / logs.size() * 100;
    }

    private double calculateAvgDuration(List<RequestLoggingFilter.RequestLog> logs) {
        if (logs.isEmpty()) return 0.0;
        return logs.stream()
            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
            .average()
            .orElse(0.0);
    }

    private Map<String, Long> groupByMethod(List<RequestLoggingFilter.RequestLog> logs) {
        Map<String, Long> result = new HashMap<>();
        logs.forEach(log ->
            result.merge(log.getMethod(), 1L, Long::sum)
        );
        return result;
    }

    private Map<String, Long> groupByStatus(List<RequestLoggingFilter.RequestLog> logs) {
        Map<String, Long> result = new HashMap<>();
        logs.forEach(log -> {
            String statusGroup = log.getStatusCode() / 100 + "xx";
            result.merge(statusGroup, 1L, Long::sum);
        });
        return result;
    }
}
