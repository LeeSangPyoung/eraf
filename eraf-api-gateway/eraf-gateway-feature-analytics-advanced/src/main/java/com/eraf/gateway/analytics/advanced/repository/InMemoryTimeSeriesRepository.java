package com.eraf.gateway.analytics.advanced.repository;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.domain.TimeSeriesMetric;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * 인메모리 시계열 저장소 구현
 * 실제 프로덕션에서는 InfluxDB, TimescaleDB, Prometheus 등을 사용
 */
@Slf4j
public class InMemoryTimeSeriesRepository implements TimeSeriesRepository {

    private final Deque<AdvancedApiCall> apiCalls = new ConcurrentLinkedDeque<>();
    private final Map<String, Deque<TimeSeriesMetric>> metrics = new ConcurrentHashMap<>();
    private final int maxSize;

    public InMemoryTimeSeriesRepository(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void save(AdvancedApiCall apiCall) {
        apiCalls.addFirst(apiCall);
        trimToSize(apiCalls, maxSize);
    }

    @Override
    public void saveAll(List<AdvancedApiCall> calls) {
        calls.forEach(this::save);
    }

    @Override
    public void saveMetric(TimeSeriesMetric metric) {
        metrics.computeIfAbsent(metric.getMetricName(), k -> new ConcurrentLinkedDeque<>())
                .addFirst(metric);
        trimToSize(metrics.get(metric.getMetricName()), maxSize);
    }

    @Override
    public void saveMetrics(List<TimeSeriesMetric> metricsList) {
        metricsList.forEach(this::saveMetric);
    }

    @Override
    public List<AdvancedApiCall> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdvancedApiCall> findByPath(String path, LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> call.getPath().equals(path))
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdvancedApiCall> findByConsumer(String consumerIdentifier, LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> Objects.equals(call.getConsumerIdentifier(), consumerIdentifier))
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSeriesMetric> findMetrics(
            String metricName,
            LocalDateTime start,
            LocalDateTime end,
            TimeSeriesMetric.AggregationWindow window) {
        Deque<TimeSeriesMetric> metricDeque = metrics.get(metricName);
        if (metricDeque == null) {
            return Collections.emptyList();
        }

        return metricDeque.stream()
                .filter(m -> m.getWindow() == window)
                .filter(m -> !m.getTimestamp().isBefore(start) && !m.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSeriesMetric> findMetricsWithDimensions(
            String metricName,
            LocalDateTime start,
            LocalDateTime end,
            TimeSeriesMetric.AggregationWindow window,
            Map<String, String> dimensions) {
        return findMetrics(metricName, start, end, window).stream()
                .filter(m -> matchesDimensions(m, dimensions))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findLatencies(String path, LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> call.getPath().equals(path))
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .map(AdvancedApiCall::getTotalLatencyMs)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> countErrorsByCode(LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .filter(call -> call.getErrorCode() != null)
                .collect(Collectors.groupingBy(
                        AdvancedApiCall::getErrorCode,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> countRequestsByPath(LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .collect(Collectors.groupingBy(
                        AdvancedApiCall::getPath,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> countRequestsByConsumer(LocalDateTime start, LocalDateTime end) {
        return apiCalls.stream()
                .filter(call -> !call.getTimestamp().isBefore(start) && !call.getTimestamp().isAfter(end))
                .filter(call -> call.getConsumerIdentifier() != null)
                .collect(Collectors.groupingBy(
                        AdvancedApiCall::getConsumerIdentifier,
                        Collectors.counting()
                ));
    }

    @Override
    public long deleteOlderThan(LocalDateTime threshold) {
        long deleted = apiCalls.removeIf(call -> call.getTimestamp().isBefore(threshold)) ? 1 : 0;

        for (Deque<TimeSeriesMetric> metricDeque : metrics.values()) {
            deleted += metricDeque.removeIf(m -> m.getTimestamp().isBefore(threshold)) ? 1 : 0;
        }

        return deleted;
    }

    @Override
    public void downsample(
            TimeSeriesMetric.AggregationWindow sourceWindow,
            TimeSeriesMetric.AggregationWindow targetWindow,
            LocalDateTime start,
            LocalDateTime end) {
        // 다운샘플링 로직은 실제 시계열 DB에서 구현
        log.debug("Downsampling from {} to {} for period {} to {}",
                sourceWindow, targetWindow, start, end);
    }

    private boolean matchesDimensions(TimeSeriesMetric metric, Map<String, String> dimensions) {
        if (dimensions == null || dimensions.isEmpty()) {
            return true;
        }

        Map<String, String> metricDimensions = metric.getDimensions();
        if (metricDimensions == null) {
            return false;
        }

        return dimensions.entrySet().stream()
                .allMatch(entry -> Objects.equals(metricDimensions.get(entry.getKey()), entry.getValue()));
    }

    private <T> void trimToSize(Deque<T> deque, int maxSize) {
        while (deque.size() > maxSize) {
            deque.removeLast();
        }
    }

    public int getApiCallCount() {
        return apiCalls.size();
    }

    public int getMetricCount(String metricName) {
        Deque<TimeSeriesMetric> metricDeque = metrics.get(metricName);
        return metricDeque != null ? metricDeque.size() : 0;
    }
}
