package com.eraf.gateway.analytics;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API Analytics Repository 인터페이스
 */
public interface AnalyticsRepository {

    /**
     * API 호출 기록 저장
     */
    void save(ApiCallRecord record);

    /**
     * 경로별 메트릭 조회
     */
    ApiMetrics getMetricsByPath(String path, LocalDateTime from, LocalDateTime to);

    /**
     * 전체 메트릭 조회
     */
    List<ApiMetrics> getAllMetrics(LocalDateTime from, LocalDateTime to);

    /**
     * 상위 N개 경로 (요청 수 기준)
     */
    List<ApiMetrics> getTopPaths(int limit, LocalDateTime from, LocalDateTime to);

    /**
     * 상위 N개 경로 (에러 수 기준)
     */
    List<ApiMetrics> getTopErrorPaths(int limit, LocalDateTime from, LocalDateTime to);

    /**
     * 상위 N개 경로 (응답시간 기준)
     */
    List<ApiMetrics> getSlowestPaths(int limit, LocalDateTime from, LocalDateTime to);

    /**
     * 특정 기간 호출 기록 조회
     */
    List<ApiCallRecord> getRecords(LocalDateTime from, LocalDateTime to, int limit);

    /**
     * 오래된 기록 삭제
     */
    void deleteOlderThan(LocalDateTime threshold);
}
