package com.eraf.security.audit;

import java.time.Instant;
import java.util.Map;

/**
 * 보안 감사 이벤트
 */
public class SecurityAuditEvent {

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        ACCESS_DENIED,
        AUTHENTICATION_FAILURE,
        PASSWORD_CHANGE,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        SESSION_CREATED,
        SESSION_EXPIRED,
        API_KEY_USED,
        API_KEY_INVALID,
        TOKEN_CREATED,
        TOKEN_REFRESHED,
        TOKEN_EXPIRED,
        TOKEN_INVALID
    }

    private final Instant timestamp;
    private final EventType eventType;
    private final String principal;
    private final String ipAddress;
    private final String userAgent;
    private final String requestUri;
    private final String method;
    private final Map<String, Object> details;
    private final boolean success;

    private SecurityAuditEvent(Builder builder) {
        this.timestamp = builder.timestamp;
        this.eventType = builder.eventType;
        this.principal = builder.principal;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.requestUri = builder.requestUri;
        this.method = builder.method;
        this.details = builder.details;
        this.success = builder.success;
    }

    public static Builder builder(EventType eventType) {
        return new Builder(eventType);
    }

    // Getters
    public Instant getTimestamp() { return timestamp; }
    public EventType getEventType() { return eventType; }
    public String getPrincipal() { return principal; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getRequestUri() { return requestUri; }
    public String getMethod() { return method; }
    public Map<String, Object> getDetails() { return details; }
    public boolean isSuccess() { return success; }

    public static class Builder {
        private Instant timestamp = Instant.now();
        private final EventType eventType;
        private String principal;
        private String ipAddress;
        private String userAgent;
        private String requestUri;
        private String method;
        private Map<String, Object> details;
        private boolean success = true;

        public Builder(EventType eventType) {
            this.eventType = eventType;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder principal(String principal) {
            this.principal = principal;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public SecurityAuditEvent build() {
            return new SecurityAuditEvent(this);
        }
    }

    @Override
    public String toString() {
        return "SecurityAuditEvent{" +
                "timestamp=" + timestamp +
                ", eventType=" + eventType +
                ", principal='" + principal + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", success=" + success +
                '}';
    }
}
