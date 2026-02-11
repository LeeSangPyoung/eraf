package com.eraf.openapi.service;

import com.eraf.openapi.filter.RequestLoggingFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service
 * 요청 로그를 기반으로 통계 및 분석 데이터 제공
 */
@Service
public class AnalyticsService {

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:00");

    /**
     * 대시보드 전체 개요 통계
     */
    public DashboardOverview getOverview() {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(10000);

        DashboardOverview overview = new DashboardOverview();
        overview.setTotalRequests(logs.size());
        overview.setSuccessRate(calculateSuccessRate(logs));
        overview.setAvgResponseTime(calculateAvgDuration(logs));
        overview.setErrorRate(calculateErrorRate(logs));

        // 최근 1시간 통계
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        List<RequestLoggingFilter.RequestLog> recentLogs = logs.stream()
                .filter(log -> log.getTimestamp().toEpochMilli() > oneHourAgo)
                .toList();
        overview.setRecentRequestsPerHour(recentLogs.size());

        // 가장 느린 API Top 5
        overview.setSlowestApis(findSlowestApis(logs, 5));

        // 가장 많이 호출된 API Top 5
        overview.setTopApis(findTopApis(logs, 5));

        return overview;
    }

    /**
     * 시간대별 트래픽 통계 (최근 24시간)
     */
    public TrafficStats getTrafficStats() {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(10000);

        // 최근 24시간
        long dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        List<RequestLoggingFilter.RequestLog> last24Hours = logs.stream()
                .filter(log -> log.getTimestamp().toEpochMilli() > dayAgo)
                .toList();

        TrafficStats stats = new TrafficStats();
        stats.setHourlyData(groupByHour(last24Hours));
        stats.setMethodDistribution(groupByMethod(last24Hours));
        stats.setStatusDistribution(groupByStatus(last24Hours));

        return stats;
    }

    /**
     * Top APIs 통계 (호출 횟수 기준)
     */
    public List<ApiStats> getTopApis(int limit) {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(10000);
        return findTopApis(logs, limit);
    }

