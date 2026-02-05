package com.eraf.saga.execution;

import com.eraf.saga.core.StepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepExecutionTest {

    private StepExecution step;

    @BeforeEach
    void setUp() {
        step = new StepExecution(1, "testStep");
    }

    @Test
    @DisplayName("기본 생성자")
    void testDefaultConstructor() {
        // Given
        StepExecution defaultStep = new StepExecution();

        // Then
        assertEquals(StepStatus.PENDING, defaultStep.getStatus());
        assertEquals(0, defaultStep.getRetryCount());
        assertNull(defaultStep.getStartedAt());
        assertNull(defaultStep.getCompletedAt());
    }

    @Test
    @DisplayName("순서와 이름으로 생성")
    void testConstructorWithOrderAndName() {
        // Then
        assertEquals(1, step.getOrder());
        assertEquals("testStep", step.getName());
        assertEquals(StepStatus.PENDING, step.getStatus());
    }

    @Test
    @DisplayName("실행 중 상태 전환")
    void testMarkRunning() {
        // When
        step.markRunning();

        // Then
        assertEquals(StepStatus.RUNNING, step.getStatus());
        assertNotNull(step.getStartedAt());
    }

    @Test
    @DisplayName("성공 상태 전환")
    void testMarkSuccess() {
        // Given
        step.markRunning();

        // When
        step.markSuccess("result-output");

        // Then
        assertEquals(StepStatus.SUCCESS, step.getStatus());
        assertEquals("result-output", step.getOutput());
        assertNotNull(step.getCompletedAt());
    }

    @Test
    @DisplayName("실패 상태 전환")
    void testMarkFailed() {
        // Given
        step.markRunning();

        // When
        step.markFailed("Error occurred");

        // Then
        assertEquals(StepStatus.FAILED, step.getStatus());
        assertEquals("Error occurred", step.getErrorMessage());
        assertNotNull(step.getCompletedAt());
    }

    @Test
    @DisplayName("보상 중 상태 전환")
    void testMarkCompensating() {
        // When
        step.markCompensating();

        // Then
        assertEquals(StepStatus.COMPENSATING, step.getStatus());
    }

    @Test
    @DisplayName("보상 완료 상태 전환")
    void testMarkCompensated() {
        // Given
        step.markCompensating();

        // When
        step.markCompensated();

        // Then
        assertEquals(StepStatus.COMPENSATED, step.getStatus());
        assertNotNull(step.getCompletedAt());
    }

    @Test
    @DisplayName("건너뛰기 상태 전환")
    void testMarkSkipped() {
        // When
        step.markSkipped();

        // Then
        assertEquals(StepStatus.SKIPPED, step.getStatus());
    }

    @Test
    @DisplayName("완료 여부 확인 - SUCCESS")
    void testIsCompletedSuccess() {
        // Given
        step.markSuccess("done");

        // Then
        assertTrue(step.isCompleted());
    }

    @Test
    @DisplayName("완료 여부 확인 - COMPENSATED")
    void testIsCompletedCompensated() {
        // Given
        step.markCompensated();

        // Then
        assertTrue(step.isCompleted());
    }

    @Test
    @DisplayName("완료 여부 확인 - SKIPPED")
    void testIsCompletedSkipped() {
        // Given
        step.markSkipped();

        // Then
        assertTrue(step.isCompleted());
    }

    @Test
    @DisplayName("완료 여부 확인 - RUNNING (미완료)")
    void testIsCompletedRunning() {
        // Given
        step.markRunning();

        // Then
        assertFalse(step.isCompleted());
    }

    @Test
    @DisplayName("완료 여부 확인 - FAILED (미완료)")
    void testIsCompletedFailed() {
        // Given
        step.markFailed("error");

        // Then
        assertFalse(step.isCompleted());
    }

    @Test
    @DisplayName("재시도 횟수 증가")
    void testIncrementRetryCount() {
        // Then
        assertEquals(0, step.getRetryCount());

        step.incrementRetryCount();
        assertEquals(1, step.getRetryCount());

        step.incrementRetryCount();
        assertEquals(2, step.getRetryCount());
    }

    @Test
    @DisplayName("Setter 동작 확인")
    void testSetters() {
        // When
        step.setOrder(5);
        step.setName("newName");
        step.setStatus(StepStatus.RUNNING);
        step.setOutput("output");
        step.setErrorMessage("error");
        step.setRetryCount(3);

        // Then
        assertEquals(5, step.getOrder());
        assertEquals("newName", step.getName());
        assertEquals(StepStatus.RUNNING, step.getStatus());
        assertEquals("output", step.getOutput());
        assertEquals("error", step.getErrorMessage());
        assertEquals(3, step.getRetryCount());
    }

    @Test
    @DisplayName("상태 전환 타임스탬프 기록")
    void testTimestampRecording() throws InterruptedException {
        // When
        step.markRunning();
        var startTime = step.getStartedAt();

        Thread.sleep(10); // 약간의 시간 경과

        step.markSuccess("done");
        var endTime = step.getCompletedAt();

        // Then
        assertNotNull(startTime);
        assertNotNull(endTime);
        assertTrue(endTime.isAfter(startTime));
    }
}
