package com.eraf.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;

import static org.junit.jupiter.api.Assertions.*;

class ErafStepListenerTest {

    private ErafStepListener stepListener;

    @BeforeEach
    void setUp() {
        stepListener = new ErafStepListener();
    }

    @Test
    @DisplayName("Step 시작 전 호출")
    void testBeforeStep() {
        // Given
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());
        StepExecution stepExecution = new StepExecution("testStep", jobExecution);

        // When & Then
        assertDoesNotThrow(() -> stepListener.beforeStep(stepExecution));
    }

    @Test
    @DisplayName("Step 완료 후 호출")
    void testAfterStep() {
        // Given
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());
        StepExecution stepExecution = new StepExecution("testStep", jobExecution);

        // When
        ExitStatus result = stepListener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
    }
}
