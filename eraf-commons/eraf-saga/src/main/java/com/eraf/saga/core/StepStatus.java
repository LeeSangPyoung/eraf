package com.eraf.saga.core;

/**
 * Saga Step 실행 상태
 */
public enum StepStatus {

    /**
     * 대기 중
     */
    PENDING,

    /**
     * 실행 중
     */
    RUNNING,

    /**
     * 성공
     */
    SUCCESS,

    /**
     * 실패
     */
    FAILED,

    /**
     * 보상 중
     */
    COMPENSATING,

    /**
     * 보상 완료
     */
    COMPENSATED,

    /**
     * 건너뜀
     */
    SKIPPED
}
