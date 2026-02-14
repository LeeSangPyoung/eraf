package com.eraf.jpa.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 감사 로그 조회 서비스
 * 복잡한 필터링, 검색, 집계 기능 제공
 */
@Service
@Transactional(readOnly = true)
public class AuditLogQueryService {

    private final AuditLogJpaRepository auditLogRepository;

    public AuditLogQueryService(AuditLogJpaRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 복합 필터 조건으로 감사 로그 검색
     */
    public Page<AuditLogEntity> search(AuditLogSearchCriteria criteria, Pageable pageable) {
        Specification<AuditLogEntity> spec = buildSpecification(criteria);
        return auditLogRepository.findAll(spec, pageable);
    }

    /**
     * 사용자별 최근 활동 조회
     */
    public List<AuditLogEntity> findRecentActivitiesByUser(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * 리소스별 변경 이력 조회
     */
    public List<AuditLogEntity> findResourceHistory(String resource, String resourceId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByResourceAndResourceIdOrderByTimestampDesc(resource, resourceId, pageable);
    }

    /**
     * 특정 기간의 감사 로그 조회
     */
    public List<AuditLogEntity> findByPeriod(Instant from, Instant to, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByPeriod(from, to, pageable);
    }

    /**
     * 최근 N일간의 감사 로그 조회
     */
    public List<AuditLogEntity> findRecentDays(int days, int limit) {
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant to = Instant.now();
        return findByPeriod(from, to, limit);
    }

    /**
     * 특정 액션의 감사 로그 조회
     */
    public List<AuditLogEntity> findByAction(String action, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    /**
     * 실패한 작업 조회
     */
    public Page<AuditLogEntity> findFailures(Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .results(List.of(AuditEventStandard.Result.FAILURE, AuditEventStandard.Result.ERROR))
                .build();
        return search(criteria, pageable);
    }

    /**
     * 접근 거부 로그 조회
     */
    public Page<AuditLogEntity> findAccessDenied(Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .results(List.of(AuditEventStandard.Result.DENIED))
                .build();
        return search(criteria, pageable);
    }

    /**
     * 로그인 실패 이력 조회
     */
    public List<AuditLogEntity> findLoginFailures(String userId, int limit) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .userId(userId)
                .actions(List.of(AuditEventStandard.Action.LOGIN_FAILED))
                .build();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return search(criteria, pageable).getContent();
    }

    /**
     * 트레이스 ID로 연관된 모든 로그 조회
     */
    public List<AuditLogEntity> findByTraceId(String traceId) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .traceId(traceId)
                .build();

        return search(criteria, Pageable.unpaged()).getContent();
    }

    /**
     * Specification 빌드
     */
    private Specification<AuditLogEntity> buildSpecification(AuditLogSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // userId 필터
            if (criteria.getUserId() != null && !criteria.getUserId().isEmpty()) {
                predicates.add(cb.equal(root.get("userId"), criteria.getUserId()));
            }

            // username 필터
            if (criteria.getUsername() != null && !criteria.getUsername().isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + criteria.getUsername() + "%"));
            }

            // actions 필터 (다중 선택)
            if (criteria.getActions() != null && !criteria.getActions().isEmpty()) {
                predicates.add(root.get("action").in(criteria.getActions()));
            }

            // resource 필터
            if (criteria.getResource() != null && !criteria.getResource().isEmpty()) {
                predicates.add(cb.equal(root.get("resource"), criteria.getResource()));
            }

            // resourceId 필터
            if (criteria.getResourceId() != null && !criteria.getResourceId().isEmpty()) {
                predicates.add(cb.equal(root.get("resourceId"), criteria.getResourceId()));
            }

            // results 필터 (다중 선택)
            if (criteria.getResults() != null && !criteria.getResults().isEmpty()) {
                predicates.add(root.get("result").in(criteria.getResults()));
            }

            // clientIp 필터
            if (criteria.getClientIp() != null && !criteria.getClientIp().isEmpty()) {
                predicates.add(cb.equal(root.get("clientIp"), criteria.getClientIp()));
            }

            // traceId 필터
            if (criteria.getTraceId() != null && !criteria.getTraceId().isEmpty()) {
                predicates.add(cb.equal(root.get("traceId"), criteria.getTraceId()));
            }

            // tenantId 필터
            if (criteria.getTenantId() != null && !criteria.getTenantId().isEmpty()) {
                predicates.add(cb.equal(root.get("tenantId"), criteria.getTenantId()));
            }

            // 시간 범위 필터
            if (criteria.getFromTimestamp() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), criteria.getFromTimestamp()));
            }

            if (criteria.getToTimestamp() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), criteria.getToTimestamp()));
            }

            // deleted 필터 (기본값: false)
            if (criteria.getIncludeDeleted() != null && !criteria.getIncludeDeleted()) {
                predicates.add(cb.equal(root.get("deleted"), false));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 검색 조건 빌더 클래스
     */
    public static class AuditLogSearchCriteria {
        private String userId;
        private String username;
        private List<String> actions;
        private String resource;
        private String resourceId;
        private List<String> results;
        private String clientIp;
        private String traceId;
        private String tenantId;
        private Instant fromTimestamp;
        private Instant toTimestamp;
        private Boolean includeDeleted;

        // Getters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public List<String> getActions() { return actions; }
        public String getResource() { return resource; }
        public String getResourceId() { return resourceId; }
        public List<String> getResults() { return results; }
        public String getClientIp() { return clientIp; }
        public String getTraceId() { return traceId; }
        public String getTenantId() { return tenantId; }
        public Instant getFromTimestamp() { return fromTimestamp; }
        public Instant getToTimestamp() { return toTimestamp; }
        public Boolean getIncludeDeleted() { return includeDeleted; }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();

            public Builder userId(String userId) {
                criteria.userId = userId;
                return this;
            }

            public Builder username(String username) {
                criteria.username = username;
                return this;
            }

            public Builder actions(List<String> actions) {
                criteria.actions = actions;
                return this;
            }

            public Builder action(String action) {
                criteria.actions = List.of(action);
                return this;
            }

            public Builder resource(String resource) {
                criteria.resource = resource;
                return this;
            }

            public Builder resourceId(String resourceId) {
                criteria.resourceId = resourceId;
                return this;
            }

            public Builder results(List<String> results) {
                criteria.results = results;
                return this;
            }

            public Builder result(String result) {
                criteria.results = List.of(result);
                return this;
            }

            public Builder clientIp(String clientIp) {
                criteria.clientIp = clientIp;
                return this;
            }

            public Builder traceId(String traceId) {
                criteria.traceId = traceId;
                return this;
            }

            public Builder tenantId(String tenantId) {
                criteria.tenantId = tenantId;
                return this;
            }

            public Builder fromTimestamp(Instant fromTimestamp) {
                criteria.fromTimestamp = fromTimestamp;
                return this;
            }

            public Builder toTimestamp(Instant toTimestamp) {
                criteria.toTimestamp = toTimestamp;
                return this;
            }

            public Builder includeDeleted(Boolean includeDeleted) {
                criteria.includeDeleted = includeDeleted;
                return this;
            }

            public Builder lastNDays(int days) {
                criteria.fromTimestamp = Instant.now().minus(days, ChronoUnit.DAYS);
                criteria.toTimestamp = Instant.now();
                return this;
            }

            public AuditLogSearchCriteria build() {
                return criteria;
            }
        }
    }
}
