package com.eraf.openapi.admin.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.admin.dto.ServiceRequest;
import com.eraf.openapi.admin.dto.ServiceResponse;
import com.eraf.openapi.admin.service.ServiceAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Service 관리 REST API
 */
@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class ServiceAdminController {

    private final ServiceAdminService serviceAdminService;

    /**
     * 모든 Service 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<ServiceResponse> services = enabledOnly ?
                serviceAdminService.findAllEnabled() :
                serviceAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /**
     * Service 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getService(@PathVariable Long id) {
        ServiceResponse service = serviceAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    /**
     * Service 상세 조회 (이름)
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceByName(@PathVariable String name) {
        ServiceResponse service = serviceAdminService.findByName(name);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    /**
     * Service 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse created = serviceAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Service 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse updated = serviceAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Service 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Service 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ServiceResponse>> toggleServiceEnabled(@PathVariable Long id) {
        ServiceResponse updated = serviceAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Service 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStats() {
        return ResponseEntity.ok(ApiResponse.success(serviceAdminService.getStats()));
    }
}
