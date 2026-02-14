package com.eraf.jpa.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA AttributeConverter for transparent column encryption with key rotation support
 *
 * <p>**NEW: 키 로테이션 지원**</p>
 * <ul>
 *     <li>버전 기반 암호화: 데이터에 키 버전 prefix 자동 추가 (예: "v1:암호문")</li>
 *     <li>이전 키 지원: 키 변경 후에도 기존 데이터 복호화 가능</li>
 *     <li>자동 fallback: 버전 불일치 시 모든 키로 복호화 시도</li>
 * </ul>
 *
 * <p>암호화 키 설정:</p>
 * <pre>
 * 환경변수:
 *   ERAF_ENCRYPTION_KEY=현재키(v1)
 *   ERAF_ENCRYPTION_KEY_V0=이전키1(v0)
 *   ERAF_ENCRYPTION_KEY_V_1=이전키2(v-1)
 *
 * 시스템 속성:
 *   eraf.encryption.key=현재키
 *   eraf.encryption.key.v0=이전키1
 * </pre>
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Entity}
 * public class User {
 *     {@code @Encrypt}
 *     {@code @Column(length = 1000)}
 *     private String socialSecurityNumber;  // 자동으로 v1:암호문 형식으로 저장
 * }
 * </pre>
 *
 * <p>Migration:</p>
 * <ul>
 *     <li>기존 데이터(버전 없음)는 v0로 간주하여 복호화</li>
 *     <li>신규 데이터는 항상 최신 버전(v1)으로 암호화</li>
 *     <li>FieldEncryptionRotationService로 기존 데이터를 최신 버전으로 재암호화 가능</li>
 * </ul>
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(EncryptedStringConverter.class);

    private final EncryptionKeyManager keyManager = EncryptionKeyManager.getInstance();

    /**
     * Entity → Database: 평문을 최신 키로 암호화하여 저장
     * 출력 형식: "v1:BASE64_CIPHERTEXT"
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            String encrypted = keyManager.encrypt(attribute);
            log.debug("[EncryptedStringConverter] Encrypted value (v{}: length {} → {})",
                    keyManager.getCurrentVersion(), attribute.length(), encrypted.length());
            return encrypted;
        } catch (Exception e) {
            log.error("[EncryptedStringConverter] Encryption failed", e);
            throw new IllegalStateException("Failed to encrypt sensitive data", e);
        }
    }

    /**
     * Database → Entity: 버전 기반 복호화 (자동 키 선택)
     * 지원 형식:
     * - "v1:BASE64" → v1 키로 복호화
     * - "v0:BASE64" → v0 키로 복호화
     * - "BASE64" → 레거시 (v0로 간주)
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            int version = keyManager.extractVersion(dbData);
            String decrypted = keyManager.decrypt(dbData);
            log.debug("[EncryptedStringConverter] Decrypted value (v{}: length {} → {})",
                    version, dbData.length(), decrypted != null ? decrypted.length() : 0);
            return decrypted;
        } catch (Exception e) {
            log.error("[EncryptedStringConverter] Decryption failed - possible key mismatch or corrupted data", e);
            throw new IllegalStateException("Failed to decrypt sensitive data", e);
        }
    }
}
