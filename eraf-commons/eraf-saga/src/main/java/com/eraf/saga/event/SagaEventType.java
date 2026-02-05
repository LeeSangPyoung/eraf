package com.eraf.saga.event;

/**
 * Saga 이벤트 타입
 */
public enum SagaEventType {

    /**
     * Saga 시작됨
     */
    SAGA_STARTED,

    /**
     * Saga 완료됨
     */
    SAGA_COMPLETED,

    /**
     * Saga 실패함
     */
    SAGA_FAILED,

    /**
     * Saga 보상 완료됨
     */
    SAGA_COMPENSATED,

    /**
     * Step 시작됨
     */
    STEP_STARTED,

    /**
     * Step 완료됨
     */
    STEP_COMPLETED,

    /**
     * Step 실패함
     */
    STEP_FAILED,

    /**
     * Step 보상됨
     */
    STEP_COMPENSATED
}
