package com.eraf.gateway.analytics.advanced.repository;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.domain.TimeSeriesMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 시계열 데이터 저장소
 * InfluxDB, TimescaleDB, Prometheus 등의 시계열 DB 구조를 지원
 */
public interface TimeSeriesRepository {

    /**
     * 고급 API 호출 기록 저장
     */
    void save(AdvancedApiCall apiCall);

    /**
     * 배치로 API 호출 기록 저장
     */
    void saveAll(List<AdvancedApiCall> apiCalls);

    /**
     * 시계열 메트릭 저장
     */
    void saveMetric(TimeSeriesMetric metric);

    /**
     * 배치로 메트릭 저장
     */
    void saveMetrics(List<TimeSeriesMetric> metrics);

    /**
     * 특정 기간의 API 호출 조회
     */
    List<AdvancedApiCall> findByTimeRange(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 경로의 API 호출 조회
     */
    List<AdvancedApiCall> findByPath(String path, LocalDateTime start, LocalDateTime end);

    /**
     * 특정 consumer의 API 호출 조회
     */
    List<AdvancedApiCall> findByConsumer(String consumerIdentifier, LocalDateTime start, LocalDateTime end);

    /**
     * 특정 메트릭 조회
     */
    List<TimeSeriesMetric> findMetrics(
            String metricName,
            LocalDateTime start,
            LocalDateTime end,
            TimeSeriesMetric.AggregationWindow window
    );

    /**
     * 특정 메트릭을 차원별로 조회
     */
    List<TimeSeriesMetric> findMetricsWithDimensions(
            String metricName,
            LocalDateTime start,
            LocalDateTime end,
            TimeSeriesMetric.AggregationWindow window,
            Map<String, String> dimensions
    );

    /**
     * 레이턴시 데이터 조회 (백분위수 계산용)
     */
    List<Long> findLatencies(String path, LocalDateTime start, LocalDateTime end);

    /**
     * 에러 발생 수 집계
     */
    Map<String, Long> countErrorsByCode(LocalDateTime start, LocalDateTime end);

    /**
     * 경로별 요청 수 집계
     */
    Map<String, Long> countRequestsByPath(LocalDateTime start, LocalDateTime end);

    /**
     * Consumer별 요청 수 집계
     */
    Map<String, Long> countRequestsByConsumer(LocalDateTime start, LocalDateTime end);

    /**
     * 오래된 데이터 삭제 (보존 정책)
     */
    long deleteOlderThan(LocalDateTime threshold);

    /**
     * 다운샘플링: 고해상도 데이터를 저해상도로 집계
     */
    void downsample(
            TimeSeriesMetric.AggregationWindow sourceWindow,
            TimeSeriesMetric.AggregationWindow targetWindow,
            LocalDateTime start,
            LocalDateTime end
    );
}
