package com.eraf.security.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 설정 속성
 */
@ConfigurationProperties(prefix = "eraf.security.cors")
public class CorsProperties {

    /**
     * CORS 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 허용할 Origin 목록 (빈 목록이면 모든 Origin 허용)
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Origin 패턴 (정규식 지원)
     * 운영 환경에서는 반드시 명시적으로 설정하세요.
     */
    private List<String> allowedOriginPatterns = new ArrayList<>();

    /**
     * 허용할 HTTP 메서드
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /**
     * 허용할 헤더
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 노출할 헤더
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * 자격 증명 허용 여부
     * true로 설정 시 allowedOriginPatterns에 와일드카드(*)를 사용할 수 없습니다.
     */
    private boolean allowCredentials = false;

    /**
     * Preflight 캐시 시간 (초)
     */
    private long maxAge = 3600;

    /**
     * CORS 적용 경로 패턴
     */
    private String pathPattern = "/**";

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }
}
