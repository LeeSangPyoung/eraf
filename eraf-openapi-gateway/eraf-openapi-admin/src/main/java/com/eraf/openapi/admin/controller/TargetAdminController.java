package com.eraf.openapi.admin.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.admin.dto.TargetRequest;
import com.eraf.openapi.admin.dto.TargetResponse;
import com.eraf.openapi.admin.service.TargetAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Target 관리 REST API
 */
@RestController
@RequestMapping("/admin/targets")
@RequiredArgsConstructor
public class TargetAdminController {

    private final TargetAdminService targetAdminService;

    /**
     * 모든 Target 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TargetResponse>>> getAllTargets(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<TargetResponse> targets = enabledOnly ?
                targetAdminService.findAllEnabled() :
                targetAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(targets));
    }

    /**
     * Target 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TargetResponse>> getTarget(@PathVariable Long id) {
        TargetResponse target = targetAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(target));
    }

    /**
     * Service ID로 Target 조회
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<TargetResponse>>> getTargetsByService(
            @PathVariable Long serviceId,
            @RequestParam(required = false, defaultValue = "false") boolean healthyOnly) {
        List<TargetResponse> targets = healthyOnly ?
                targetAdminService.findHealthyTargetsByServiceId(serviceId) :
                targetAdminService.findByServiceId(serviceId);
        return ResponseEntity.ok(ApiResponse.success(targets));
    }

    /**
     * Target 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TargetResponse>> createTarget(@Valid @RequestBody TargetRequest request) {
        TargetResponse created = targetAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Target 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TargetResponse>> updateTarget(
            @PathVariable Long id,
            @Valid @RequestBody TargetRequest request) {
        TargetResponse updated = targetAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Target 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTarget(@PathVariable Long id) {
        targetAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Target 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<TargetResponse>> toggleTargetEnabled(@PathVariable Long id) {
        TargetResponse updated = targetAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Target 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTargetStats() {
        return ResponseEntity.ok(ApiResponse.success(targetAdminService.getStats()));
    }
}
