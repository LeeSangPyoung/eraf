package com.eraf.security.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityAuditLogger 테스트
 */
@ExtendWith(MockitoExtension.class)
class SecurityAuditLoggerTest {

    private SecurityAuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new SecurityAuditLogger();
    }

    @Test
    @DisplayName("감사 이벤트 로깅 (예외 없이 실행)")
    void shouldLogAuditEventWithoutException() {
        // Given
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHENTICATION_SUCCESS")
                .username("testuser")
                .ipAddress("192.168.1.100")
                .requestUri("/api/login")
                .requestMethod("POST")
                .timestamp(Instant.now())
                .success(true)
                .build();

        // When & Then - 예외 없이 실행되어야 함
        assertDoesNotThrow(() -> auditLogger.log(event));
    }

    @Test
    @DisplayName("실패 이벤트 로깅")
    void shouldLogFailureEvent() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "Invalid password");
        details.put("attempts", 3);

        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHENTICATION_FAILURE")
                .username("hacker")
                .ipAddress("10.0.0.99")
                .requestUri("/api/login")
                .requestMethod("POST")
                .timestamp(Instant.now())
                .success(false)
                .details(details)
                .build();

        // When & Then
        assertDoesNotThrow(() -> auditLogger.log(event));
    }

    @Test
    @DisplayName("null 필드가 있는 이벤트 로깅")
    void shouldLogEventWithNullFields() {
        // Given
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("SESSION_EXPIRED")
                .timestamp(Instant.now())
                .success(false)
                .build();

        // When & Then
        assertDoesNotThrow(() -> auditLogger.log(event));
    }

    @Test
    @DisplayName("상세 정보가 포함된 이벤트 로깅")
    void shouldLogEventWithDetails() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", "sess-12345");
        details.put("roles", new String[]{"ROLE_ADMIN", "ROLE_USER"});
        details.put("loginTime", Instant.now().toString());
        details.put("nestedObject", Map.of("key", "value"));

        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("USER_LOGIN")
                .username("admin")
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .requestUri("/api/auth/login")
                .requestMethod("POST")
                .timestamp(Instant.now())
                .success(true)
                .details(details)
                .build();

        // When & Then
        assertDoesNotThrow(() -> auditLogger.log(event));
    }

    @Test
    @DisplayName("다양한 이벤트 타입 로깅")
    void shouldLogVariousEventTypes() {
        // Given
        String[] eventTypes = {
                "AUTHENTICATION_SUCCESS",
                "AUTHENTICATION_FAILURE",
                "AUTHORIZATION_FAILURE",
                "SESSION_CREATED",
                "SESSION_DESTROYED",
                "PASSWORD_CHANGED",
                "ACCOUNT_LOCKED",
                "API_KEY_USED"
        };

        // When & Then
        for (String eventType : eventTypes) {
            SecurityAuditEvent event = SecurityAuditEvent.builder()
                    .eventType(eventType)
                    .timestamp(Instant.now())
                    .success(true)
                    .build();

            assertDoesNotThrow(() -> auditLogger.log(event));
        }
    }
}
