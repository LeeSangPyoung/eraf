package com.eraf.gateway.analytics.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 호출 메트릭
 */
@Getter
@Builder
public class ApiMetrics {

    private final String path;
    private final String method;
    private final long totalRequests;
    private final long successCount;
    private final long errorCount;
    private final long avgResponseTimeMs;
    private final long minResponseTimeMs;
    private final long maxResponseTimeMs;
    private final long p50ResponseTimeMs;
    private final long p95ResponseTimeMs;
    private final long p99ResponseTimeMs;
    private final LocalDateTime lastRequestTime;
    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;
}
