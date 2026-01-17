package com.eraf.gateway.analytics.repository;

import com.eraf.gateway.analytics.domain.ApiCallRecord;
import com.eraf.gateway.analytics.domain.ApiMetrics;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 인메모리 Analytics Repository 구현체
 */
@Slf4j
public class InMemoryAnalyticsRepository implements AnalyticsRepository {

    private final List<ApiCallRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void save(ApiCallRecord record) {
        records.add(record);
        log.debug("Saved analytics record: {} {} - {} ms", record.getMethod(), record.getPath(), record.getResponseTimeMs());
    }

    @Override
    public ApiMetrics getMetricsByPath(String path, LocalDateTime from, LocalDateTime to) {
        List<ApiCallRecord> filteredRecords = records.stream()
                .filter(r -> r.getPath().equals(path))
                .filter(r -> isInRange(r.getTimestamp(), from, to))
                .collect(Collectors.toList());

        return calculateMetrics(filteredRecords, path);
    }

    @Override
    public List<ApiMetrics> getAllMetrics(LocalDateTime from, LocalDateTime to) {
        Map<String, List<ApiCallRecord>> grouped = records.stream()
                .filter(r -> isInRange(r.getTimestamp(), from, to))
                .collect(Collectors.groupingBy(ApiCallRecord::getPath));

        return grouped.entrySet().stream()
                .map(entry -> calculateMetrics(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiMetrics> getTopPaths(int limit, LocalDateTime from, LocalDateTime to) {
        return getAllMetrics(from, to).stream()
                .sorted(Comparator.comparingLong(ApiMetrics::getTotalRequests).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiMetrics> getTopErrorPaths(int limit, LocalDateTime from, LocalDateTime to) {
        return getAllMetrics(from, to).stream()
                .sorted(Comparator.comparingLong(ApiMetrics::getErrorCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiMetrics> getSlowestPaths(int limit, LocalDateTime from, LocalDateTime to) {
        return getAllMetrics(from, to).stream()
                .sorted(Comparator.comparingLong(ApiMetrics::getAvgResponseTimeMs).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiCallRecord> getRecords(LocalDateTime from, LocalDateTime to, int limit) {
        return records.stream()
                .filter(r -> isInRange(r.getTimestamp(), from, to))
                .sorted(Comparator.comparing(ApiCallRecord::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOlderThan(LocalDateTime threshold) {
        int beforeSize = records.size();
        records.removeIf(r -> r.getTimestamp().isBefore(threshold));
        int removedCount = beforeSize - records.size();
        if (removedCount > 0) {
            log.info("Deleted {} old analytics records before {}", removedCount, threshold);
        }
    }

    private ApiMetrics calculateMetrics(List<ApiCallRecord> recordList, String path) {
        if (recordList.isEmpty()) {
            return ApiMetrics.builder()
                    .path(path)
                    .method("ALL")
                    .totalRequests(0)
                    .successCount(0)
                    .errorCount(0)
                    .avgResponseTimeMs(0)
                    .minResponseTimeMs(0)
                    .maxResponseTimeMs(0)
                    .p50ResponseTimeMs(0)
                    .p95ResponseTimeMs(0)
                    .p99ResponseTimeMs(0)
                    .lastRequestTime(null)
                    .periodStart(null)
                    .periodEnd(null)
                    .build();
        }

        long totalRequests = recordList.size();
        long successCount = recordList.stream().filter(r -> r.getStatusCode() < 400).count();
        long errorCount = totalRequests - successCount;

        List<Long> responseTimes = recordList.stream()
                .map(ApiCallRecord::getResponseTimeMs)
                .sorted()
                .collect(Collectors.toList());

        long avgResponseTime = (long) responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long minResponseTime = responseTimes.get(0);
        long maxResponseTime = responseTimes.get(responseTimes.size() - 1);

        // Calculate percentiles
        long p50 = getPercentile(responseTimes, 0.50);
        long p95 = getPercentile(responseTimes, 0.95);
        long p99 = getPercentile(responseTimes, 0.99);

        LocalDateTime lastRequestTime = recordList.stream()
                .map(ApiCallRecord::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime periodStart = recordList.stream()
                .map(ApiCallRecord::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime periodEnd = lastRequestTime;

        // Get most common method
        String method = recordList.stream()
                .collect(Collectors.groupingBy(ApiCallRecord::getMethod, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("ALL");

        return ApiMetrics.builder()
                .path(path)
                .method(method)
                .totalRequests(totalRequests)
                .successCount(successCount)
                .errorCount(errorCount)
                .avgResponseTimeMs(avgResponseTime)
                .minResponseTimeMs(minResponseTime)
                .maxResponseTimeMs(maxResponseTime)
                .p50ResponseTimeMs(p50)
                .p95ResponseTimeMs(p95)
                .p99ResponseTimeMs(p99)
                .lastRequestTime(lastRequestTime)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
    }

    private long getPercentile(List<Long> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil(percentile * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }

    private boolean isInRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        return (from == null || !timestamp.isBefore(from)) &&
               (to == null || !timestamp.isAfter(to));
    }
}
