package com.eraf.starter.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ERAF 배치 잡 리스너
 * 잡 실행 시작/종료 시 로깅 및 메트릭 수집
 */
public class ErafJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ErafJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        log.info("========================================");
        log.info("[BATCH] Job Started: {}", jobName);
        log.info("[BATCH] Job Execution ID: {}", jobExecution.getId());
        log.info("[BATCH] Start Time: {}", jobExecution.getStartTime());
        log.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        Duration duration = Duration.ZERO;
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime);
        }

        log.info("========================================");
        log.info("[BATCH] Job Completed: {}", jobName);
        log.info("[BATCH] Status: {}", jobExecution.getStatus());
        log.info("[BATCH] Exit Status: {}", jobExecution.getExitStatus().getExitCode());
        log.info("[BATCH] Duration: {}ms", duration.toMillis());

        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            log.error("[BATCH] Failures:");
            jobExecution.getAllFailureExceptions().forEach(e ->
                log.error("  - {}: {}", e.getClass().getSimpleName(), e.getMessage()));
        }

        log.info("========================================");
    }
}
