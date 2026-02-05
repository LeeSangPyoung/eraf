package com.eraf.security.apikey;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * API Key 인증 설정
 */
@ConfigurationProperties(prefix = "eraf.security.api-key")
public class ApiKeyProperties {

    /**
     * API Key 인증 활성화 여부
     */
    private boolean enabled = false;

    /**
     * API Key 헤더 이름
     */
    private String headerName = "X-API-Key";

    /**
     * 쿼리 파라미터 이름 (헤더 대신 사용 가능)
     */
    private String queryParamName = "apiKey";

    /**
     * API Key 적용 URL 패턴
     */
    private String[] applyPatterns = {"/api/**"};

    /**
     * API Key 검증 건너뛸 URL 패턴
     */
    private String[] skipPatterns = {};

    /**
     * 등록된 API Key 목록
     */
    private List<ApiKeyEntry> keys = new ArrayList<>();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public void setQueryParamName(String queryParamName) {
        this.queryParamName = queryParamName;
    }

    public String[] getApplyPatterns() {
        return applyPatterns;
    }

    public void setApplyPatterns(String[] applyPatterns) {
        this.applyPatterns = applyPatterns;
    }

    public String[] getSkipPatterns() {
        return skipPatterns;
    }

    public void setSkipPatterns(String[] skipPatterns) {
        this.skipPatterns = skipPatterns;
    }

    public List<ApiKeyEntry> getKeys() {
        return keys;
    }

    public void setKeys(List<ApiKeyEntry> keys) {
        this.keys = keys;
    }

    /**
     * API Key 항목
     */
    public static class ApiKeyEntry {
        /**
         * API Key 값
         */
        private String key;

        /**
         * 클라이언트/애플리케이션 이름
         */
        private String name;

        /**
         * 부여된 역할 목록
         */
        private List<String> roles = new ArrayList<>();

        /**
         * 허용된 IP 목록 (비어있으면 모든 IP 허용)
         */
        private List<String> allowedIps = new ArrayList<>();

        /**
         * 허용된 URL 패턴 (비어있으면 모든 URL 허용)
         */
        private List<String> allowedPatterns = new ArrayList<>();

        /**
         * 활성화 여부
         */
        private boolean enabled = true;

        // Getters and Setters

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getAllowedIps() {
            return allowedIps;
        }

        public void setAllowedIps(List<String> allowedIps) {
            this.allowedIps = allowedIps;
        }

        public List<String> getAllowedPatterns() {
            return allowedPatterns;
        }

        public void setAllowedPatterns(List<String> allowedPatterns) {
            this.allowedPatterns = allowedPatterns;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
