package com.eraf.gateway.store.memory;

import com.eraf.gateway.analytics.ApiCallRecord;
import com.eraf.gateway.analytics.ApiMetrics;
import com.eraf.gateway.analytics.AnalyticsRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * 인메모리 Analytics Repository 구현
 */
@Slf4j
public class InMemoryAnalyticsRepository implements AnalyticsRepository {

    private final Deque<ApiCallRecord> records = new ConcurrentLinkedDeque<>();
    private final int maxRecords;

    public InMemoryAnalyticsRepository() {
        this(100000); // 기본 10만 건
    }

    public InMemoryAnalyticsRepository(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    @Override
    public void save(ApiCallRecord record) {
        records.addFirst(record);

        // 최대 개수 초과 시 오래된 것 삭제
        while (records.size() > maxRecords) {
            records.removeLast();
        }
    }

    @Override
    public ApiMetrics getMetricsByPath(String path, LocalDateTime from, LocalDateTime to) {
        List<ApiCallRecord> filtered = records.stream()
                .filter(r -> r.getPath().equals(path))
                .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                .collect(Collectors.toList());

        return buildMetrics(path, filtered, from, to);
    }

    @Override
    public List<ApiMetrics> getAllMetrics(LocalDateTime from, LocalDateTime to) {
        Map<String, List<ApiCallRecord>> grouped = records.stream()
                .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                .collect(Collectors.groupingBy(ApiCallRecord::getPath));

        return grouped.entrySet().stream()
                .map(e -> buildMetrics(e.getKey(), e.getValue(), from, to))
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
                .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOlderThan(LocalDateTime threshold) {
        records.removeIf(r -> r.getTimestamp().isBefore(threshold));
    }

    private ApiMetrics buildMetrics(String path, List<ApiCallRecord> recordList, LocalDateTime from, LocalDateTime to) {
        if (recordList.isEmpty()) {
            return ApiMetrics.builder()
                    .path(path)
                    .totalRequests(0)
                    .successCount(0)
                    .errorCount(0)
                    .periodStart(from)
                    .periodEnd(to)
                    .build();
        }

        long totalRequests = recordList.size();
        long successCount = recordList.stream().filter(r -> r.getStatusCode() >= 200 && r.getStatusCode() < 400).count();
        long errorCount = recordList.stream().filter(r -> r.getStatusCode() >= 400).count();

        List<Long> responseTimes = recordList.stream()
                .map(ApiCallRecord::getResponseTimeMs)
                .sorted()
                .collect(Collectors.toList());

        long avgResponseTime = (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        int size = responseTimes.size();
        long p50 = responseTimes.get((int) (size * 0.5));
        long p95 = responseTimes.get((int) Math.min(size * 0.95, size - 1));
        long p99 = responseTimes.get((int) Math.min(size * 0.99, size - 1));

        LocalDateTime lastRequestTime = recordList.stream()
                .map(ApiCallRecord::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        String method = recordList.stream()
                .map(ApiCallRecord::getMethod)
                .findFirst()
                .orElse(null);

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
                .periodStart(from)
                .periodEnd(to)
                .build();
    }
}
