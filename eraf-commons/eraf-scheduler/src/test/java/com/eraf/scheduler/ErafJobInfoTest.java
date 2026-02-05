package com.eraf.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ErafJobInfoTest {

    @Test
    @DisplayName("Job 정보 생성")
    void testCreateJobInfo() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();

        // When
        jobInfo.setJobName("testJob");
        jobInfo.setCronExpression("0 0 * * * ?");
        jobInfo.setDescription("Test Job");
        jobInfo.setEnabled(true);

        // Then
        assertEquals("testJob", jobInfo.getJobName());
        assertEquals("0 0 * * * ?", jobInfo.getCronExpression());
        assertTrue(jobInfo.isEnabled());
    }

    @Test
    @DisplayName("마지막 실행 시간 기록")
    void testLastExecution() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();
        Instant now = Instant.now();

        // When
        jobInfo.setLastExecutionTime(now);
        jobInfo.setLastExecutionStatus("SUCCESS");

        // Then
        assertEquals(now, jobInfo.getLastExecutionTime());
        assertEquals("SUCCESS", jobInfo.getLastExecutionStatus());
    }
}
