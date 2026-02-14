package com.eraf.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowDefinitionTest {

    @Test
    @DisplayName("Builder로 WorkflowDefinition 생성")
    void testBuildDefinition() {
        // Given & When
        WorkflowDefinition definition = WorkflowDefinition.builder("approval-flow")
                .description("문서 승인 워크플로우")
                .step(WorkflowStep.builder("submit")
                        .type(WorkflowStepType.TASK)
                        .build())
                .step(WorkflowStep.builder("review")
                        .type(WorkflowStepType.APPROVAL)
                        .timeout(Duration.ofMinutes(30))
                        .build())
                .step(WorkflowStep.builder("complete")
                        .type(WorkflowStepType.TASK)
                        .build())
                .build();

        // Then
        assertEquals("approval-flow", definition.getName());
        assertEquals("문서 승인 워크플로우", definition.getDescription());
        assertEquals(3, definition.getStepCount());
    }

    @Test
    @DisplayName("Step 순서 보장")
    void testStepOrder() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("ordered-flow")
                .step(WorkflowStep.builder("first").build())
                .step(WorkflowStep.builder("second").build())
                .step(WorkflowStep.builder("third").build())
                .build();

        // When
        List<WorkflowStep> steps = definition.getSteps();

        // Then
        assertEquals(3, steps.size());
        assertEquals("first", steps.get(0).getName());
        assertEquals("second", steps.get(1).getName());
        assertEquals("third", steps.get(2).getName());
    }

    @Test
    @DisplayName("이름으로 Step 조회")
    void testGetStepByName() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test")
                .step(WorkflowStep.builder("step-a").type(WorkflowStepType.TASK).build())
                .step(WorkflowStep.builder("step-b").type(WorkflowStepType.APPROVAL).build())
                .build();

        // When
        WorkflowStep step = definition.getStep("step-b");

        // Then
        assertNotNull(step);
        assertEquals("step-b", step.getName());
        assertEquals(WorkflowStepType.APPROVAL, step.getType());
    }

    @Test
    @DisplayName("존재하지 않는 Step 조회 시 null 반환")
    void testGetNonExistentStep() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test")
                .step(WorkflowStep.builder("step-a").build())
                .build();

        // When
        WorkflowStep step = definition.getStep("nonexistent");

        // Then
        assertNull(step);
    }

    @Test
    @DisplayName("첫 번째 Step 조회")
    void testGetFirstStep() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test")
                .step(WorkflowStep.builder("alpha").build())
                .step(WorkflowStep.builder("beta").build())
                .build();

        // When
        WorkflowStep first = definition.getFirstStep();

        // Then
        assertNotNull(first);
        assertEquals("alpha", first.getName());
    }

    @Test
    @DisplayName("다음 Step 조회")
    void testGetNextStep() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test")
                .step(WorkflowStep.builder("step-1").build())
                .step(WorkflowStep.builder("step-2").build())
                .step(WorkflowStep.builder("step-3").build())
                .build();

        // When & Then
        WorkflowStep next = definition.getNextStep("step-1");
        assertNotNull(next);
        assertEquals("step-2", next.getName());

        next = definition.getNextStep("step-2");
        assertNotNull(next);
        assertEquals("step-3", next.getName());

        // 마지막 Step의 다음은 null
        next = definition.getNextStep("step-3");
        assertNull(next);
    }

    @Test
    @DisplayName("Step 존재 여부 확인")
    void testHasStep() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test")
                .step(WorkflowStep.builder("existing").build())
                .build();

        // Then
        assertTrue(definition.hasStep("existing"));
        assertFalse(definition.hasStep("not-existing"));
    }

    @Test
    @DisplayName("이름 없이 빌드 시 예외 발생")
    void testBuildWithoutName() {
        // Then
        assertThrows(IllegalArgumentException.class, () ->
                WorkflowDefinition.builder("").build());
    }

    @Test
    @DisplayName("Step 없이 빌드 시 예외 발생")
    void testBuildWithoutSteps() {
        // Then
        assertThrows(IllegalArgumentException.class, () ->
                WorkflowDefinition.builder("empty-flow").build());
    }

    @Test
    @DisplayName("steps() 메서드로 여러 Step 한번에 추가")
    void testBuildWithStepsList() {
        // Given
        List<WorkflowStep> steps = List.of(
                WorkflowStep.builder("a").build(),
                WorkflowStep.builder("b").build()
        );

        // When
        WorkflowDefinition definition = WorkflowDefinition.builder("bulk-add")
                .steps(steps)
                .build();

        // Then
        assertEquals(2, definition.getStepCount());
    }

    @Test
    @DisplayName("toString 확인")
    void testToString() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("my-workflow")
                .step(WorkflowStep.builder("s1").build())
                .build();

        // Then
        String str = definition.toString();
        assertTrue(str.contains("my-workflow"));
        assertTrue(str.contains("1"));
    }
}
