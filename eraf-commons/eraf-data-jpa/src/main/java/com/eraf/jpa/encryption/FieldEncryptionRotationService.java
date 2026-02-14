package com.eraf.jpa.encryption;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 필드 암호화 키 로테이션 서비스
 *
 * <p>주요 기능:</p>
 * <ul>
 *     <li>@Encrypt 필드 자동 검색</li>
 *     <li>이전 키로 암호화된 데이터를 최신 키로 재암호화</li>
 *     <li>배치 처리 (메모리 효율적)</li>
 *     <li>진행 상황 추적</li>
 * </ul>
 *
 * <p>사용 시나리오:</p>
 * <ol>
 *     <li>새 암호화 키 생성 및 배포 (ERAF_ENCRYPTION_KEY 업데이트)</li>
 *     <li>이전 키를 v0으로 설정 (ERAF_ENCRYPTION_KEY_V0)</li>
 *     <li>애플리케이션 재시작 (새 키 로드)</li>
 *     <li>rotateAllEncryptedFields() 호출 → 모든 암호화 필드를 v1로 재암호화</li>
 *     <li>완료 후 v0 키 삭제 가능</li>
 * </ol>
 */
@Service
public class FieldEncryptionRotationService {

    private static final Logger log = LoggerFactory.getLogger(FieldEncryptionRotationService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final EncryptionKeyManager keyManager = EncryptionKeyManager.getInstance();

    /**
     * 모든 @Encrypt 필드를 최신 키로 재암호화
     *
     * @return 재암호화 통계
     */
    @Transactional
    public RotationResult rotateAllEncryptedFields() {
        log.info("[FieldEncryptionRotationService] Starting key rotation for all @Encrypt fields");

        RotationResult result = new RotationResult();
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entityType : entities) {
            Class<?> javaType = entityType.getJavaType();
            List<String> encryptedFields = findEncryptedFields(javaType);

            if (!encryptedFields.isEmpty()) {
                log.info("[FieldEncryptionRotationService] Processing entity: {} with {} encrypted field(s)",
                        javaType.getSimpleName(), encryptedFields.size());

                RotationResult entityResult = rotateEntity(javaType, encryptedFields);
                result.merge(entityResult);
            }
        }

        log.info("[FieldEncryptionRotationService] Key rotation completed: {}", result);
        return result;
    }

    /**
     * 특정 엔티티의 암호화 필드 재암호화
     *
     * @param entityClass      엔티티 클래스
     * @param encryptedFields  암호화된 필드 목록
     * @return 재암호화 통계
     */
    @Transactional
    public RotationResult rotateEntity(Class<?> entityClass, List<String> encryptedFields) {
        RotationResult result = new RotationResult();
        result.entityClass = entityClass.getSimpleName();

        String entityName = entityClass.getSimpleName();
        String idField = "id"; // 기본 ID 필드명 (대부분의 엔티티가 사용)

        try {
            // 모든 엔티티 조회 (배치 처리)
            Query query = entityManager.createQuery("SELECT e FROM " + entityName + " e");
            List<?> entities = query.getResultList();

            for (Object entity : entities) {
                try {
                    boolean updated = rotateEntityFields(entity, encryptedFields);
                    if (updated) {
                        entityManager.merge(entity);
                        result.reEncryptedCount++;
                    } else {
                        result.skippedCount++; // 이미 최신 버전
                    }
                } catch (Exception e) {
                    log.error("[FieldEncryptionRotationService] Failed to rotate entity: {}", entity, e);
                    result.failedCount++;
                }
            }

            entityManager.flush();
            log.info("[FieldEncryptionRotationService] Entity {} rotation completed: {}", entityName, result);

        } catch (Exception e) {
            log.error("[FieldEncryptionRotationService] Failed to rotate entity class: {}", entityClass, e);
            result.failedCount++;
        }

        return result;
    }

