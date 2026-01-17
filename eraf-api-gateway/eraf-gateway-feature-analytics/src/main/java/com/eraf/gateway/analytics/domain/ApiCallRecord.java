package com.eraf.gateway.analytics.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 호출 기록
 */
@Getter
@Builder
public class ApiCallRecord {

    private final String id;
    private final String path;
    private final String method;
    private final String clientIp;
    private final String apiKey;
    private final int statusCode;
    private final long responseTimeMs;
    private final long requestSize;
    private final long responseSize;
    private final String userAgent;
    private final String traceId;
    private final LocalDateTime timestamp;
    private final String errorCode;
    private final String errorMessage;
}
