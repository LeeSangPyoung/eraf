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

        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.AUTHENTICATION_FAILURE)
                .principal("testuser")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .requestUri("/api/login")
                .method("POST")
                .timestamp(timestamp)
                .success(false)
                .details(details)
                .build();

        assertEquals(SecurityAuditEvent.EventType.AUTHENTICATION_FAILURE, event.getEventType());
        assertEquals("testuser", event.getPrincipal());
        assertEquals("192.168.1.100", event.getIpAddress());
        assertEquals("Mozilla/5.0", event.getUserAgent());
        assertEquals("/api/login", event.getRequestUri());
        assertEquals("POST", event.getMethod());
        assertEquals(timestamp, event.getTimestamp());
        assertFalse(event.isSuccess());
        assertEquals("Invalid credentials", event.getDetails().get("reason"));
    }

    @Test
    @DisplayName("성공 이벤트 생성")
    void shouldCreateSuccessEvent() {
        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGIN_SUCCESS)
                .principal("admin")
                .success(true)
                .build();

        assertTrue(event.isSuccess());
        assertEquals(SecurityAuditEvent.EventType.LOGIN_SUCCESS, event.getEventType());
    }

    @Test
    @DisplayName("Details에 여러 정보 추가")
    void shouldStoreMultipleDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", "sess-123");
        details.put("roles", new String[]{"ROLE_USER", "ROLE_ADMIN"});
        details.put("loginAttempts", 3);

        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGIN_SUCCESS)
                .details(details)
                .build();

        assertEquals("sess-123", event.getDetails().get("sessionId"));
        assertEquals(3, event.getDetails().get("loginAttempts"));
    }

    @Test
    @DisplayName("null 값 허용")
    void shouldAllowNullValues() {
        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.ACCESS_DENIED)
                .principal(null)
                .ipAddress(null)
                .details(null)
                .build();

        assertNull(event.getPrincipal());
        assertNull(event.getIpAddress());
        assertNull(event.getDetails());
    }

    @Test
    @DisplayName("다양한 이벤트 타입")
    void shouldSupportVariousEventTypes() {
        SecurityAuditEvent.EventType[] eventTypes = {
                SecurityAuditEvent.EventType.LOGIN_SUCCESS,
                SecurityAuditEvent.EventType.LOGIN_FAILURE,
                SecurityAuditEvent.EventType.LOGOUT,
                SecurityAuditEvent.EventType.ACCESS_DENIED,
                SecurityAuditEvent.EventType.SESSION_CREATED,
                SecurityAuditEvent.EventType.SESSION_EXPIRED,
                SecurityAuditEvent.EventType.PASSWORD_CHANGE,
                SecurityAuditEvent.EventType.ACCOUNT_LOCKED,
                SecurityAuditEvent.EventType.API_KEY_USED
        };

        for (SecurityAuditEvent.EventType eventType : eventTypes) {
            SecurityAuditEvent event = SecurityAuditEvent.builder(eventType)
                    .build();

            assertEquals(eventType, event.getEventType());
        }
    }

    @Test
    @DisplayName("기본 타임스탬프 자동 설정")
    void shouldSetDefaultTimestamp() {
        Instant before = Instant.now();
        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.TOKEN_CREATED)
                .principal("user")
                .build();
        Instant after = Instant.now();

        assertNotNull(event.getTimestamp());
        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    @DisplayName("toString 포함 정보")
    void shouldIncludeInfoInToString() {
        SecurityAuditEvent event = SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGIN_SUCCESS)
                .principal("admin")
                .ipAddress("10.0.0.1")
                .build();

        String str = event.toString();
        assertTrue(str.contains("LOGIN_SUCCESS"));
        assertTrue(str.contains("admin"));
        assertTrue(str.contains("10.0.0.1"));
    }
}