    /**
     * 단일 엔티티의 암호화 필드 재암호화
     *
     * @param entity           엔티티 인스턴스
     * @param encryptedFields  암호화된 필드 목록
     * @return true if any field was re-encrypted
     */
    private boolean rotateEntityFields(Object entity, List<String> encryptedFields) throws Exception {
        boolean updated = false;

        for (String fieldName : encryptedFields) {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            String encryptedValue = (String) field.get(entity);
            if (encryptedValue == null) {
                continue; // NULL 필드는 스킵
            }

            // 이미 최신 키로 암호화된 경우 스킵
            if (keyManager.isEncryptedWithCurrentKey(encryptedValue)) {
                continue;
            }

            // 재암호화 (이전 키로 복호화 → 최신 키로 암호화)
            String reEncrypted = keyManager.reEncrypt(encryptedValue);
            field.set(entity, reEncrypted);
            updated = true;

            log.debug("[FieldEncryptionRotationService] Re-encrypted field {}.{} (v{} → v{})",
                    entity.getClass().getSimpleName(), fieldName,
                    keyManager.extractVersion(encryptedValue),
                    keyManager.getCurrentVersion());
        }

        return updated;
    }

    /**
     * 엔티티에서 @Encrypt 어노테이션이 붙은 필드 검색
     */
    private List<String> findEncryptedFields(Class<?> clazz) {
        List<String> encryptedFields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Encrypt.class)) {
                encryptedFields.add(field.getName());
            }
        }

        return encryptedFields;
    }

    /**
     * 특정 엔티티 타입의 암호화 상태 조회
     *
     * @param entityClass 엔티티 클래스
     * @return 암호화 버전별 통계
     */
    public EncryptionStatusSummary getEncryptionStatus(Class<?> entityClass) {
        EncryptionStatusSummary summary = new EncryptionStatusSummary();
        summary.entityClass = entityClass.getSimpleName();

        List<String> encryptedFields = findEncryptedFields(entityClass);
        if (encryptedFields.isEmpty()) {
            return summary; // 암호화 필드 없음
        }

        String entityName = entityClass.getSimpleName();
        Query query = entityManager.createQuery("SELECT e FROM " + entityName + " e");
        List<?> entities = query.getResultList();

        for (Object entity : entities) {
            for (String fieldName : encryptedFields) {
                try {
                    Field field = entity.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    String encryptedValue = (String) field.get(entity);

                    if (encryptedValue == null) {
                        summary.nullCount++;
                        continue;
                    }

                    int version = keyManager.extractVersion(encryptedValue);
                    if (version == keyManager.getCurrentVersion()) {
                        summary.currentVersionCount++;
                    } else {
                        summary.oldVersionCount++;
                    }
                } catch (Exception e) {
                    log.error("[FieldEncryptionRotationService] Failed to check encryption status", e);
                }
            }
        }

        return summary;
    }

    /**
     * 재암호화 결과
     */
    public static class RotationResult {
        private String entityClass;
        private int reEncryptedCount = 0;  // 재암호화된 엔티티 수
        private int skippedCount = 0;      // 이미 최신 버전 (스킵)
        private int failedCount = 0;       // 실패한 엔티티 수

        public void merge(RotationResult other) {
            this.reEncryptedCount += other.reEncryptedCount;
            this.skippedCount += other.skippedCount;
            this.failedCount += other.failedCount;
        }

        @Override
        public String toString() {
            return String.format("RotationResult{entity=%s, reEncrypted=%d, skipped=%d, failed=%d}",
                    entityClass, reEncryptedCount, skippedCount, failedCount);
        }

        // Getters
        public String getEntityClass() { return entityClass; }
        public int getReEncryptedCount() { return reEncryptedCount; }
        public int getSkippedCount() { return skippedCount; }
        public int getFailedCount() { return failedCount; }
    }

    /**
     * 암호화 상태 요약
     */
    public static class EncryptionStatusSummary {
        private String entityClass;
        private int currentVersionCount = 0;  // 최신 버전으로 암호화된 필드 수
        private int oldVersionCount = 0;      // 이전 버전으로 암호화된 필드 수
        private int nullCount = 0;            // NULL 필드 수

        @Override
        public String toString() {
            return String.format("EncryptionStatus{entity=%s, current=%d, old=%d, null=%d}",
                    entityClass, currentVersionCount, oldVersionCount, nullCount);
        }

        // Getters
        public String getEntityClass() { return entityClass; }
        public int getCurrentVersionCount() { return currentVersionCount; }
        public int getOldVersionCount() { return oldVersionCount; }
        public int getNullCount() { return nullCount; }
        public int getTotal() { return currentVersionCount + oldVersionCount + nullCount; }
        public double getRotationProgress() {
            int total = currentVersionCount + oldVersionCount;
            return total > 0 ? (double) currentVersionCount / total * 100 : 100.0;
        }
    }
}
