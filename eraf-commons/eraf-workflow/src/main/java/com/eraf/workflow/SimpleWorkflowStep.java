package com.eraf.workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 워크플로우 Step (가변 객체)
 * <p>
 * README의 간단한 예제에서 사용하는 Mutable API입니다.
 * 프로덕션 환경에서는 {@link WorkflowStep}의 Builder 패턴을 사용하는 것을 권장합니다.
 *
 * <pre>{@code
 * SimpleWorkflowStep step = new SimpleWorkflowStep();
 * step.setName("validate");
 * step.setType(WorkflowStepType.TASK);
 * step.setRetryable(true);
 * step.setMaxRetries(3);
 *
 * // WorkflowStep으로 변환
 * WorkflowStep workflowStep = step.build();
 * }</pre>
 */
public class SimpleWorkflowStep {

    private String name;
    private WorkflowStepType type = WorkflowStepType.TASK;
    private Class<? extends WorkflowStepHandler> handlerClass;
    private Duration timeout = Duration.ofMinutes(5);
    private boolean retryable = false;
    private int maxRetries = 0;
    private final List<String> nextSteps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowStepType getType() {
        return type;
    }

    public void setType(WorkflowStepType type) {
        this.type = type;
    }

    public Class<? extends WorkflowStepHandler> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<? extends WorkflowStepHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        if (maxRetries > 0) {
            this.retryable = true;
        }
    }

    public void addNextStep(String stepName) {
        nextSteps.add(stepName);
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    /**
     * WorkflowStep으로 변환
     */
    public WorkflowStep build() {
        WorkflowStep.Builder builder = WorkflowStep.builder(name)
                .type(type)
                .timeout(timeout)
                .retryable(retryable)
                .maxRetries(maxRetries);

        if (handlerClass != null) {
            builder.handler(handlerClass);
        }

        if (!nextSteps.isEmpty()) {
            builder.nextSteps(nextSteps);
        }

        return builder.build();
    }
}
