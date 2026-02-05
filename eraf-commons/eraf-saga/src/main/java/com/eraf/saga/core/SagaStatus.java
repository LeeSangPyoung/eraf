package com.eraf.saga.core;

/**
 * Saga 실행 상태
 */
public enum SagaStatus {

    /**
     * 시작됨
     */
    STARTED,

    /**
     * 진행 중
     */
    IN_PROGRESS,

    /**
     * 완료됨
     */
    COMPLETED,

    /**
     * 실패 - 보상 진행 중
     */
    COMPENSATING,

    /**
     * 실패 - 보상 완료
     */
    COMPENSATED,

    /**
     * 실패 - 보상도 실패
     */
    FAILED
}
