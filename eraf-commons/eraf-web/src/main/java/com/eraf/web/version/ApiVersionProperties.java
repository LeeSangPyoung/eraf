package com.eraf.web.version;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 버전 관리 설정
 */
@ConfigurationProperties(prefix = "eraf.web.api-version")
public class ApiVersionProperties {

    /**
     * API 버전 관리 활성화
     */
    private boolean enabled = false;

    /**
     * 기본 버전 전략
     */
    private VersionStrategy defaultStrategy = VersionStrategy.URI;

    /**
     * 기본 버전 (버전이 명시되지 않은 경우)
     */
    private String defaultVersion = "1";

    /**
     * Deprecated API 경고 헤더 추가
     */
    private boolean addDeprecationWarning = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public VersionStrategy getDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(VersionStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public boolean isAddDeprecationWarning() {
        return addDeprecationWarning;
    }

    public void setAddDeprecationWarning(boolean addDeprecationWarning) {
        this.addDeprecationWarning = addDeprecationWarning;
    }
}
