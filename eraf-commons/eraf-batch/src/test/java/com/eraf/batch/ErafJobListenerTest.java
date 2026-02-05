package com.eraf.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

import static org.junit.jupiter.api.Assertions.*;

class ErafJobListenerTest {

    private ErafJobListener jobListener;

    @BeforeEach
    void setUp() {
        jobListener = new ErafJobListener();
    }

    @Test
    @DisplayName("Job 시작 전 호출")
    void testBeforeJob() {
        // Given
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());

        // When & Then - 예외 없이 실행되어야 함
        assertDoesNotThrow(() -> jobListener.beforeJob(jobExecution));
    }

    @Test
    @DisplayName("Job 완료 후 호출")
    void testAfterJob() {
        // Given
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());

        // When & Then - 예외 없이 실행되어야 함
        assertDoesNotThrow(() -> jobListener.afterJob(jobExecution));
    }
}
