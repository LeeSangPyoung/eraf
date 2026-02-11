package com.eraf.openapi.admin.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.admin.dto.UpstreamRequest;
import com.eraf.openapi.admin.dto.UpstreamResponse;
import com.eraf.openapi.admin.service.UpstreamAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Upstream 관리 REST API
 */
@RestController
@RequestMapping("/admin/upstreams")
@RequiredArgsConstructor
public class UpstreamAdminController {

    private final UpstreamAdminService upstreamAdminService;

    /**
     * 모든 Upstream 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UpstreamResponse>>> getAllUpstreams(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<UpstreamResponse> upstreams = enabledOnly ?
                upstreamAdminService.findAllEnabled() :
                upstreamAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(upstreams));
    }

    /**
     * Upstream 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UpstreamResponse>> getUpstream(@PathVariable Long id) {
        UpstreamResponse upstream = upstreamAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(upstream));
    }

    /**
     * Upstream 상세 조회 (Name)
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<UpstreamResponse>> getUpstreamByName(@PathVariable String name) {
        UpstreamResponse upstream = upstreamAdminService.findByName(name);
        return ResponseEntity.ok(ApiResponse.success(upstream));
    }

    /**
     * Upstream 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UpstreamResponse>> createUpstream(@Valid @RequestBody UpstreamRequest request) {
        UpstreamResponse created = upstreamAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Upstream 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UpstreamResponse>> updateUpstream(
            @PathVariable Long id,
            @Valid @RequestBody UpstreamRequest request) {
        UpstreamResponse updated = upstreamAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Upstream 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUpstream(@PathVariable Long id) {
        upstreamAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Upstream 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<UpstreamResponse>> toggleUpstream(@PathVariable Long id) {
        UpstreamResponse updated = upstreamAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Upstream 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = upstreamAdminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
