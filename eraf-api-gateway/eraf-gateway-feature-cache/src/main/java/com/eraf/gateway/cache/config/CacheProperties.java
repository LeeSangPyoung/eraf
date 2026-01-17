package com.eraf.gateway.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.cache")
public class CacheProperties {

    /**
     * Cache 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 기본 TTL (초)
     */
    private int defaultTtlSeconds = 300;

    /**
     * 쿼리 파라미터 기준 캐시 구분 여부
     */
    private boolean varyByQueryParams = true;

    /**
     * 헤더 기준 캐시 구분 여부
     */
    private boolean varyByHeaders = false;

    /**
     * 캐시 키에 포함할 헤더 목록
     */
    private List<String> varyHeaders = new ArrayList<>();

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * 만료 캐시 정리 주기 (초)
     */
    private int cleanupIntervalSeconds = 60;

    /**
     * 최대 캐시 크기 (엔트리 수)
     */
    private int maxCacheSize = 1000;
}
