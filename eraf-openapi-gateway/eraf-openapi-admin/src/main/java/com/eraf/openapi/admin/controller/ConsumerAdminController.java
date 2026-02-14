package com.eraf.openapi.admin.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.admin.dto.ConsumerRequest;
import com.eraf.openapi.admin.dto.ConsumerResponse;
import com.eraf.openapi.admin.service.ConsumerAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consumer 관리 REST API
 */
@RestController
@RequestMapping("/admin/consumers")
@RequiredArgsConstructor
public class ConsumerAdminController {

    private final ConsumerAdminService consumerAdminService;

    /**
     * 모든 Consumer 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsumerResponse>>> getAllConsumers(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<ConsumerResponse> consumers = enabledOnly ?
                consumerAdminService.findAllEnabled() :
                consumerAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(consumers));
    }

    /**
     * Consumer 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsumerResponse>> getConsumer(@PathVariable Long id) {
        ConsumerResponse consumer = consumerAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(consumer));
    }

    /**
     * Consumer 상세 조회 (Username)
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<ConsumerResponse>> getConsumerByUsername(@PathVariable String username) {
        ConsumerResponse consumer = consumerAdminService.findByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(consumer));
    }

    /**
     * Consumer 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConsumerResponse>> createConsumer(@Valid @RequestBody ConsumerRequest request) {
        ConsumerResponse created = consumerAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Consumer 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsumerResponse>> updateConsumer(
            @PathVariable Long id,
            @Valid @RequestBody ConsumerRequest request) {
        ConsumerResponse updated = consumerAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Consumer 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConsumer(@PathVariable Long id) {
        consumerAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Consumer 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ConsumerResponse>> toggleConsumerEnabled(@PathVariable Long id) {
        ConsumerResponse updated = consumerAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * API Key 재생성
     */
    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<ApiResponse<ConsumerResponse>> regenerateApiKey(@PathVariable Long id) {
        ConsumerResponse updated = consumerAdminService.regenerateApiKey(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Consumer 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = consumerAdminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ===== 고급 기능: API Key 만료 관리 =====

    /**
     * API Key 만료 기한 설정
     * POST /admin/consumers/{id}/expiration
     * Body: {"expiresAt": "2024-12-31T23:59:59"}
     */
    @PostMapping("/{id}/expiration")
    public ResponseEntity<ApiResponse<ConsumerResponse>> setExpiration(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        LocalDateTime expiresAt = LocalDateTime.parse(request.get("expiresAt"));
        ConsumerResponse updated = consumerAdminService.setExpiration(id, expiresAt);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 만료된 Consumer 조회
     * GET /admin/consumers/expired
     */
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<ConsumerResponse>>> getExpiredConsumers() {
        List<ConsumerResponse> expired = consumerAdminService.findExpired();
        return ResponseEntity.ok(ApiResponse.success(expired));
    }

    /**
     * 곧 만료될 Consumer 조회
     * GET /admin/consumers/expiring-soon?days=7
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<ConsumerResponse>>> getExpiringSoonConsumers(
            @RequestParam(defaultValue = "7") int days) {
        List<ConsumerResponse> expiring = consumerAdminService.findExpiringSoon(days);
        return ResponseEntity.ok(ApiResponse.success(expiring));
    }

    // ===== 고급 기능: IP 화이트리스트 =====

    /**
     * IP 화이트리스트 설정
     * POST /admin/consumers/{id}/ip-whitelist
     * Body: {"ipRanges": ["192.168.1.0/24", "10.0.0.1"]}
     */
    @PostMapping("/{id}/ip-whitelist")
    public ResponseEntity<ApiResponse<ConsumerResponse>> setIpWhitelist(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> request) {
        List<String> ipRanges = request.get("ipRanges");
        ConsumerResponse updated = consumerAdminService.setIpWhitelist(id, ipRanges);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // ===== 고급 기능: 스코프/권한 =====

    /**
     * 스코프 설정
     * POST /admin/consumers/{id}/scopes
     * Body: {"scopes": ["read:api", "write:api", "admin:*"]}
     */
    @PostMapping("/{id}/scopes")
    public ResponseEntity<ApiResponse<ConsumerResponse>> setScopes(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> request) {
        List<String> scopes = request.get("scopes");
        ConsumerResponse updated = consumerAdminService.setScopes(id, scopes);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // ===== 고급 기능: 사용 통계 =====

    /**
     * API Key 사용 통계 조회
     * GET /admin/consumers/{id}/usage-stats
     */
    @GetMapping("/{id}/usage-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsageStats(@PathVariable Long id) {
        Map<String, Object> stats = consumerAdminService.getUsageStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * API Key 유효성 검증
     * POST /admin/consumers/validate
     * Body: {"apiKey": "ck_xxx", "requestIp": "192.168.1.1"}
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateApiKey(
            @RequestBody Map<String, String> request) {
        String apiKey = request.get("apiKey");
        String requestIp = request.get("requestIp");
        Map<String, Object> result = consumerAdminService.validateApiKey(apiKey, requestIp);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
