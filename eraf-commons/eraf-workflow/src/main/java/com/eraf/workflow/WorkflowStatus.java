package com.eraf.workflow;

/**
 * Workflow 인스턴스 상태
 */
public enum WorkflowStatus {

    /** 생성됨 - 아직 실행되지 않음 */
    CREATED,

    /** 실행 중 */
    RUNNING,

    /** 정상 완료 */
    COMPLETED,

    /** 실패 */
    FAILED,

    /** 일시 중단 (승인 대기 등) */
    SUSPENDED,

    /** 취소됨 */
    CANCELLED
}
