package com.eraf.jpa.audit;

/**
 * 표준 감사 이벤트 정의
 * 일관된 감사 로깅을 위한 표준 액션/결과 상수
 */
public final class AuditEventStandard {

    private AuditEventStandard() {
        // Utility class - prevent instantiation
    }

    /**
     * 표준 액션
     */
    public static final class Action {
        private Action() {}

        // CRUD 기본 액션
        public static final String CREATE = "CREATE";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String SOFT_DELETE = "SOFT_DELETE";
        public static final String RESTORE = "RESTORE";

        // 인증/권한 액션
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String ACCESS_DENIED = "ACCESS_DENIED";
        public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";

        // 시스템 액션
        public static final String SYSTEM_START = "SYSTEM_START";
        public static final String SYSTEM_STOP = "SYSTEM_STOP";
        public static final String CONFIG_CHANGE = "CONFIG_CHANGE";
        public static final String BACKUP = "BACKUP";
        public static final String RESTORE_BACKUP = "RESTORE_BACKUP";

        // 데이터 액션
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String DOWNLOAD = "DOWNLOAD";
        public static final String UPLOAD = "UPLOAD";

        // 승인 프로세스
        public static final String APPROVE = "APPROVE";
        public static final String REJECT = "REJECT";
        public static final String SUBMIT = "SUBMIT";
        public static final String CANCEL = "CANCEL";

        // 기타
        public static final String SEARCH = "SEARCH";
        public static final String PRINT = "PRINT";
        public static final String SEND_EMAIL = "SEND_EMAIL";
        public static final String SEND_SMS = "SEND_SMS";
    }

    /**
     * 표준 결과
     */
    public static final class Result {
        private Result() {}

        public static final String SUCCESS = "SUCCESS";
        public static final String FAILURE = "FAILURE";
        public static final String ERROR = "ERROR";
        public static final String PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
        public static final String DENIED = "DENIED";
        public static final String TIMEOUT = "TIMEOUT";
        public static final String CANCELLED = "CANCELLED";
    }

    /**
     * 표준 리소스 타입
     */
    public static final class Resource {
        private Resource() {}

        // 사용자 관련
        public static final String USER = "USER";
        public static final String ROLE = "ROLE";
        public static final String PERMISSION = "PERMISSION";
        public static final String USER_GROUP = "USER_GROUP";

        // 시스템 관련
        public static final String SYSTEM = "SYSTEM";
        public static final String CONFIG = "CONFIG";
        public static final String LOG = "LOG";
        public static final String AUDIT = "AUDIT";

        // 데이터 관련
        public static final String DATABASE = "DATABASE";
        public static final String FILE = "FILE";
        public static final String DOCUMENT = "DOCUMENT";
        public static final String REPORT = "REPORT";

        // 비즈니스 리소스 (프로젝트별로 확장)
        public static final String ORDER = "ORDER";
        public static final String PRODUCT = "PRODUCT";
        public static final String CUSTOMER = "CUSTOMER";
        public static final String INVOICE = "INVOICE";
    }

    /**
     * 감사 로그 빌더
     */
    public static class AuditLogBuilder {
        private final AuditLogEntity entity = new AuditLogEntity();

        public AuditLogBuilder action(String action) {
            entity.setAction(action);
            return this;
        }

        public AuditLogBuilder resource(String resource) {
            entity.setResource(resource);
            return this;
        }

        public AuditLogBuilder resourceId(String resourceId) {
            entity.setResourceId(resourceId);
            return this;
        }

        public AuditLogBuilder result(String result) {
            entity.setResult(result);
            return this;
        }

        public AuditLogBuilder details(String details) {
            entity.setDetails(details);
            return this;
        }

        public AuditLogBuilder userId(String userId) {
            entity.setUserId(userId);
            return this;
        }

        public AuditLogBuilder username(String username) {
            entity.setUsername(username);
            return this;
        }

        public AuditLogBuilder clientIp(String clientIp) {
            entity.setClientIp(clientIp);
            return this;
        }

        public AuditLogBuilder traceId(String traceId) {
            entity.setTraceId(traceId);
            return this;
        }

        public AuditLogBuilder tenantId(String tenantId) {
            entity.setTenantId(tenantId);
            return this;
        }

        public AuditLogEntity build() {
            if (entity.getTimestamp() == null) {
                entity.setTimestamp(java.time.Instant.now());
            }
            if (entity.getDeleted() == null) {
                entity.setDeleted(false);
            }
            return entity;
        }
    }

    /**
     * 새 감사 로그 빌더 생성
     */
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    /**
     * 미리 정의된 성공 로그 생성
     */
    public static AuditLogEntity success(String action, String resource, String resourceId) {
        return builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(Result.SUCCESS)
                .build();
    }

    /**
     * 미리 정의된 실패 로그 생성
     */
    public static AuditLogEntity failure(String action, String resource, String resourceId, String errorDetails) {
        return builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(Result.FAILURE)
                .details(errorDetails)
                .build();
    }

    /**
     * 미리 정의된 접근 거부 로그 생성
     */
    public static AuditLogEntity denied(String action, String resource, String resourceId) {
        return builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(Result.DENIED)
                .build();
    }
}
