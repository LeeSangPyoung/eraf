package com.eraf.gateway.util;

import com.eraf.core.exception.ErrorCode;
import com.eraf.core.http.HttpResponseUtils;
import com.eraf.gateway.exception.GatewayErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gateway 응답 유틸리티
 * eraf-core의 HttpResponseUtils를 활용
 */
public final class GatewayResponseUtils {

    private GatewayResponseUtils() {
    }

    /**
     * 에러 응답 전송
     */
    public static void sendError(HttpServletResponse response, GatewayErrorCode errorCode) throws IOException {
        HttpResponseUtils.sendError(response, errorCode);
    }

    /**
     * 에러 응답 전송 (커스텀 메시지)
     */
    public static void sendError(HttpServletResponse response, GatewayErrorCode errorCode, String message) throws IOException {
        HttpResponseUtils.sendError(response, errorCode, message);
    }

    /**
     * 에러 응답 전송 (ErrorCode 인터페이스)
     */
    public static void sendError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        HttpResponseUtils.sendError(response, errorCode);
    }

    /**
     * 에러 응답 전송 (상태 코드, 코드, 메시지 직접 지정)
     */
    public static void sendError(HttpServletResponse response, int status, String code, String message) throws IOException {
        HttpResponseUtils.sendError(response, status, code, message);
    }

    /**
     * 성공 응답 전송
     */
    public static <T> void sendSuccess(HttpServletResponse response, T data) throws IOException {
        HttpResponseUtils.sendSuccess(response, data);
    }

    /**
     * 성공 응답 전송 (메시지 포함)
     */
    public static <T> void sendSuccess(HttpServletResponse response, T data, String message) throws IOException {
        HttpResponseUtils.sendSuccess(response, data, message);
    }
}
