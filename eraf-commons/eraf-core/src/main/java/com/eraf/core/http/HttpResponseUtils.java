package com.eraf.core.http;

import com.eraf.core.exception.ErrorCode;
import com.eraf.core.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * HTTP 응답 유틸리티
 * ApiResponse 형식으로 표준화된 응답 작성
 */
public final class HttpResponseUtils {

    private static final ObjectMapper objectMapper;
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private HttpResponseUtils() {
    }

    /**
     * 에러 응답 전송 (ErrorCode 사용)
     */
    public static void sendError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        sendError(response, errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 에러 응답 전송 (ErrorCode + 커스텀 메시지)
     */
    public static void sendError(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        sendError(response, errorCode.getStatus(), errorCode.getCode(), message);
    }

    /**
     * 에러 응답 전송 (직접 지정)
     */
    public static void sendError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(CONTENT_TYPE_JSON);

        ApiResponse<?> apiResponse = ApiResponse.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * 성공 응답 전송 (데이터 없음)
     */
    public static void sendSuccess(HttpServletResponse response) throws IOException {
        sendSuccess(response, null);
    }

    /**
     * 성공 응답 전송 (데이터 포함)
     */
    public static <T> void sendSuccess(HttpServletResponse response, T data) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(CONTENT_TYPE_JSON);

        ApiResponse<T> apiResponse = ApiResponse.success(data);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * 성공 응답 전송 (데이터 + 메시지)
     */
    public static <T> void sendSuccess(HttpServletResponse response, T data, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(CONTENT_TYPE_JSON);

        ApiResponse<T> apiResponse = ApiResponse.success(data, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * JSON 응답 전송 (상태 코드 지정)
     */
    public static <T> void sendJson(HttpServletResponse response, int status, T data) throws IOException {
        response.setStatus(status);
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().write(objectMapper.writeValueAsString(data));
    }

    /**
     * ObjectMapper 인스턴스 반환 (커스텀 직렬화 필요 시)
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
