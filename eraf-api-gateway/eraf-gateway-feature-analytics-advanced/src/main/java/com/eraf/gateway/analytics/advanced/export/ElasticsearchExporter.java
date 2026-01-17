package com.eraf.gateway.analytics.advanced.export;

import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.domain.TimeSeriesMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 익스포터 (선택사항)
 * API 호출 데이터를 Elasticsearch로 전송하여 Kibana로 시각화
 *
 * 실제 프로덕션에서는 elasticsearch-rest-high-level-client를 사용
 * 여기서는 인터페이스만 제공
 */
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchExporter {

    private final String host;
    private final int port;
    private final String indexPrefix;

    /**
     * API 호출을 Elasticsearch 문서로 변환
     */
    public Map<String, Object> toDocument(AdvancedApiCall apiCall) {
        Map<String, Object> doc = new HashMap<>();

        doc.put("@timestamp", apiCall.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        doc.put("path", apiCall.getPath());
        doc.put("method", apiCall.getMethod());
        doc.put("client_ip", apiCall.getClientIp());
        doc.put("status_code", apiCall.getStatusCode());

        // Latency breakdown
        Map<String, Long> latency = new HashMap<>();
        latency.put("total_ms", apiCall.getTotalLatencyMs());
        latency.put("upstream_ms", apiCall.getUpstreamLatencyMs());
        latency.put("gateway_ms", apiCall.getGatewayLatencyMs());
        doc.put("latency", latency);

        // Size
        Map<String, Long> size = new HashMap<>();
        size.put("request_bytes", apiCall.getRequestSize());
        size.put("response_bytes", apiCall.getResponseSize());
        doc.put("size", size);

        // Cache
        Map<String, Object> cache = new HashMap<>();
        cache.put("hit", apiCall.isCacheHit());
        cache.put("key", apiCall.getCacheKey());
        doc.put("cache", cache);

        // Auth
        Map<String, String> auth = new HashMap<>();
        auth.put("method", apiCall.getAuthMethod());
        auth.put("consumer", apiCall.getConsumerIdentifier());
        doc.put("auth", auth);

        // Dimensions
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("region", apiCall.getRegion());
        dimensions.put("client_type", apiCall.getClientType());
        dimensions.put("version", apiCall.getVersion());
        if (apiCall.getCustomDimensions() != null) {
            dimensions.putAll(apiCall.getCustomDimensions());
        }
        doc.put("dimensions", dimensions);

        // Error
        if (apiCall.getErrorCode() != null) {
            Map<String, String> error = new HashMap<>();
            error.put("code", apiCall.getErrorCode());
            error.put("message", apiCall.getErrorMessage());
            doc.put("error", error);
        }

        doc.put("trace_id", apiCall.getTraceId());
        doc.put("user_agent", apiCall.getUserAgent());

        return doc;
    }

    /**
     * 시계열 메트릭을 Elasticsearch 문서로 변환
     */
    public Map<String, Object> toDocument(TimeSeriesMetric metric) {
        Map<String, Object> doc = new HashMap<>();

        doc.put("@timestamp", metric.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        doc.put("metric_name", metric.getMetricName());
        doc.put("value", metric.getValue());
        doc.put("unit", metric.getUnit());
        doc.put("window", metric.getWindow().getLabel());

        if (metric.getPath() != null) {
            doc.put("path", metric.getPath());
        }
        if (metric.getMethod() != null) {
            doc.put("method", metric.getMethod());
        }
        if (metric.getRegion() != null) {
            doc.put("region", metric.getRegion());
        }
        if (metric.getClientType() != null) {
            doc.put("client_type", metric.getClientType());
        }

        if (metric.getDimensions() != null) {
            doc.put("dimensions", metric.getDimensions());
        }

        return doc;
    }

    /**
     * 벌크 인덱싱을 위한 Elasticsearch Bulk API 포맷
     */
    public String toBulkRequest(List<AdvancedApiCall> apiCalls) {
        StringBuilder bulk = new StringBuilder();
        String index = indexPrefix + "-api-calls";

        for (AdvancedApiCall call : apiCalls) {
            // Action line
            bulk.append("{\"index\":{\"_index\":\"").append(index).append("\"}}\n");
            // Document line
            bulk.append(toJson(toDocument(call))).append("\n");
        }

        return bulk.toString();
    }

    /**
     * Map을 JSON 문자열로 변환 (간단한 구현)
     * 실제로는 Jackson이나 Gson 사용
     */
    private String toJson(Map<String, Object> map) {
        // Simplified JSON conversion
        // In production, use Jackson ObjectMapper
        return map.toString().replace("=", ":");
    }

    /**
     * Elasticsearch에 데이터 전송 (실제 구현 필요)
     */
    public void index(AdvancedApiCall apiCall) {
        String indexName = indexPrefix + "-api-calls";
        Map<String, Object> document = toDocument(apiCall);

        log.debug("Would index to Elasticsearch [{}]: {}", indexName, document);

        // 실제 구현:
        // RestHighLevelClient client = ...
        // IndexRequest request = new IndexRequest(indexName).source(document);
        // client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 벌크 인덱싱
     */
    public void bulkIndex(List<AdvancedApiCall> apiCalls) {
        String indexName = indexPrefix + "-api-calls";

        log.debug("Would bulk index {} documents to Elasticsearch [{}]", apiCalls.size(), indexName);

        // 실제 구현:
        // BulkRequest bulkRequest = new BulkRequest();
        // apiCalls.forEach(call -> {
        //     bulkRequest.add(new IndexRequest(indexName).source(toDocument(call)));
        // });
        // client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
