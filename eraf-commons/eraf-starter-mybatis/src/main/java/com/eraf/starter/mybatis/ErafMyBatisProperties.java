package com.eraf.starter.mybatis;

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
