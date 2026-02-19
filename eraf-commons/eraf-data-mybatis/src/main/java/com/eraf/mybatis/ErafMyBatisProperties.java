package com.eraf.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF MyBatis Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.mybatis")
public class ErafMyBatisProperties {

    /**
     * Enable camel case to underscore conversion
     */
    private boolean mapUnderscoreToCamelCase = true;

    /**
     * Enable lazy loading
     */
    private boolean lazyLoadingEnabled = false;

    /**
     * Mapper locations pattern
     */
    private String mapperLocations = "classpath*:mapper/**/*.xml";

    /**
     * Type aliases package
     */
    private String typeAliasesPackage;

    /**
     * 슬로우 쿼리 감지 설정
     */
    private SlowQuery slowQuery = new SlowQuery();

    public SlowQuery getSlowQuery() {
        return slowQuery;
    }

    public void setSlowQuery(SlowQuery slowQuery) {
        this.slowQuery = slowQuery;
    }

    public static class SlowQuery {
        /** 슬로우 쿼리 감지 활성화 여부 */
        private boolean enabled = true;
        /** 슬로우 쿼리 임계값 (밀리초) */
        private long thresholdMs = 1000;
        /** 심각 슬로우 쿼리 임계값 (밀리초) */
        private long criticalThresholdMs = 3000;
        /** SQL 내용을 로그에 포함할지 여부 */
        private boolean logSql = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public long getThresholdMs() { return thresholdMs; }
        public void setThresholdMs(long thresholdMs) { this.thresholdMs = thresholdMs; }
        public long getCriticalThresholdMs() { return criticalThresholdMs; }
        public void setCriticalThresholdMs(long criticalThresholdMs) { this.criticalThresholdMs = criticalThresholdMs; }
        public boolean isLogSql() { return logSql; }
        public void setLogSql(boolean logSql) { this.logSql = logSql; }
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public boolean isLazyLoadingEnabled() {
        return lazyLoadingEnabled;
    }

    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        this.lazyLoadingEnabled = lazyLoadingEnabled;
    }

    public String getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }
}
