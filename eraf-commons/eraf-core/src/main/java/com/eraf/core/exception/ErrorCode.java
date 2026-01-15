package com.eraf.core.exception;

/**
 * 에러 코드 인터페이스
 * 프로젝트에서 enum으로 구현하여 사용
 */
public interface ErrorCode {

    /**
     * 에러 코드 (예: "USER_NOT_FOUND")
     */
    String getCode();

    /**
     * 에러 메시지
     */
    String getMessage();

    /**
     * HTTP 상태 코드
     */
    int getStatus();
}
