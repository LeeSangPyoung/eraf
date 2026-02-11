package com.eraf.openapi.admin.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.admin.dto.ConsumerRequest;
import com.eraf.openapi.admin.dto.ConsumerResponse;
import com.eraf.openapi.admin.service.ConsumerAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
