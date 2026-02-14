package com.eraf.jpa.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 암호화 키 관리자 (키 버전 관리 + 로테이션 지원)
 *
 * <p>키 버전 관리:</p>
 * <ul>
 *     <li>현재 키(v1): 새로운 암호화에 사용</li>
 *     <li>이전 키들(v0, v-1, ...): 복호화 전용 (키 로테이션 후 기존 데이터 읽기용)</li>
 * </ul>
 *
 * <p>키 설정 방법:</p>
 * <pre>
 * 환경변수:
 *   ERAF_ENCRYPTION_KEY=현재키        # v1 (최신)
 *   ERAF_ENCRYPTION_KEY_V0=이전키1     # v0
 *   ERAF_ENCRYPTION_KEY_V_1=이전키2    # v-1
 *
 * 시스템 속성:
 *   eraf.encryption.key=현재키
 *   eraf.encryption.key.v0=이전키1
 *   eraf.encryption.key.v_1=이전키2
 * </pre>
 *
 * <p>암호화 데이터 형식:</p>
 * <pre>
 * v1:BASE64_CIPHERTEXT     # 버전 v1로 암호화된 데이터
 * v0:BASE64_CIPHERTEXT     # 버전 v0로 암호화된 데이터
 * BASE64_CIPHERTEXT        # 레거시 (버전 없음, v0로 간주)
 * </pre>
 */
public class EncryptionKeyManager {

    private static final Logger log = LoggerFactory.getLogger(EncryptionKeyManager.class);

    private static final String ENV_KEY_PREFIX = "ERAF_ENCRYPTION_KEY";
    private static final String PROPERTY_KEY_PREFIX = "eraf.encryption.key";
    private static final String DEFAULT_KEY = "ERAF_DEFAULT_ENCRYPTION_KEY_CHANGE_ME_IN_PRODUCTION";

    private static final String VERSION_SEPARATOR = ":";
    private static final int CURRENT_VERSION = 1;

    // 키 저장소: version → key
    private final Map<Integer, String> keys = new ConcurrentHashMap<>();
    private volatile int currentVersion = CURRENT_VERSION;

    /**
     * 싱글톤 인스턴스 (JVM 전역)
     */
    private static final EncryptionKeyManager INSTANCE = new EncryptionKeyManager();

    public static EncryptionKeyManager getInstance() {
        return INSTANCE;
    }

    private EncryptionKeyManager() {
        loadKeys();
    }

    /**
     * 환경변수 및 시스템 속성에서 키 로드
     */
    private void loadKeys() {
        // 현재 키 로드 (v1)
        String currentKey = loadKey("", ENV_KEY_PREFIX, PROPERTY_KEY_PREFIX);
        if (currentKey == null || currentKey.isBlank()) {
            currentKey = DEFAULT_KEY;
            log.warn("[EncryptionKeyManager] Using DEFAULT encryption key for v{}. " +
                    "Set {} environment variable or {} system property for production!",
                    currentVersion, ENV_KEY_PREFIX, PROPERTY_KEY_PREFIX);
        } else {
            log.info("[EncryptionKeyManager] Current encryption key (v{}) loaded from environment/property",
                    currentVersion);
        }
        keys.put(currentVersion, currentKey);

        // 이전 키들 로드 (v0, v-1, v-2, ...)
        for (int version = 0; version >= -10; version--) {
            String versionSuffix = version >= 0 ? "_V" + version : "_V_" + Math.abs(version);
            String key = loadKey(versionSuffix, ENV_KEY_PREFIX + versionSuffix, PROPERTY_KEY_PREFIX + ".v" + version);
            if (key != null && !key.isBlank()) {
                keys.put(version, key);
                log.info("[EncryptionKeyManager] Historical encryption key (v{}) loaded for decryption", version);
            }
        }

        log.info("[EncryptionKeyManager] Initialized with {} key version(s): {}", keys.size(), keys.keySet());
    }

    /**
     * 환경변수 → 시스템 속성 순으로 키 로드
     */
    private String loadKey(String suffix, String envKey, String propertyKey) {
        String key = System.getenv(envKey);
        if (key == null || key.isBlank()) {
            key = System.getProperty(propertyKey);
        }
        return key;
    }

