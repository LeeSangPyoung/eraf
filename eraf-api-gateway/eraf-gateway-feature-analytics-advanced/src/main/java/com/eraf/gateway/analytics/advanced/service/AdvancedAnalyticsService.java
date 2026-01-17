package com.eraf.gateway.analytics.advanced.service;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.domain.TimeSeriesMetric;
import com.eraf.gateway.analytics.advanced.metrics.*;
import com.eraf.gateway.analytics.advanced.repository.TimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 고급 분석 서비스
 * 메트릭 수집, 집계, 계산
 */
@Slf4j
@RequiredArgsConstructor
public class AdvancedAnalyticsService {

    private final TimeSeriesRepository repository;
    private final ExecutorService executorService;
    private final List<AdvancedApiCall> batchBuffer = Collections.synchronizedList(new ArrayList<>());
    private final int batchSize;

    public AdvancedAnalyticsService(TimeSeriesRepository repository, int batchSize, int asyncThreads) {
        this.repository = repository;
        this.batchSize = batchSize;
        this.executorService = Executors.newFixedThreadPool(asyncThreads);
    }

    /**
     * API 호출을 차원과 함께 기록 (비동기)
     */
    public CompletableFuture<Void> recordWithDimensionsAsync(AdvancedApiCall apiCall) {
        return CompletableFuture.runAsync(() -> {
            synchronized (batchBuffer) {
                batchBuffer.add(apiCall);
                if (batchBuffer.size() >= batchSize) {
                    flushBatch();
                }
            }
        }, executorService);
    }

    /**
     * API 호출 기록 (동기)
     */
    public void recordWithDimensions(AdvancedApiCall apiCall) {
        repository.save(apiCall);
    }

