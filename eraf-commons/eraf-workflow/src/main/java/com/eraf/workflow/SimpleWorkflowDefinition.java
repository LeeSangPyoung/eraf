package com.eraf.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 워크플로우 정의 (가변 객체)
 * <p>
 * README의 간단한 예제에서 사용하는 Mutable API입니다.
 * 프로덕션 환경에서는 {@link WorkflowDefinition}의 Builder 패턴을 사용하는 것을 권장합니다.
 *
 * <pre>{@code
 * SimpleWorkflowDefinition workflow = new SimpleWorkflowDefinition();
 * workflow.setName("order-workflow");
 * workflow.setDescription("주문 처리 워크플로우");
 *
 * SimpleWorkflowStep step1 = new SimpleWorkflowStep();
 * step1.setName("validate");
 * step1.setType(WorkflowStepType.TASK);
 * workflow.addStep(step1);
 *
 * // WorkflowDefinition으로 변환
 * WorkflowDefinition definition = workflow.build();
 * }</pre>
 */
public class SimpleWorkflowDefinition {

    private String name;
    private String description;
    private final List<SimpleWorkflowStep> steps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addStep(SimpleWorkflowStep step) {
        steps.add(step);
    }

    public List<SimpleWorkflowStep> getSteps() {
        return steps;
    }

    /**
     * WorkflowDefinition으로 변환
     */
    public WorkflowDefinition build() {
        WorkflowDefinition.Builder builder = WorkflowDefinition.builder(name);
        if (description != null) {
            builder.description(description);
        }
        for (SimpleWorkflowStep step : steps) {
            builder.step(step.build());
        }
        return builder.build();
    }
}
