package com.eraf.core.logging;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 감사 로그 저장소 인터페이스
 * 기본 구현은 NoOp(로깅만), DB 사용 시 JPA 구현으로 대체
 */
public interface AuditLogStore {

    /**
     * 감사 로그 저장
     */
    void save(AuditLogEntry entry);

    /**
     * ID로 감사 로그 조회
     */
    Optional<AuditLogEntry> findById(Long id);

    /**
     * 사용자별 감사 로그 조회
     */
    List<AuditLogEntry> findByUserId(String userId, int limit);

    /**
     * 리소스별 감사 로그 조회
     */
    List<AuditLogEntry> findByResource(String resource, String resourceId, int limit);

    /**
     * 기간별 감사 로그 조회
     */
    List<AuditLogEntry> findByPeriod(Instant from, Instant to, int limit);

    /**
     * 액션별 감사 로그 조회
     */
    List<AuditLogEntry> findByAction(String action, int limit);

    /**
     * 감사 로그 엔트리
     */
    class AuditLogEntry {
        private Long id;
        private Instant timestamp;
        private String traceId;
        private String userId;
        private String username;
        private String clientIp;
        private String action;
        private String resource;
        private String resourceId;
        private String result;
        private String details;

        public AuditLogEntry() {
        }

        public static AuditLogEntry from(AuditLogger.AuditEvent event) {
            AuditLogEntry entry = new AuditLogEntry();
            entry.timestamp = Instant.now();
            entry.traceId = getFieldValue(event, "traceId");
            entry.userId = getFieldValue(event, "userId");
            entry.username = getFieldValue(event, "username");
            entry.clientIp = getFieldValue(event, "clientIp");
            entry.action = getFieldValue(event, "action");
            entry.resource = getFieldValue(event, "resource");
            entry.resourceId = getFieldValue(event, "resourceId");
            entry.result = getFieldValue(event, "result");
            entry.details = extractDetails(event);
            return entry;
        }

        private static String getFieldValue(AuditLogger.AuditEvent event, String fieldName) {
            try {
                var field = AuditLogger.AuditEvent.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(event);
                return value != null ? value.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }

        private static String extractDetails(AuditLogger.AuditEvent event) {
            String json = event.toJson();
            int detailsStart = json.indexOf("\"details\":");
            if (detailsStart > 0) {
                int braceStart = json.indexOf("{", detailsStart);
                int braceEnd = json.lastIndexOf("}");
                if (braceStart > 0 && braceEnd > braceStart) {
                    return json.substring(braceStart, braceEnd);
                }
            }
            return null;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getClientIp() { return clientIp; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }

        public String getResourceId() { return resourceId; }
        public void setResourceId(String resourceId) { this.resourceId = resourceId; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