    /**
     * 배치 플러시
     */
    public void flushBatch() {
        List<AdvancedApiCall> toSave;
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) {
                return;
            }
            toSave = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
        }
        repository.saveAll(toSave);
        log.debug("Flushed {} API calls to repository", toSave.size());
    }

    /**
     * 백분위수 계산
     */
    public LatencyPercentiles calculatePercentiles(String path, LocalDateTime start, LocalDateTime end) {
        List<Long> latencies = repository.findLatencies(path, start, end);
        if (latencies.isEmpty()) {
            return LatencyPercentiles.builder()
                    .path(path)
                    .sampleCount(0)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        List<Long> sorted = latencies.stream().sorted().collect(Collectors.toList());
        int size = sorted.size();

        return LatencyPercentiles.builder()
                .path(path)
                .p50(percentile(sorted, 0.50))
                .p75(percentile(sorted, 0.75))
                .p95(percentile(sorted, 0.95))
                .p99(percentile(sorted, 0.99))
                .p999(percentile(sorted, 0.999))
                .min(sorted.get(0))
                .max(sorted.get(size - 1))
                .mean(sorted.stream().mapToLong(Long::longValue).average().orElse(0))
                .sampleCount(size)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 에러율 메트릭 계산
     */
    public ErrorRateMetrics calculateErrorRate(LocalDateTime start, LocalDateTime end) {
        List<AdvancedApiCall> calls = repository.findByTimeRange(start, end);

        long total = calls.size();
        long success = calls.stream().filter(c -> c.getStatusCode() >= 200 && c.getStatusCode() < 300).count();
        long clientError = calls.stream().filter(c -> c.getStatusCode() >= 400 && c.getStatusCode() < 500).count();
        long serverError = calls.stream().filter(c -> c.getStatusCode() >= 500).count();

        Map<Integer, Long> statusDist = calls.stream()
                .collect(Collectors.groupingBy(AdvancedApiCall::getStatusCode, Collectors.counting()));

        Map<String, Long> errorsByPath = calls.stream()
                .filter(c -> c.getStatusCode() >= 400)
                .collect(Collectors.groupingBy(AdvancedApiCall::getPath, Collectors.counting()));

        Map<String, Long> errorCodeDist = calls.stream()
                .filter(c -> c.getErrorCode() != null)
                .collect(Collectors.groupingBy(AdvancedApiCall::getErrorCode, Collectors.counting()));

        return ErrorRateMetrics.builder()
                .totalRequests(total)
                .successCount(success)
                .clientErrorCount(clientError)
                .serverErrorCount(serverError)
                .errorRate(ErrorRateMetrics.calculateErrorRate(clientError + serverError, total))
                .clientErrorRate(ErrorRateMetrics.calculateErrorRate(clientError, total))
                .serverErrorRate(ErrorRateMetrics.calculateErrorRate(serverError, total))
                .successRate(ErrorRateMetrics.calculateErrorRate(success, total))
                .statusCodeDistribution(statusDist)
                .errorsByEndpoint(errorsByPath)
                .errorCodeDistribution(errorCodeDist)
                .timestamp(System.currentTimeMillis())
                .windowSeconds(java.time.Duration.between(start, end).getSeconds())
                .build();
    }

    /**
     * 처리량 메트릭 계산
     */
    public ThroughputMetrics calculateThroughput(LocalDateTime start, LocalDateTime end) {
        List<AdvancedApiCall> calls = repository.findByTimeRange(start, end);
        long durationSeconds = java.time.Duration.between(start, end).getSeconds();

        long totalRequests = calls.size();
        long totalBytesIn = calls.stream().mapToLong(AdvancedApiCall::getRequestSize).sum();
        long totalBytesOut = calls.stream().mapToLong(AdvancedApiCall::getResponseSize).sum();

        Map<String, Double> throughputByPath = calls.stream()
                .collect(Collectors.groupingBy(
                        AdvancedApiCall::getPath,
                        Collectors.collectingAndThen(Collectors.counting(), count -> count / (double) durationSeconds)
                ));

        return ThroughputMetrics.builder()
                .requestsPerSecond(ThroughputMetrics.calculateRPS(totalRequests, durationSeconds))
                .requestsPerMinute(ThroughputMetrics.calculateRPS(totalRequests, durationSeconds) * 60)
                .totalRequests(totalRequests)
                .bytesPerSecond((totalBytesIn + totalBytesOut) / (double) durationSeconds)
                .megabytesPerSecond(ThroughputMetrics.calculateMBps(totalBytesIn + totalBytesOut, durationSeconds))
                .totalBytesIn(totalBytesIn)
                .totalBytesOut(totalBytesOut)
                .throughputByEndpoint(throughputByPath)
                .timestamp(System.currentTimeMillis())
                .windowSeconds(durationSeconds)
                .build();
    }

    /**
     * Top N 메트릭 계산
     */
    public TopNMetrics calculateTopN(LocalDateTime start, LocalDateTime end, int limit) {
        List<AdvancedApiCall> calls = repository.findByTimeRange(start, end);

        // Top consumers
        Map<String, List<AdvancedApiCall>> byConsumer = calls.stream()
                .filter(c -> c.getConsumerIdentifier() != null)
                .collect(Collectors.groupingBy(AdvancedApiCall::getConsumerIdentifier));

        List<TopNMetrics.ConsumerMetric> topConsumers = byConsumer.entrySet().stream()
                .map(entry -> {
                    List<AdvancedApiCall> consumerCalls = entry.getValue();
                    long errorCount = consumerCalls.stream().filter(c -> c.getStatusCode() >= 400).count();
                    return TopNMetrics.ConsumerMetric.builder()
                            .consumerIdentifier(entry.getKey())
                            .requestCount(consumerCalls.size())
                            .errorCount(errorCount)
                            .errorRate(ErrorRateMetrics.calculateErrorRate(errorCount, consumerCalls.size()))
                            .avgLatencyMs(consumerCalls.stream().mapToLong(AdvancedApiCall::getTotalLatencyMs).average().orElse(0))
                            .totalBytesTransferred(consumerCalls.stream()
                                    .mapToLong(c -> c.getRequestSize() + c.getResponseSize()).sum())
                            .build();
                })
                .sorted(Comparator.comparingLong(TopNMetrics.ConsumerMetric::getRequestCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Top APIs
        Map<String, List<AdvancedApiCall>> byPath = calls.stream()
                .collect(Collectors.groupingBy(c -> c.getPath() + " " + c.getMethod()));

        List<TopNMetrics.ApiMetric> topApis = byPath.entrySet().stream()
                .map(entry -> {
                    List<AdvancedApiCall> pathCalls = entry.getValue();
                    long errorCount = pathCalls.stream().filter(c -> c.getStatusCode() >= 400).count();
                    List<Long> latencies = pathCalls.stream().map(AdvancedApiCall::getTotalLatencyMs)
                            .sorted().collect(Collectors.toList());

                    return TopNMetrics.ApiMetric.builder()
                            .path(pathCalls.get(0).getPath())
                            .method(pathCalls.get(0).getMethod())
                            .requestCount(pathCalls.size())
                            .avgLatencyMs(latencies.stream().mapToLong(Long::longValue).average().orElse(0))
                            .p95LatencyMs(percentile(latencies, 0.95))
                            .errorRate(ErrorRateMetrics.calculateErrorRate(errorCount, pathCalls.size()))
                            .build();
                })
                .sorted(Comparator.comparingLong(TopNMetrics.ApiMetric::getRequestCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Top errors
        Map<String, List<AdvancedApiCall>> byErrorCode = calls.stream()
                .filter(c -> c.getErrorCode() != null)
                .collect(Collectors.groupingBy(AdvancedApiCall::getErrorCode));

        List<TopNMetrics.ErrorMetric> topErrors = byErrorCode.entrySet().stream()
                .map(entry -> {
                    List<AdvancedApiCall> errorCalls = entry.getValue();
                    return TopNMetrics.ErrorMetric.builder()
                            .errorCode(entry.getKey())
                            .errorMessage(errorCalls.get(0).getErrorMessage())
                            .statusCode(errorCalls.get(0).getStatusCode())
                            .occurrenceCount(errorCalls.size())
                            .topAffectedPath(errorCalls.stream()
                                    .collect(Collectors.groupingBy(AdvancedApiCall::getPath, Collectors.counting()))
                                    .entrySet().stream().max(Map.Entry.comparingByValue())
                                    .map(Map.Entry::getKey).orElse(""))
                            .firstOccurrence(errorCalls.stream()
                                    .min(Comparator.comparing(AdvancedApiCall::getTimestamp))
                                    .map(c -> c.getTimestamp().toString()).orElse(""))
                            .lastOccurrence(errorCalls.stream()
                                    .max(Comparator.comparing(AdvancedApiCall::getTimestamp))
                                    .map(c -> c.getTimestamp().toString()).orElse(""))
                            .build();
                })
                .sorted(Comparator.comparingLong(TopNMetrics.ErrorMetric::getOccurrenceCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Slowest endpoints
        List<TopNMetrics.SlowEndpointMetric> slowestEndpoints = byPath.entrySet().stream()
                .map(entry -> {
                    List<AdvancedApiCall> pathCalls = entry.getValue();
                    List<Long> latencies = pathCalls.stream().map(AdvancedApiCall::getTotalLatencyMs)
                            .sorted().collect(Collectors.toList());

                    return TopNMetrics.SlowEndpointMetric.builder()
                            .path(pathCalls.get(0).getPath())
                            .method(pathCalls.get(0).getMethod())
                            .avgLatencyMs(latencies.stream().mapToLong(Long::longValue).average().orElse(0))
                            .p95LatencyMs(percentile(latencies, 0.95))
                            .p99LatencyMs(percentile(latencies, 0.99))
                            .requestCount(pathCalls.size())
                            .slowRequestCount(pathCalls.stream().filter(c -> c.getTotalLatencyMs() > 1000).count())
                            .build();
                })
                .sorted(Comparator.comparingDouble(TopNMetrics.SlowEndpointMetric::getP95LatencyMs).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return TopNMetrics.builder()
                .topConsumers(topConsumers)
                .topApis(topApis)
                .topErrors(topErrors)
                .slowestEndpoints(slowestEndpoints)
                .timestamp(System.currentTimeMillis())
                .windowSeconds(java.time.Duration.between(start, end).getSeconds())
                .build();
    }

    /**
     * 시계열 집계
     */
    public void aggregateTimeSeries(TimeSeriesMetric.AggregationWindow window, LocalDateTime start, LocalDateTime end) {
        // 다양한 메트릭을 집계하여 시계열로 저장
        List<TimeSeriesMetric> metrics = new ArrayList<>();

        // Latency 메트릭
        Map<String, List<AdvancedApiCall>> byPath = repository.findByTimeRange(start, end).stream()
                .collect(Collectors.groupingBy(AdvancedApiCall::getPath));

        byPath.forEach((path, calls) -> {
            List<Long> latencies = calls.stream().map(AdvancedApiCall::getTotalLatencyMs)
                    .sorted().collect(Collectors.toList());

            if (!latencies.isEmpty()) {
                metrics.add(TimeSeriesMetric.builder()
                        .metricName("latency_p95")
                        .value(percentile(latencies, 0.95))
                        .unit("ms")
                        .timestamp(end)
                        .window(window)
                        .path(path)
                        .build());
            }
        });

        repository.saveMetrics(metrics);
    }

    /**
     * 백분위수 계산 헬퍼
     */
    private double percentile(List<Long> sorted, double percentile) {
        if (sorted.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    /**
     * 서비스 종료 시 정리
     */
    public void shutdown() {
        flushBatch();
        executorService.shutdown();
    }
}
