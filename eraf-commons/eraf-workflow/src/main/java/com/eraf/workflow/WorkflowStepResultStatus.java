package com.eraf.workflow;

/**
 * Workflow Step 실행 결과 상태
 */
public enum WorkflowStepResultStatus {

    /** Step 정상 완료 */
    COMPLETED,

    /** Step 실패 */
    FAILED,

    /** 외부 입력 대기 (승인 등) */
    WAITING
}
