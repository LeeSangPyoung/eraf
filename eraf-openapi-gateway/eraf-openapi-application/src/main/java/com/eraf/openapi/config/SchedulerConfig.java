package com.eraf.openapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler 설정
 * Health Check 등 주기적 작업을 위한 설정
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
