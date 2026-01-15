package com.eraf.starter.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ERAF Batch Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(JobLauncher.class)
@EnableConfigurationProperties(ErafBatchProperties.class)
@EnableBatchProcessing
public class ErafBatchAutoConfiguration {

}
