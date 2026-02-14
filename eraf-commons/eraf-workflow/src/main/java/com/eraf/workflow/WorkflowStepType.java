package com.eraf.workflow;

/**
 * Workflow Step 유형
 */
public enum WorkflowStepType {

    /** 일반 작업 - 자동으로 실행 */
    TASK,

    /** 승인 단계 - 외부 승인이 필요하여 SUSPENDED 상태로 대기 */
    APPROVAL,

    /** 조건 분기 - 결과에 따라 다음 Step이 결정됨 */
    CONDITIONAL,

    /** 병렬 실행 - 여러 Step을 동시에 실행 */
    PARALLEL
}
