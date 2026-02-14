package com.eraf.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow 정의
 * <p>
 * Step들의 순서와 구성을 정의합니다. Builder 패턴으로 생성합니다.
 *
 * <pre>{@code
 * WorkflowDefinition definition = WorkflowDefinition.builder("approval-flow")
 *     .description("문서 승인 워크플로우")
 *     .step(WorkflowStep.builder("submit")
 *         .type(WorkflowStepType.TASK)
 *         .handler(SubmitHandler.class)
 *         .build())
 *     .step(WorkflowStep.builder("review")
 *         .type(WorkflowStepType.APPROVAL)
 *         .handler(ReviewHandler.class)
 *         .build())
 *     .step(WorkflowStep.builder("complete")
 *         .type(WorkflowStepType.TASK)
 *         .handler(CompleteHandler.class)
 *         .build())
 *     .build();
 * }</pre>
 */
public class WorkflowDefinition {

    private final String name;
    private final String description;
    private final List<WorkflowStep> steps;
    private final Map<String, WorkflowStep> stepMap;

    private WorkflowDefinition(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.steps = Collections.unmodifiableList(new ArrayList<>(builder.steps));
        Map<String, WorkflowStep> map = new LinkedHashMap<>();
        for (WorkflowStep step : this.steps) {
            map.put(step.getName(), step);
        }
        this.stepMap = Collections.unmodifiableMap(map);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 모든 Step 목록 (순서 보장)
     */
    public List<WorkflowStep> getSteps() {
        return steps;
    }

    /**
     * Step 수
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * 이름으로 Step 조회
     */
    public WorkflowStep getStep(String name) {
        return stepMap.get(name);
    }

    /**
     * 첫 번째 Step 조회
     */
    public WorkflowStep getFirstStep() {
        return steps.isEmpty() ? null : steps.get(0);
    }

    /**
     * 지정된 Step의 다음 Step 조회
     * <p>
     * Step에 nextSteps가 지정되지 않은 경우 정의 순서상 다음 Step을 반환합니다.
     */
    public WorkflowStep getNextStep(String currentStepName) {
        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).getName().equals(currentStepName)) {
                return steps.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Step 존재 여부
     */
    public boolean hasStep(String name) {
        return stepMap.containsKey(name);
    }

    /**
     * Builder 생성
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public String toString() {
        return "WorkflowDefinition{name='" + name + "', steps=" + steps.size() + "}";
    }

    /**
     * WorkflowDefinition Builder
     */
    public static class Builder {

        private final String name;
        private String description;
        private final List<WorkflowStep> steps = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder step(WorkflowStep step) {
            this.steps.add(step);
            return this;
        }

        public Builder steps(List<WorkflowStep> steps) {
            this.steps.addAll(steps);
            return this;
        }

        public WorkflowDefinition build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Workflow name must not be blank");
            }
            if (steps.isEmpty()) {
                throw new IllegalArgumentException("Workflow must have at least one step");
            }
            return new WorkflowDefinition(this);
        }
    }
}
