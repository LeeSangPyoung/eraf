package com.eraf.openapi.admin.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.admin.dto.ApiRequest;
import com.eraf.openapi.admin.service.ApiAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API 관리 REST API
 */
@RestController
@RequestMapping("/admin/apis")
@RequiredArgsConstructor
public class ApiAdminController {

    private final ApiAdminService apiAdminService;

    /**
     * 모든 API 조회
     *
     * Query Parameters:
     * - enabledOnly: 활성화된 API만 조회 (default: false)
     * - serviceId: 특정 서비스의 API만 조회
     * - routeId: 특정 라우트의 API만 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<com.eraf.openapi.admin.dto.ApiResponse>>> getAllApis(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long routeId) {

        List<com.eraf.openapi.admin.dto.ApiResponse> apis;

        if (serviceId != null) {
            apis = apiAdminService.findByServiceId(serviceId);
        } else if (routeId != null) {
            apis = apiAdminService.findByRouteId(routeId);
        } else if (enabledOnly) {
            apis = apiAdminService.findAllEnabled();
        } else {
            apis = apiAdminService.findAll();
        }

        return ResponseEntity.ok(ApiResponse.success(apis));
    }

    /**
     * API 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(apiAdminService.getStats()));
    }

    /**
     * 경로와 메서드로 API 조회
     */
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<com.eraf.openapi.admin.dto.ApiResponse>> lookupApi(
            @RequestParam String path,
            @RequestParam String method) {
        com.eraf.openapi.admin.dto.ApiResponse api = apiAdminService.findByPathAndMethod(path, method);
        if (api == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(api));
    }

    /**
     * Service ID로 API 조회
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<com.eraf.openapi.admin.dto.ApiResponse>>> getApisByService(@PathVariable Long serviceId) {
        List<com.eraf.openapi.admin.dto.ApiResponse> apis = apiAdminService.findByServiceId(serviceId);
        return ResponseEntity.ok(ApiResponse.success(apis));
    }

    /**
     * Route ID로 API 조회
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<com.eraf.openapi.admin.dto.ApiResponse>>> getApisByRoute(@PathVariable Long routeId) {
        List<com.eraf.openapi.admin.dto.ApiResponse> apis = apiAdminService.findByRouteId(routeId);
        return ResponseEntity.ok(ApiResponse.success(apis));
    }

    /**
     * API 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<com.eraf.openapi.admin.dto.ApiResponse>> getApi(@PathVariable Long id) {
        com.eraf.openapi.admin.dto.ApiResponse api = apiAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(api));
    }

    /**
     * API 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<com.eraf.openapi.admin.dto.ApiResponse>> createApi(@Valid @RequestBody ApiRequest request) {
        com.eraf.openapi.admin.dto.ApiResponse created = apiAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * API 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<com.eraf.openapi.admin.dto.ApiResponse>> updateApi(
            @PathVariable Long id,
            @Valid @RequestBody ApiRequest request) {
        com.eraf.openapi.admin.dto.ApiResponse updated = apiAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * API 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApi(@PathVariable Long id) {
        apiAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * API 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<com.eraf.openapi.admin.dto.ApiResponse>> toggleApiEnabled(@PathVariable Long id) {
        com.eraf.openapi.admin.dto.ApiResponse updated = apiAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
