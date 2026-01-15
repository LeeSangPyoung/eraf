package com.eraf.core.sequence;

/**
 * 채번 리셋 정책
 */
public enum Reset {

    /**
     * 매일 초기화
     */
    DAILY,

    /**
     * 매월 초기화
     */
    MONTHLY,

    /**
     * 매년 초기화
     */
    YEARLY,

    /**
     * 초기화 안 함
     */
    NEVER
}
