package com.eraf.starter.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ERAF 배치 스텝 리스너
 * 스텝 실행 시작/종료 시 로깅 및 메트릭 수집
 */
public class ErafStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ErafStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        log.info("[STEP] Starting: {}", stepName);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();

        Duration duration = Duration.ZERO;
        LocalDateTime startTime = stepExecution.getStartTime();
        LocalDateTime endTime = stepExecution.getEndTime();
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime);
        }

        log.info("[STEP] Completed: {} | Status: {} | Read: {} | Written: {} | Skipped: {} | Duration: {}ms",
                stepName,
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                duration.toMillis());

        if (!stepExecution.getFailureExceptions().isEmpty()) {
            log.error("[STEP] Failures in {}:", stepName);
            stepExecution.getFailureExceptions().forEach(e ->
                log.error("  - {}: {}", e.getClass().getSimpleName(), e.getMessage()));
        }

        return stepExecution.getExitStatus();
    }
}
