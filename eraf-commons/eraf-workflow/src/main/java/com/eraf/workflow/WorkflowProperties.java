package com.eraf.workflow;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Workflow Configuration Properties
 *
 * <pre>
 * eraf:
 *   workflow:
 *     enabled: true
 *     max-concurrent-workflows: 100
 *     step-timeout: 5m
 * </pre>
 */
@ConfigurationProperties(prefix = "eraf.workflow")
public class WorkflowProperties {

    /**
     * Workflow 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 최대 동시 실행 워크플로우 수 (0 = 제한 없음)
     */
    private int maxConcurrentWorkflows = 0;

    /**
     * Step 기본 타임아웃
     */
    private Duration stepTimeout = Duration.ofMinutes(5);

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxConcurrentWorkflows() {
        return maxConcurrentWorkflows;
    }

    public void setMaxConcurrentWorkflows(int maxConcurrentWorkflows) {
        this.maxConcurrentWorkflows = maxConcurrentWorkflows;
    }

    public Duration getStepTimeout() {
        return stepTimeout;
    }

    public void setStepTimeout(Duration stepTimeout) {
        this.stepTimeout = stepTimeout;
    }
}
