package com.eraf.gateway.validation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation Properties
 * 검증 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.validation")
public class ValidationProperties {

    /**
     * 검증 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 최대 요청 바디 크기 (bytes)
     * 기본값: 10MB
     */
    private long maxBodySize = 10 * 1024 * 1024;

    /**
     * 헤더 검증 활성화
     */
    private boolean validateHeaders = true;

    /**
     * 쿼리 파라미터 검증 활성화
     */
    private boolean validateQueryParams = true;

    /**
     * 바디 검증 활성화
     */
    private boolean validateBody = true;

    /**
     * Strict 모드 (알 수 없는 필드에 대해 실패)
     */
    private boolean strictMode = false;

    /**
     * 제외할 경로 패턴 목록
     * 예: ["/health", "/actuator/**"]
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * OpenAPI 스펙 URL 또는 파일 경로
     */
    private String openApiSpec;

    /**
     * JSON Schema 기본 버전
     * 예: "draft-07", "draft-2019-09"
     */
    private String jsonSchemaVersion = "draft-07";

    /**
     * 캐시 설정
     */
    private CacheProperties cache = new CacheProperties();

    /**
     * 캐시 설정
     */
    @Data
    public static class CacheProperties {

        /**
         * 스키마 캐시 활성화
         */
        private boolean enabled = true;

        /**
         * 캐시 최대 크기
         */
        private int maxSize = 100;

        /**
         * 캐시 TTL (초)
         */
        private long ttl = 3600;
    }
}
