package com.eraf.security.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityAuditEvent 테스트
 */
class SecurityAuditEventTest {

    @Test
    @DisplayName("Builder를 통한 이벤트 생성")
    void shouldCreateEventUsingBuilder() {
        Instant timestamp = Instant.now();
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "Invalid credentials");

        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHENTICATION_FAILURE")
                .username("testuser")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .requestUri("/api/login")
                .requestMethod("POST")
                .timestamp(timestamp)
                .success(false)
                .details(details)
                .build();

        assertEquals("AUTHENTICATION_FAILURE", event.getEventType());
        assertEquals("testuser", event.getUsername());
        assertEquals("192.168.1.100", event.getIpAddress());
        assertEquals("Mozilla/5.0", event.getUserAgent());
        assertEquals("/api/login", event.getRequestUri());
        assertEquals("POST", event.getRequestMethod());
        assertEquals(timestamp, event.getTimestamp());
        assertFalse(event.isSuccess());
        assertEquals("Invalid credentials", event.getDetails().get("reason"));
    }

    @Test
    @DisplayName("성공 이벤트 생성")
    void shouldCreateSuccessEvent() {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHENTICATION_SUCCESS")
                .username("admin")
                .success(true)
                .build();

        assertTrue(event.isSuccess());
        assertEquals("AUTHENTICATION_SUCCESS", event.getEventType());
    }

    @Test
    @DisplayName("Setter/Getter 동작")
    void shouldWorkWithSettersAndGetters() {
        SecurityAuditEvent event = new SecurityAuditEvent();
        Instant now = Instant.now();

        event.setEventType("ACCESS_DENIED");
        event.setUsername("user123");
        event.setIpAddress("10.0.0.1");
        event.setUserAgent("CustomAgent/1.0");
        event.setRequestUri("/admin/users");
        event.setRequestMethod("DELETE");
        event.setTimestamp(now);
        event.setSuccess(false);

        assertEquals("ACCESS_DENIED", event.getEventType());
        assertEquals("user123", event.getUsername());
        assertEquals("10.0.0.1", event.getIpAddress());
        assertEquals("CustomAgent/1.0", event.getUserAgent());
        assertEquals("/admin/users", event.getRequestUri());
        assertEquals("DELETE", event.getRequestMethod());
        assertEquals(now, event.getTimestamp());
        assertFalse(event.isSuccess());
    }

    @Test
    @DisplayName("Details에 여러 정보 추가")
    void shouldStoreMultipleDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", "sess-123");
        details.put("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
        details.put("loginAttempts", 3);

        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("LOGIN")
                .details(details)
                .build();

        assertEquals("sess-123", event.getDetails().get("sessionId"));
        assertEquals(3, event.getDetails().get("loginAttempts"));
    }

    @Test
    @DisplayName("null 값 허용")
    void shouldAllowNullValues() {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("UNKNOWN")
                .username(null)
                .ipAddress(null)
                .details(null)
                .build();

        assertNull(event.getUsername());
        assertNull(event.getIpAddress());
        assertNull(event.getDetails());
    }

    @Test
    @DisplayName("다양한 이벤트 타입")
    void shouldSupportVariousEventTypes() {
        String[] eventTypes = {
                "AUTHENTICATION_SUCCESS",
                "AUTHENTICATION_FAILURE",
                "AUTHORIZATION_FAILURE",
                "SESSION_CREATED",
                "SESSION_EXPIRED",
                "PASSWORD_CHANGED",
                "ACCOUNT_LOCKED",
                "API_KEY_USED"
        };

        for (String eventType : eventTypes) {
            SecurityAuditEvent event = SecurityAuditEvent.builder()
                    .eventType(eventType)
                    .build();

            assertEquals(eventType, event.getEventType());
        }
    }
}
