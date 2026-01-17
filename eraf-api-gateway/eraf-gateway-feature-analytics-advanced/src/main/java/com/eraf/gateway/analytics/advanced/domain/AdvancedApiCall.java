package com.eraf.gateway.analytics.advanced.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 고급 API 호출 기록
 * 기존 ApiCallRecord를 확장하여 더 세부적인 메트릭을 수집
 */
@Getter
@Builder
public class AdvancedApiCall {

    private final String id;
    private final String path;
    private final String method;
    private final String clientIp;
    private final String apiKey;
    private final int statusCode;

    // Latency breakdown
    private final long totalLatencyMs;          // 전체 응답 시간
    private final long upstreamLatencyMs;       // 업스트림 서버 응답 시간
    private final long gatewayLatencyMs;        // 게이트웨이 처리 시간 (total - upstream)

    // Size metrics
    private final long requestSize;
    private final long responseSize;

    // Cache metrics
    private final boolean cacheHit;
    private final String cacheKey;

    // Authentication
    private final String authMethod;            // API_KEY, JWT, BASIC, NONE
    private final String consumerIdentifier;    // API Key name or JWT subject

    // Custom dimensions
    private final String region;                // us-east-1, ap-northeast-2, etc.
    private final String clientType;            // mobile, web, api, internal
    private final String version;               // API version (v1, v2, etc.)
    private final Map<String, String> customDimensions;

    // Standard fields
    private final String userAgent;
    private final String traceId;
    private final LocalDateTime timestamp;
    private final String errorCode;
    private final String errorMessage;
}
