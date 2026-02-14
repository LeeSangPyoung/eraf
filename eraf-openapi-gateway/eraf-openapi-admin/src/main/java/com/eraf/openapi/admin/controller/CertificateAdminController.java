package com.eraf.openapi.admin.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.admin.dto.CertificateRequest;
import com.eraf.openapi.admin.dto.CertificateResponse;
import com.eraf.openapi.admin.service.CertificateAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Certificate 관리 REST API
 */
@RestController
@RequestMapping("/admin/certificates")
@RequiredArgsConstructor
public class CertificateAdminController {

    private final CertificateAdminService certificateAdminService;

    /**
     * 모든 Certificate 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getAllCertificates(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<CertificateResponse> certificates = enabledOnly ?
                certificateAdminService.findAllEnabled() :
                certificateAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    /**
     * Certificate 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificate(@PathVariable Long id) {
        CertificateResponse certificate = certificateAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(certificate));
    }

    /**
     * Certificate 상세 조회 (Name)
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificateByName(@PathVariable String name) {
        CertificateResponse certificate = certificateAdminService.findByName(name);
        return ResponseEntity.ok(ApiResponse.success(certificate));
    }

    /**
     * Certificate 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CertificateResponse>> createCertificate(@Valid @RequestBody CertificateRequest request) {
        CertificateResponse created = certificateAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Certificate 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateResponse>> updateCertificate(
            @PathVariable Long id,
            @Valid @RequestBody CertificateRequest request) {
        CertificateResponse updated = certificateAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Certificate 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(@PathVariable Long id) {
        certificateAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Certificate 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CertificateResponse>> toggleCertificate(@PathVariable Long id) {
        CertificateResponse updated = certificateAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 만료 임박 Certificate 조회
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getExpiringSoon() {
        List<CertificateResponse> certificates = certificateAdminService.findExpiringSoon();
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    /**
     * 만료된 Certificate 조회
     */
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getExpired() {
        List<CertificateResponse> certificates = certificateAdminService.findExpired();
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    /**
     * Certificate 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = certificateAdminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