    /**
     * 현재 키로 암호화 (항상 최신 버전 사용)
     *
     * @param plainText 평문
     * @return "v1:BASE64_CIPHERTEXT" 형식의 버전 포함 암호문
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        String key = keys.get(currentVersion);
        if (key == null) {
            throw new IllegalStateException("Current encryption key (v" + currentVersion + ") not found");
        }

        try {
            String encrypted = com.eraf.crypto.Crypto.encrypt(plainText, key);
            return "v" + currentVersion + VERSION_SEPARATOR + encrypted;
        } catch (Exception e) {
            log.error("[EncryptionKeyManager] Encryption failed with v{}", currentVersion, e);
            throw new IllegalStateException("Failed to encrypt with key v" + currentVersion, e);
        }
    }

    /**
     * 버전 기반 복호화 (자동 키 선택)
     *
     * @param versionedCipherText "v1:BASE64" 또는 "BASE64" (레거시)
     * @return 평문
     */
    public String decrypt(String versionedCipherText) {
        if (versionedCipherText == null) {
            return null;
        }

        // 버전 파싱
        int version;
        String cipherText;

        if (versionedCipherText.contains(VERSION_SEPARATOR)) {
            String[] parts = versionedCipherText.split(VERSION_SEPARATOR, 2);
            String versionStr = parts[0].substring(1); // "v1" → "1"
            version = Integer.parseInt(versionStr);
            cipherText = parts[1];
        } else {
            // 레거시 데이터 (버전 없음) → v0로 간주
            version = 0;
            cipherText = versionedCipherText;
        }

        // 해당 버전의 키로 복호화
        String key = keys.get(version);
        if (key == null) {
            // 키가 없으면 모든 키로 시도 (fallback)
            log.warn("[EncryptionKeyManager] Key for v{} not found, trying all available keys", version);
            return decryptWithAnyKey(cipherText);
        }

        try {
            return com.eraf.crypto.Crypto.decrypt(cipherText, key);
        } catch (Exception e) {
            log.error("[EncryptionKeyManager] Decryption failed with v{}, trying fallback", version, e);
            return decryptWithAnyKey(cipherText);
        }
    }

    /**
     * 모든 키로 복호화 시도 (Fallback)
     */
    private String decryptWithAnyKey(String cipherText) {
        for (Map.Entry<Integer, String> entry : keys.entrySet()) {
            try {
                String decrypted = com.eraf.crypto.Crypto.decrypt(cipherText, entry.getValue());
                log.info("[EncryptionKeyManager] Successfully decrypted with fallback key v{}", entry.getKey());
                return decrypted;
            } catch (Exception ignored) {
                // 다음 키 시도
            }
        }
        throw new IllegalStateException("Failed to decrypt with any available key");
    }

    /**
     * 데이터가 최신 키로 암호화되었는지 확인
     */
    public boolean isEncryptedWithCurrentKey(String versionedCipherText) {
        if (versionedCipherText == null) {
            return false;
        }

        if (versionedCipherText.startsWith("v" + currentVersion + VERSION_SEPARATOR)) {
            return true;
        }

        return false; // 레거시 또는 이전 버전
    }

    /**
     * 데이터 재암호화 (이전 키 → 현재 키)
     *
     * @param versionedCipherText 이전 버전으로 암호화된 데이터
     * @return 최신 버전으로 재암호화된 데이터
     */
    public String reEncrypt(String versionedCipherText) {
        if (versionedCipherText == null) {
            return null;
        }

        if (isEncryptedWithCurrentKey(versionedCipherText)) {
            return versionedCipherText; // 이미 최신 버전
        }

        // 복호화 → 재암호화
        String plainText = decrypt(versionedCipherText);
        return encrypt(plainText);
    }

    /**
     * 암호화 데이터에서 버전 추출
     */
    public int extractVersion(String versionedCipherText) {
        if (versionedCipherText == null) {
            return -1;
        }

        if (versionedCipherText.contains(VERSION_SEPARATOR)) {
            String versionStr = versionedCipherText.substring(1, versionedCipherText.indexOf(VERSION_SEPARATOR));
            try {
                return Integer.parseInt(versionStr);
            } catch (NumberFormatException e) {
                return 0; // 레거시
            }
        }

        return 0; // 레거시 (버전 없음)
    }

    /**
     * 현재 키 버전 조회
     */
    public int getCurrentVersion() {
        return currentVersion;
    }

    /**
     * 로드된 키 버전 목록 조회
     */
    public Set<Integer> getAvailableVersions() {
        return new HashSet<>(keys.keySet());
    }

    /**
     * 키 재로드 (환경변수/시스템 속성 변경 후)
     */
    public synchronized void reload() {
        keys.clear();
        loadKeys();
        log.info("[EncryptionKeyManager] Keys reloaded");
    }

    /**
     * 테스트용: 키 수동 설정
     */
    public void setKeyForTesting(int version, String key) {
        keys.put(version, key);
        if (version > currentVersion) {
            currentVersion = version;
        }
        log.debug("[EncryptionKeyManager] Test key set for v{}", version);
    }
}
