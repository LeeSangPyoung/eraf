package com.eraf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * HashiCorp Vault 시크릿 조회 서비스
 *
 * <p>Spring Cloud Vault의 {@link VaultTemplate}을 사용하여 Vault에 저장된
 * 시크릿(비밀 설정값)을 안전하게 조회합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * // 단일 값 조회
 * String dbPassword = vaultConfigService.getSecret("database/prod", "password")
 *     .orElseThrow(() -> new IllegalStateException("DB password not found in Vault"));
 *
 * // 경로 전체 조회
 * Map&lt;String, Object&gt; secrets = vaultConfigService.getSecrets("database/prod");
 * </pre>
 *
 * <h3>설정 예시 (application.yml)</h3>
 * <pre>
 * eraf:
 *   config:
 *     cloud:
 *       vault:
 *         enabled: true
 *         uri: http://vault-server:8200
 *         token: ${VAULT_TOKEN}   # 환경변수 권장
 *         backend: secret         # KV secrets engine mount path
 * </pre>
 */
public class VaultConfigService {

    private static final Logger log = LoggerFactory.getLogger(VaultConfigService.class);

    private final VaultTemplate vaultTemplate;
    private final CloudConfigProperties.VaultConfig vaultConfig;

    public VaultConfigService(VaultTemplate vaultTemplate, CloudConfigProperties.VaultConfig vaultConfig) {
        this.vaultTemplate = vaultTemplate;
        this.vaultConfig = vaultConfig;
    }

    /**
     * Vault KV 경로에서 전체 시크릿 맵 조회
     *
     * @param path KV 경로 (예: "database/prod", "app/payment")
     * @return 시크릿 key-value 맵. 경로가 없으면 빈 맵 반환
     */
    public Map<String, Object> getSecrets(String path) {
        String fullPath = buildPath(path);
        try {
            VaultResponse response = vaultTemplate.read(fullPath);
            if (response == null) {
                log.debug("No secrets found at Vault path: {}", fullPath);
                return Collections.emptyMap();
            }
            Map<String, Object> data = response.getData();
            if (data == null) {
                log.debug("No secrets found at Vault path: {}", fullPath);
                return Collections.emptyMap();
            }
            log.debug("Retrieved {} secrets from Vault path: {}", data.size(), fullPath);
            return data;
        } catch (Exception e) {
            log.error("Failed to read secrets from Vault path '{}': {}", fullPath, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Vault KV 경로에서 특정 키의 시크릿 값 조회
     *
     * @param path KV 경로 (예: "database/prod")
     * @param key  조회할 키 이름 (예: "password")
     * @return 시크릿 값 Optional. 없으면 empty 반환
     */
    public Optional<String> getSecret(String path, String key) {
        Map<String, Object> secrets = getSecrets(path);
        Object value = secrets.get(key);
        if (value == null) {
            log.debug("Key '{}' not found in Vault path: {}", key, path);
            return Optional.empty();
        }
        return Optional.of(String.valueOf(value));
    }

    /**
     * Vault KV 경로에서 특정 키의 시크릿 값 조회 (기본값 포함)
     *
     * @param path         KV 경로
     * @param key          조회할 키 이름
     * @param defaultValue 값이 없을 때 반환할 기본값
     * @return 시크릿 값 또는 기본값
     */
    public String getSecret(String path, String key, String defaultValue) {
        return getSecret(path, key).orElse(defaultValue);
    }

    /**
     * Vault KV 경로에 시크릿 저장 (KV v1)
     *
     * @param path    저장할 KV 경로
     * @param secrets 저장할 시크릿 맵
     */
    public void writeSecrets(String path, Map<String, Object> secrets) {
        String fullPath = buildPath(path);
        try {
            vaultTemplate.write(fullPath, secrets);
            log.info("Successfully wrote {} secrets to Vault path: {}", secrets.size(), fullPath);
        } catch (Exception e) {
            log.error("Failed to write secrets to Vault path '{}': {}", fullPath, e.getMessage());
            throw new VaultOperationException("Failed to write secrets to path: " + path, e);
        }
    }

    /**
     * Vault KV 경로의 시크릿 삭제
     *
     * @param path 삭제할 KV 경로
     */
    public void deleteSecrets(String path) {
        String fullPath = buildPath(path);
        try {
            vaultTemplate.delete(fullPath);
            log.info("Successfully deleted secrets at Vault path: {}", fullPath);
        } catch (Exception e) {
            log.error("Failed to delete secrets at Vault path '{}': {}", fullPath, e.getMessage());
            throw new VaultOperationException("Failed to delete secrets at path: " + path, e);
        }
    }

    /**
     * Vault 연결 상태 확인
     *
     * @return Vault가 정상 응답하면 true
     */
    public boolean isHealthy() {
        try {
            vaultTemplate.opsForSys().health();
            return true;
        } catch (Exception e) {
            log.warn("Vault health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * backend + path를 조합하여 전체 Vault 경로 생성
     * 예: backend="secret", path="database/prod" → "secret/database/prod"
     */
    private String buildPath(String path) {
        String backend = vaultConfig.getBackend() != null ? vaultConfig.getBackend() : "secret";
        if (path.startsWith(backend + "/")) {
            return path;
        }
        return backend + "/" + path;
    }

    /**
     * Vault 작업 실패 시 던지는 예외
     */
    public static class VaultOperationException extends RuntimeException {
        public VaultOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
