package com.eraf.starter.actuator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Actuator Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.actuator")
public class ErafActuatorProperties {

    /**
     * Enable custom health indicators
     */
    private boolean healthEnabled = true;

    /**
     * Enable metrics collection
     */
    private boolean metricsEnabled = true;

    /**
     * Application name for metrics tagging
     */
    private String applicationName = "eraf-app";

    public boolean isHealthEnabled() {
        return healthEnabled;
    }

    public void setHealthEnabled(boolean healthEnabled) {
        this.healthEnabled = healthEnabled;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
