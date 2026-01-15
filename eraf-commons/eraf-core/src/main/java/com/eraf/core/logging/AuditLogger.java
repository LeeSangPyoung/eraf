package com.eraf.core.logging;

import com.eraf.core.context.ErafContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 감사 로거 (사용자 활동 기록)
 */
public class AuditLogger {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static AuditLogStore auditLogStore = new NoOpAuditLogStore();

    /**
     * AuditLogStore 설정 (DB 저장용)
     */
    public static void setAuditLogStore(AuditLogStore store) {
        if (store != null) {
            auditLogStore = store;
        }
    }

    /**
     * 감사 로그 기록
     */
    public static void log(AuditEvent event) {
        // 로그 파일 출력
        AUDIT_LOGGER.info(event.toJson());

        // DB 저장 (AuditLogStore 구현체에 따라 동작)
        try {
            AuditLogStore.AuditLogEntry entry = AuditLogStore.AuditLogEntry.from(event);
            auditLogStore.save(entry);
        } catch (Exception e) {
            AUDIT_LOGGER.warn("감사 로그 DB 저장 실패", e);
        }
    }

    /**
     * 간단한 감사 로그 기록
     */
    public static void log(String action, String resource) {
        AuditEvent event = AuditEvent.builder()
                .action(action)
                .resource(resource)
                .build();
        log(event);
    }

    /**
     * 상세 감사 로그 기록
     */
    public static void log(String action, String resource, String resourceId, Map<String, Object> details) {
        AuditEvent event = AuditEvent.builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .build();
        log(event);
    }

    /**
     * 감사 이벤트
     */
    public static class AuditEvent {
        private final Instant timestamp;
        private final String traceId;
        private final String userId;
        private final String username;
        private final String clientIp;
        private final String action;
        private final String resource;
        private final String resourceId;
        private final String result;
        private final Map<String, Object> details;

        private AuditEvent(Builder builder) {
            this.timestamp = Instant.now();
            this.traceId = builder.traceId != null ? builder.traceId : ErafContext.getTraceId();
            this.userId = builder.userId != null ? builder.userId : ErafContext.getCurrentUserId();
            this.username = builder.username != null ? builder.username : ErafContext.getCurrentUsername();
            this.clientIp = builder.clientIp != null ? builder.clientIp : ErafContext.getClientIp();
            this.action = builder.action;
            this.resource = builder.resource;
            this.resourceId = builder.resourceId;
            this.result = builder.result != null ? builder.result : "SUCCESS";
            this.details = builder.details;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"timestamp\":\"").append(timestamp).append("\"");
            appendField(sb, "traceId", traceId);
            appendField(sb, "userId", userId);
            appendField(sb, "username", username);
            appendField(sb, "clientIp", clientIp);
            appendField(sb, "action", action);
            appendField(sb, "resource", resource);
            appendField(sb, "resourceId", resourceId);
            appendField(sb, "result", result);
            if (details != null && !details.isEmpty()) {
                sb.append(", \"details\":{");
                boolean first = true;
                for (Map.Entry<String, Object> entry : details.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(entry.getKey()).append("\":");
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        sb.append("\"").append(value).append("\"");
                    } else {
                        sb.append(value);
                    }
                    first = false;
                }
                sb.append("}");
            }
            sb.append("}");
            return sb.toString();
        }

        private void appendField(StringBuilder sb, String key, String value) {
            if (value != null) {
                sb.append(", \"").append(key).append("\":\"").append(value).append("\"");
            }
        }

        public static class Builder {
            private String traceId;
            private String userId;
            private String username;
            private String clientIp;
            private String action;
            private String resource;
            private String resourceId;
            private String result;
            private Map<String, Object> details = new LinkedHashMap<>();

            public Builder traceId(String traceId) {
                this.traceId = traceId;
                return this;
            }

            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder clientIp(String clientIp) {
                this.clientIp = clientIp;
                return this;
            }

            public Builder action(String action) {
                this.action = action;
                return this;
            }

            public Builder resource(String resource) {
                this.resource = resource;
                return this;
            }

            public Builder resourceId(String resourceId) {
                this.resourceId = resourceId;
                return this;
            }

            public Builder result(String result) {
                this.result = result;
                return this;
            }

            public Builder success() {
                this.result = "SUCCESS";
                return this;
            }

            public Builder failure() {
                this.result = "FAILURE";
                return this;
            }

            public Builder detail(String key, Object value) {
                this.details.put(key, value);
                return this;
            }

            public Builder details(Map<String, Object> details) {
                if (details != null) {
                    this.details.putAll(details);
                }
                return this;
            }

            public AuditEvent build() {
                return new AuditEvent(this);
            }
        }
    }
}
