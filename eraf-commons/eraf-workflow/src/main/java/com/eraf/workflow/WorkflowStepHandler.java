package com.eraf.workflow;

/**
 * Workflow Step 실행 핸들러 인터페이스
 * <p>
 * 각 Step의 실행 로직을 구현합니다.
 * Step 이름을 키로 WorkflowEngine에 등록하여 사용합니다.
 *
 * <pre>{@code
 * public class ApprovalHandler implements WorkflowStepHandler {
 *     @Override
 *     public WorkflowStepResult execute(WorkflowContext context) {
 *         String requestId = context.getString("requestId");
 *         // 승인 로직 처리...
 *         return WorkflowStepResult.completed();
 *     }
 * }
 * }</pre>
 */
@FunctionalInterface
public interface WorkflowStepHandler {

    /**
     * Step 실행
     *
     * @param context 워크플로우 컨텍스트 (변수, 인스턴스 정보 등)
     * @return Step 실행 결과
     */
    WorkflowStepResult execute(WorkflowContext context);
}
