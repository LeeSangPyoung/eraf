package com.eraf.workflow;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Workflow Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(WorkflowProperties.class)
@ConditionalOnProperty(name = "eraf.workflow.enabled", havingValue = "true")
public class WorkflowAutoConfiguration {

    /**
     * Workflow Engine Bean
     */
    @Bean
    @ConditionalOnMissingBean(WorkflowEngine.class)
    public WorkflowEngine workflowEngine(WorkflowProperties properties) {
        return new WorkflowEngine(properties);
    }
}
