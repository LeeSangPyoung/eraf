package com.eraf.jpa.audit;

import com.eraf.response.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 감사 로그 Admin REST API
 * 로그 조회 및 검색 기능 제공
 */
@RestController
@RequestMapping("/admin/audit-logs")
@ConditionalOnProperty(name = "eraf.audit.admin-enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAdminController {

    private final AuditLogQueryService queryService;
    private final AuditLogJpaRepository auditLogRepository;

    public AuditLogAdminController(
            AuditLogQueryService queryService,
            AuditLogJpaRepository auditLogRepository) {
        this.queryService = queryService;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 감사 로그 검색 (복합 필터)
     * GET /admin/audit-logs?userId=user1&action=LOGIN&from=2024-01-01T00:00:00Z&size=20&page=0
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> search(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) List<String> actions,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) List<String> results,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String[] sort) {

        AuditLogQueryService.AuditLogSearchCriteria.Builder criteriaBuilder =
                AuditLogQueryService.AuditLogSearchCriteria.builder();

        if (userId != null) criteriaBuilder.userId(userId);
        if (username != null) criteriaBuilder.username(username);
        if (actions != null && !actions.isEmpty()) criteriaBuilder.actions(actions);
        if (resource != null) criteriaBuilder.resource(resource);
        if (resourceId != null) criteriaBuilder.resourceId(resourceId);
        if (results != null && !results.isEmpty()) criteriaBuilder.results(results);
        if (clientIp != null) criteriaBuilder.clientIp(clientIp);
        if (traceId != null) criteriaBuilder.traceId(traceId);
        if (tenantId != null) criteriaBuilder.tenantId(tenantId);
        if (from != null) criteriaBuilder.fromTimestamp(from);
        if (to != null) criteriaBuilder.toTimestamp(to);
        criteriaBuilder.includeDeleted(includeDeleted);

        AuditLogQueryService.AuditLogSearchCriteria criteria = criteriaBuilder.build();

        // Build Pageable with sorting
        Sort sortObj = Sort.by(
                sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<AuditLogEntity> entityPage = queryService.search(criteria, pageable);
        Page<AuditLogResponse> responsePage = entityPage.map(AuditLogResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * ID로 감사 로그 조회
     * GET /admin/audit-logs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(@PathVariable Long id) {
        Optional<AuditLogEntity> entityOpt = auditLogRepository.findById(id);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AuditLogResponse response = AuditLogResponse.from(entityOpt.get());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자별 최근 활동 조회
     * GET /admin/audit-logs/user/{userId}/recent?limit=50
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentActivitiesByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {

        List<AuditLogEntity> entities = queryService.findRecentActivitiesByUser(userId, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 리소스별 변경 이력 조회
     * GET /admin/audit-logs/resource/{resource}/{resourceId}?limit=50
     */
    @GetMapping("/resource/{resource}/{resourceId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getResourceHistory(
            @PathVariable String resource,
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "50") int limit) {

        List<AuditLogEntity> entities = queryService.findResourceHistory(resource, resourceId, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 특정 기간의 감사 로그 조회
     * GET /admin/audit-logs/period?from=2024-01-01T00:00:00Z&to=2024-12-31T23:59:59Z&limit=100
     */
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByPeriod(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "100") int limit) {

        List<AuditLogEntity> entities = queryService.findByPeriod(from, to, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 최근 N일간의 감사 로그 조회
     * GET /admin/audit-logs/recent-days?days=7&limit=100
     */
    @GetMapping("/recent-days")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentDays(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "100") int limit) {

        List<AuditLogEntity> entities = queryService.findRecentDays(days, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 특정 액션의 감사 로그 조회
     * GET /admin/audit-logs/action/{action}?limit=50
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "50") int limit) {

        List<AuditLogEntity> entities = queryService.findByAction(action, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 실패한 작업 조회
     * GET /admin/audit-logs/failures?page=0&size=20
     */
    @GetMapping("/failures")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getFailures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogEntity> entityPage = queryService.findFailures(pageable);
        Page<AuditLogResponse> responsePage = entityPage.map(AuditLogResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * 접근 거부 로그 조회
     * GET /admin/audit-logs/access-denied?page=0&size=20
     */
    @GetMapping("/access-denied")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAccessDenied(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogEntity> entityPage = queryService.findAccessDenied(pageable);
        Page<AuditLogResponse> responsePage = entityPage.map(AuditLogResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * 로그인 실패 이력 조회
     * GET /admin/audit-logs/login-failures/{userId}?limit=20
     */
    @GetMapping("/login-failures/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getLoginFailures(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {

        List<AuditLogEntity> entities = queryService.findLoginFailures(userId, limit);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 트레이스 ID로 연관된 모든 로그 조회
     * GET /admin/audit-logs/trace/{traceId}
     */
    @GetMapping("/trace/{traceId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByTraceId(
            @PathVariable String traceId) {

        List<AuditLogEntity> entities = queryService.findByTraceId(traceId);
        List<AuditLogResponse> responses = entities.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 감사 로그 통계 조회
     * GET /admin/audit-logs/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AuditLogStats>> getStats() {
        long total = auditLogRepository.count();
        long failures = auditLogRepository.countByResult(AuditEventStandard.Result.FAILURE);
        long errors = auditLogRepository.countByResult(AuditEventStandard.Result.ERROR);
        long denied = auditLogRepository.countByResult(AuditEventStandard.Result.DENIED);

        AuditLogStats stats = new AuditLogStats(total, failures, errors, denied);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 감사 로그 통계 DTO
     */
    public static class AuditLogStats {
        private final long total;
        private final long failures;
        private final long errors;
        private final long denied;

        public AuditLogStats(long total, long failures, long errors, long denied) {
            this.total = total;
            this.failures = failures;
            this.errors = errors;
            this.denied = denied;
        }

        public long getTotal() {
            return total;
        }

        public long getFailures() {
            return failures;
        }

        public long getErrors() {
            return errors;
        }

        public long getDenied() {
            return denied;
        }
    }
}
