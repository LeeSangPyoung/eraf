package com.eraf.workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow 내 개별 Step 정의
 * <p>
 * 각 Step은 유형(TASK, APPROVAL, CONDITIONAL, PARALLEL)에 따라 다르게 동작합니다.
 * <ul>
 *   <li>TASK - 핸들러를 통해 자동 실행</li>
 *   <li>APPROVAL - 외부 승인을 대기하며 Workflow를 SUSPENDED 상태로 전환</li>
 *   <li>CONDITIONAL - 핸들러 결과의 nextStep에 따라 분기</li>
 *   <li>PARALLEL - 여러 핸들러를 병렬 실행 (향후 확장)</li>
 * </ul>
 */
public class WorkflowStep {

    private final String name;
    private final WorkflowStepType type;
    private final Class<? extends WorkflowStepHandler> handlerClass;
    private final Duration timeout;
    private final boolean retryable;
    private final int maxRetries;
    private final List<String> nextSteps;

    private WorkflowStep(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.handlerClass = builder.handlerClass;
        this.timeout = builder.timeout;
        this.retryable = builder.retryable;
        this.maxRetries = builder.maxRetries;
        this.nextSteps = builder.nextSteps;
    }

    public String getName() {
        return name;
    }

    public WorkflowStepType getType() {
        return type;
    }

    public Class<? extends WorkflowStepHandler> getHandlerClass() {
        return handlerClass;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    /**
     * Builder 생성
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public String toString() {
        return "WorkflowStep{name='" + name + "', type=" + type + "}";
    }

    /**
     * WorkflowStep Builder
     */
    public static class Builder {

        private final String name;
        private WorkflowStepType type = WorkflowStepType.TASK;
        private Class<? extends WorkflowStepHandler> handlerClass;
        private Duration timeout = Duration.ofMinutes(5);
        private boolean retryable = false;
        private int maxRetries = 0;
        private List<String> nextSteps = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder type(WorkflowStepType type) {
            this.type = type;
            return this;
        }

        public Builder handler(Class<? extends WorkflowStepHandler> handlerClass) {
            this.handlerClass = handlerClass;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder retryable(boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            this.retryable = maxRetries > 0;
            return this;
        }

        public Builder nextStep(String stepName) {
            this.nextSteps.add(stepName);
            return this;
        }

        public Builder nextSteps(List<String> stepNames) {
            this.nextSteps.addAll(stepNames);
            return this;
        }

        public WorkflowStep build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Step name must not be blank");
            }
            return new WorkflowStep(this);
        }
    }
}
