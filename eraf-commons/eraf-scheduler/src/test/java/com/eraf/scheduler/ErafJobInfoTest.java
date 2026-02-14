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
        jobInfo.setName("testJob");
        jobInfo.setGroup("default");
        jobInfo.setCron("0 0 * * * ?");
        jobInfo.setDescription("Test Job");
        jobInfo.setStatus(ErafJobInfo.JobStatus.SCHEDULED);

        // Then
        assertEquals("testJob", jobInfo.getName());
        assertEquals("default", jobInfo.getGroup());
        assertEquals("0 0 * * * ?", jobInfo.getCron());
        assertEquals("Test Job", jobInfo.getDescription());
        assertEquals(ErafJobInfo.JobStatus.SCHEDULED, jobInfo.getStatus());
    }

    @Test
    @DisplayName("마지막 실행 시간 및 결과 기록")
    void testLastExecution() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();
        Instant now = Instant.now();

        // When
        jobInfo.setLastExecutionTime(now);
        jobInfo.setLastExecutionResult("SUCCESS");

        // Then
        assertEquals(now, jobInfo.getLastExecutionTime());
        assertEquals("SUCCESS", jobInfo.getLastExecutionResult());
    }

    @Test
    @DisplayName("Lock 설정")
    void testLockSettings() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();

        // When
        jobInfo.setLockEnabled(true);
        jobInfo.setLockAtMostFor("PT5M");
        jobInfo.setLockAtLeastFor("PT1M");

        // Then
        assertTrue(jobInfo.isLockEnabled());
        assertEquals("PT5M", jobInfo.getLockAtMostFor());
        assertEquals("PT1M", jobInfo.getLockAtLeastFor());
    }

    @Test
    @DisplayName("Fixed Delay/Rate 설정")
    void testFixedDelayAndRate() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();

        // When
        jobInfo.setFixedDelay(5000L);
        jobInfo.setFixedRate(10000L);

        // Then
        assertEquals(5000L, jobInfo.getFixedDelay());
        assertEquals(10000L, jobInfo.getFixedRate());
    }

    @Test
    @DisplayName("다음 실행 시간 설정")
    void testNextExecutionTime() {
        // Given
        ErafJobInfo jobInfo = new ErafJobInfo();
        Instant future = Instant.now().plusSeconds(3600);

        // When
        jobInfo.setNextExecutionTime(future);

        // Then
        assertEquals(future, jobInfo.getNextExecutionTime());
    }
}