    /**
     * 에러가 많은 APIs 통계
     */
    public List<ApiStats> getErrorProneApis(int limit) {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(10000);

        // Path별 그룹핑
        Map<String, List<RequestLoggingFilter.RequestLog>> byPath = logs.stream()
                .collect(Collectors.groupingBy(RequestLoggingFilter.RequestLog::getPath));

        return byPath.entrySet().stream()
                .map(entry -> {
                    String path = entry.getKey();
                    List<RequestLoggingFilter.RequestLog> apiLogs = entry.getValue();

                    long errorCount = apiLogs.stream()
                            .filter(log -> log.getStatusCode() >= 400)
                            .count();

                    double errorRate = apiLogs.isEmpty() ? 0.0 : (double) errorCount / apiLogs.size() * 100;

                    ApiStats stats = new ApiStats();
                    stats.setPath(path);
                    stats.setMethod("ALL");
                    stats.setRequestCount(apiLogs.size());
                    stats.setErrorCount(errorCount);
                    stats.setErrorRate(errorRate);
                    stats.setAvgResponseTime(apiLogs.stream()
                            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                            .average()
                            .orElse(0.0));

                    return stats;
                })
                .filter(stats -> stats.getErrorRate() > 0)
                .sorted(Comparator.comparingDouble(ApiStats::getErrorRate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Path + Method별 상세 통계
     */
    public List<ApiStats> getApiDetailStats() {
        List<RequestLoggingFilter.RequestLog> logs = RequestLoggingFilter.getRequestLogs(10000);

        // Path + Method로 그룹핑
        Map<String, List<RequestLoggingFilter.RequestLog>> byPathMethod = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getMethod() + " " + log.getPath()));

        return byPathMethod.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split(" ", 2);
                    String method = parts[0];
                    String path = parts.length > 1 ? parts[1] : "";
                    List<RequestLoggingFilter.RequestLog> apiLogs = entry.getValue();

                    long errorCount = apiLogs.stream()
                            .filter(log -> log.getStatusCode() >= 400)
                            .count();

                    ApiStats stats = new ApiStats();
                    stats.setMethod(method);
                    stats.setPath(path);
                    stats.setRequestCount(apiLogs.size());
                    stats.setErrorCount(errorCount);
                    stats.setErrorRate(apiLogs.isEmpty() ? 0.0 : (double) errorCount / apiLogs.size() * 100);
                    stats.setAvgResponseTime(apiLogs.stream()
                            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                            .average()
                            .orElse(0.0));

                    return stats;
                })
                .sorted(Comparator.comparingInt(ApiStats::getRequestCount).reversed())
                .collect(Collectors.toList());
    }

    // ===== Helper Methods =====

    private double calculateSuccessRate(List<RequestLoggingFilter.RequestLog> logs) {
        if (logs.isEmpty()) return 100.0;
        long successCount = logs.stream()
                .filter(log -> log.getStatusCode() >= 200 && log.getStatusCode() < 300)
                .count();
        return (double) successCount / logs.size() * 100;
    }

    private double calculateErrorRate(List<RequestLoggingFilter.RequestLog> logs) {
        if (logs.isEmpty()) return 0.0;
        long errorCount = logs.stream()
                .filter(log -> log.getStatusCode() >= 400)
                .count();
        return (double) errorCount / logs.size() * 100;
    }

    private double calculateAvgDuration(List<RequestLoggingFilter.RequestLog> logs) {
        if (logs.isEmpty()) return 0.0;
        return logs.stream()
                .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                .average()
                .orElse(0.0);
    }

    private List<ApiStats> findTopApis(List<RequestLoggingFilter.RequestLog> logs, int limit) {
        Map<String, List<RequestLoggingFilter.RequestLog>> byPath = logs.stream()
                .collect(Collectors.groupingBy(RequestLoggingFilter.RequestLog::getPath));

        return byPath.entrySet().stream()
                .map(entry -> {
                    String path = entry.getKey();
                    List<RequestLoggingFilter.RequestLog> apiLogs = entry.getValue();

                    ApiStats stats = new ApiStats();
                    stats.setPath(path);
                    stats.setMethod("ALL");
                    stats.setRequestCount(apiLogs.size());
                    stats.setAvgResponseTime(apiLogs.stream()
                            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                            .average()
                            .orElse(0.0));

                    return stats;
                })
                .sorted(Comparator.comparingInt(ApiStats::getRequestCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<ApiStats> findSlowestApis(List<RequestLoggingFilter.RequestLog> logs, int limit) {
        Map<String, List<RequestLoggingFilter.RequestLog>> byPath = logs.stream()
                .collect(Collectors.groupingBy(RequestLoggingFilter.RequestLog::getPath));

        return byPath.entrySet().stream()
                .map(entry -> {
                    String path = entry.getKey();
                    List<RequestLoggingFilter.RequestLog> apiLogs = entry.getValue();

                    double avgDuration = apiLogs.stream()
                            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                            .average()
                            .orElse(0.0);

                    ApiStats stats = new ApiStats();
                    stats.setPath(path);
                    stats.setMethod("ALL");
                    stats.setRequestCount(apiLogs.size());
                    stats.setAvgResponseTime(avgDuration);

                    return stats;
                })
                .sorted(Comparator.comparingDouble(ApiStats::getAvgResponseTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<HourlyData> groupByHour(List<RequestLoggingFilter.RequestLog> logs) {
        Map<String, List<RequestLoggingFilter.RequestLog>> byHour = logs.stream()
                .collect(Collectors.groupingBy(log -> {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                            log.getTimestamp(),
                            ZoneId.systemDefault()
                    );
                    return dateTime.format(HOUR_FORMATTER);
                }));

        return byHour.entrySet().stream()
                .map(entry -> {
                    String hour = entry.getKey();
                    List<RequestLoggingFilter.RequestLog> hourLogs = entry.getValue();

                    HourlyData data = new HourlyData();
                    data.setHour(hour);
                    data.setRequestCount(hourLogs.size());
                    data.setSuccessCount((int) hourLogs.stream()
                            .filter(log -> log.getStatusCode() >= 200 && log.getStatusCode() < 300)
                            .count());
                    data.setErrorCount((int) hourLogs.stream()
                            .filter(log -> log.getStatusCode() >= 400)
                            .count());
                    data.setAvgResponseTime(hourLogs.stream()
                            .mapToLong(RequestLoggingFilter.RequestLog::getDuration)
                            .average()
                            .orElse(0.0));

                    return data;
                })
                .sorted(Comparator.comparing(HourlyData::getHour))
                .collect(Collectors.toList());
    }

    private Map<String, Long> groupByMethod(List<RequestLoggingFilter.RequestLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        RequestLoggingFilter.RequestLog::getMethod,
                        Collectors.counting()
                ));
    }

    private Map<String, Long> groupByStatus(List<RequestLoggingFilter.RequestLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> (log.getStatusCode() / 100) + "xx",
                        Collectors.counting()
                ));
    }

    // ===== DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardOverview {
        private int totalRequests;
        private double successRate;
        private double errorRate;
        private double avgResponseTime;
        private int recentRequestsPerHour;
        private List<ApiStats> topApis;
        private List<ApiStats> slowestApis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficStats {
        private List<HourlyData> hourlyData;
        private Map<String, Long> methodDistribution;
        private Map<String, Long> statusDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiStats {
        private String method;
        private String path;
        private int requestCount;
        private long errorCount;
        private double errorRate;
        private double avgResponseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData {
        private String hour;
        private int requestCount;
        private int successCount;
        private int errorCount;
        private double avgResponseTime;
    }
}
