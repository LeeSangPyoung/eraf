package com.eraf.jpa.encryption;

import com.eraf.response.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 암호화 키 로테이션 Admin REST API
 *
 * <p>주요 기능:</p>
 * <ul>
 *     <li>키 버전 정보 조회</li>
 *     <li>암호화 상태 확인 (엔티티별)</li>
 *     <li>키 로테이션 실행</li>
 *     <li>로테이션 진행 상황 추적</li>
 * </ul>
 */
@RestController
@RequestMapping("/admin/encryption")
@ConditionalOnProperty(name = "eraf.encryption.admin-enabled", havingValue = "true", matchIfMissing = true)
public class EncryptionKeyRotationAdminController {

    private final FieldEncryptionRotationService rotationService;
    private final EncryptionKeyManager keyManager = EncryptionKeyManager.getInstance();

    public EncryptionKeyRotationAdminController(FieldEncryptionRotationService rotationService) {
        this.rotationService = rotationService;
    }

    /**
     * 암호화 키 버전 정보 조회
     * GET /admin/encryption/key-info
     */
    @GetMapping("/key-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKeyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("currentVersion", keyManager.getCurrentVersion());
        info.put("availableVersions", keyManager.getAvailableVersions());
        info.put("versionCount", keyManager.getAvailableVersions().size());

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 전체 엔티티 키 로테이션 실행
     * POST /admin/encryption/rotate-all
     */
    @PostMapping("/rotate-all")
    public ResponseEntity<ApiResponse<FieldEncryptionRotationService.RotationResult>> rotateAll() {
        FieldEncryptionRotationService.RotationResult result = rotationService.rotateAllEncryptedFields();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 특정 엔티티의 암호화 상태 조회
     * GET /admin/encryption/status/{entityClassName}
     */
    @GetMapping("/status/{entityClassName}")
    public ResponseEntity<ApiResponse<FieldEncryptionRotationService.EncryptionStatusSummary>> getStatus(
            @PathVariable String entityClassName) {

        try {
            Class<?> entityClass = Class.forName(entityClassName);
            FieldEncryptionRotationService.EncryptionStatusSummary summary =
                    rotationService.getEncryptionStatus(entityClass);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 키 매니저 재로드
     * POST /admin/encryption/reload-keys
     */
    @PostMapping("/reload-keys")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reloadKeys() {
        keyManager.reload();

        Map<String, Object> info = new HashMap<>();
        info.put("currentVersion", keyManager.getCurrentVersion());
        info.put("availableVersions", keyManager.getAvailableVersions());
        info.put("message", "Keys reloaded successfully");

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 암호화 키 로테이션 통계 조회
     * GET /admin/encryption/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Set<Integer> versions = keyManager.getAvailableVersions();

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentVersion", keyManager.getCurrentVersion());
        stats.put("totalKeyVersions", versions.size());
        stats.put("availableVersions", versions);
        stats.put("supportsRotation", versions.size() > 1);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 단일 데이터 재암호화 테스트
     * POST /admin/encryption/test-reencrypt
     * Body: {"cipherText": "v0:BASE64..."}
     */
    @PostMapping("/test-reencrypt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testReencrypt(
            @RequestBody Map<String, String> request) {

        String cipherText = request.get("cipherText");
        if (cipherText == null || cipherText.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            int oldVersion = keyManager.extractVersion(cipherText);
            boolean isCurrentKey = keyManager.isEncryptedWithCurrentKey(cipherText);
            String reEncrypted = keyManager.reEncrypt(cipherText);
            int newVersion = keyManager.extractVersion(reEncrypted);

            Map<String, Object> result = new HashMap<>();
            result.put("originalVersion", oldVersion);
            result.put("wasCurrentKey", isCurrentKey);
            result.put("newVersion", newVersion);
            result.put("reEncrypted", reEncrypted.length() > 50 ?
                    reEncrypted.substring(0, 50) + "..." : reEncrypted);
            result.put("success", true);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(error));
        }
    }
}
