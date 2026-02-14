package com.eraf.jpa.retention;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 데이터 보존 정책에 따라 만료된 데이터를 자동 삭제하는 서비스
 */
@Service
public class DataRetentionService {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionService.class);

    @PersistenceContext
    private final EntityManager entityManager;

    public DataRetentionService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * 매일 새벽 3시에 만료된 데이터 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredData() {
        log.info("Starting data retention cleanup...");

        var metamodel = entityManager.getMetamodel();
        var entities = metamodel.getEntities();

        for (var entityType : entities) {
            var javaType = entityType.getJavaType();
            var annotation = javaType.getAnnotation(DataRetentionPolicy.class);

            if (annotation != null) {
                cleanupEntity(entityType.getName(), annotation);
            }
        }

        log.info("Data retention cleanup completed");
    }

    /**
     * 특정 Entity의 만료된 데이터 삭제
     */
    private void cleanupEntity(String entityName, DataRetentionPolicy policy) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(policy.days());
            String dateField = policy.dateField();

            String jpql = String.format(
                "DELETE FROM %s e WHERE e.%s < :cutoffDate",
                entityName,
                dateField
            );

            int deletedCount = entityManager.createQuery(jpql)
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();

            if (deletedCount > 0) {
                log.info("Deleted {} expired records from {} (retention: {} days)",
                        deletedCount, entityName, policy.days());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup entity: " + entityName, e);
        }
    }
}
