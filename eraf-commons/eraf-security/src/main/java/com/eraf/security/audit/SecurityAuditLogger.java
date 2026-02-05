package com.eraf.security.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 보안 감사 로거
 */
public class SecurityAuditLogger {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("security.audit");

    private final ObjectMapper objectMapper;
    private final List<Consumer<SecurityAuditEvent>> listeners = new ArrayList<>();

    public SecurityAuditLogger() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public SecurityAuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 감사 이벤트 기록
     */
    public void log(SecurityAuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            AUDIT_LOG.info("{}", json);
        } catch (JsonProcessingException e) {
            AUDIT_LOG.info("{}", event);
        }

        // Notify listeners
        listeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                AUDIT_LOG.warn("Failed to notify audit listener: {}", e.getMessage());
            }
        });
    }

    /**
     * 로그인 성공 기록
     */
    public void logLoginSuccess(String principal, String ipAddress, String userAgent) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGIN_SUCCESS)
                .principal(principal)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build());
    }

    /**
     * 로그인 실패 기록
     */
    public void logLoginFailure(String principal, String ipAddress, String userAgent, String reason) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGIN_FAILURE)
                .principal(principal)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .details(reason != null ? java.util.Map.of("reason", reason) : null)
                .build());
    }

    /**
     * 로그아웃 기록
     */
    public void logLogout(String principal, String ipAddress) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGOUT)
                .principal(principal)
                .ipAddress(ipAddress)
                .success(true)
                .build());
    }

    /**
     * 접근 거부 기록
     */
    public void logAccessDenied(String principal, String ipAddress, String requestUri, String method) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.ACCESS_DENIED)
                .principal(principal)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .method(method)
                .success(false)
                .build());
    }

    /**
     * API Key 사용 기록
     */
    public void logApiKeyUsed(String clientName, String ipAddress, String requestUri) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.API_KEY_USED)
                .principal(clientName)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .success(true)
                .build());
    }

    /**
     * 잘못된 API Key 사용 시도 기록
     */
    public void logApiKeyInvalid(String ipAddress, String requestUri) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.API_KEY_INVALID)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .success(false)
                .build());
    }

    /**
     * 토큰 생성 기록
     */
    public void logTokenCreated(String principal, String ipAddress) {
        log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.TOKEN_CREATED)
                .principal(principal)
                .ipAddress(ipAddress)
                .success(true)
                .build());
    }

    /**
     * 리스너 등록
     */
    public void addListener(Consumer<SecurityAuditEvent> listener) {
        listeners.add(listener);
    }

    /**
     * 리스너 제거
     */
    public void removeListener(Consumer<SecurityAuditEvent> listener) {
        listeners.remove(listener);
    }
}
