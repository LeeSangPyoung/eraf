package com.eraf.jpa.envers;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 엔티티 변경 이력 조회 서비스
 * Hibernate Envers를 사용한 변경 이력 관리
 */
@Service
@Transactional(readOnly = true)
public class EntityRevisionService {

    private final EntityManager entityManager;

    public EntityRevisionService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * AuditReader 생성
     */
    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    /**
     * 특정 리비전의 엔티티 조회
     */
    public <T> T findEntityAtRevision(Class<T> entityClass, Object entityId, Long revisionNumber) {
        return getAuditReader().find(entityClass, entityId, revisionNumber);
    }

    /**
     * 엔티티의 모든 리비전 번호 조회
     */
    public List<Number> getRevisions(Class<?> entityClass, Object entityId) {
        return getAuditReader().getRevisions(entityClass, entityId);
    }

    /**
     * 특정 리비전 정보 조회
     */
    public RevisionEntity getRevisionEntity(Long revisionNumber) {
        return getAuditReader().findRevision(RevisionEntity.class, revisionNumber);
    }

    /**
     * 엔티티의 전체 변경 이력 조회
     */
    @SuppressWarnings("unchecked")
    public <T> List<EntityRevision<T>> getEntityHistory(Class<T> entityClass, Object entityId) {
        AuditReader auditReader = getAuditReader();

        List<Number> revisions = auditReader.getRevisions(entityClass, entityId);

        return revisions.stream()
                .map(revNum -> {
                    T entity = auditReader.find(entityClass, entityId, revNum);
                    RevisionEntity revisionEntity = auditReader.findRevision(RevisionEntity.class, revNum);

                    Object[] queryResult = (Object[]) auditReader.createQuery()
                            .forRevisionsOfEntity(entityClass, false, true)
                            .add(AuditEntity.id().eq(entityId))
                            .add(AuditEntity.revisionNumber().eq(revNum))
                            .getSingleResult();

                    org.hibernate.envers.RevisionType revType =
                            (org.hibernate.envers.RevisionType) queryResult[2];

                    return new EntityRevision<>(entity, revisionEntity, revType);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 시점의 엔티티 조회 (가장 가까운 이전 리비전)
     */
    public <T> T findEntityAtDate(Class<T> entityClass, Object entityId, java.util.Date date) {
        Number revision = getAuditReader().getRevisionNumberForDate(date);
        if (revision == null) {
            return null;
        }
        return getAuditReader().find(entityClass, entityId, revision);
    }

    /**
     * 엔티티의 최신 리비전 조회
     */
    public <T> T findCurrentRevision(Class<T> entityClass, Object entityId) {
        List<Number> revisions = getAuditReader().getRevisions(entityClass, entityId);
        if (revisions.isEmpty()) {
            return null;
        }
        Number lastRevision = revisions.get(revisions.size() - 1);
        return getAuditReader().find(entityClass, entityId, lastRevision);
    }

    /**
     * 두 리비전 간의 변경 사항 조회 (Audit Query 사용)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findChangedEntities(Class<T> entityClass, Long fromRevision, Long toRevision) {
        AuditQuery query = getAuditReader().createQuery()
                .forRevisionsOfEntity(entityClass, false, false)
                .add(AuditEntity.revisionNumber().between(fromRevision, toRevision));

        return query.getResultList();
    }

    /**
     * 특정 사용자가 변경한 엔티티 조회
     */
    @SuppressWarnings("unchecked")
    public <T> List<EntityRevision<T>> findChangedByUser(Class<T> entityClass, String userId) {
        AuditQuery query = getAuditReader().createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.revisionProperty("userId").eq(userId));

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> {
                    T entity = (T) result[0];
                    RevisionEntity revision = (RevisionEntity) result[1];
                    org.hibernate.envers.RevisionType revType =
                            (org.hibernate.envers.RevisionType) result[2];
                    return new EntityRevision<>(entity, revision, revType);
                })
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 변경 이력 DTO
     */
    public static class EntityRevision<T> {
        private final T entity;
        private final RevisionEntity revision;
        private final org.hibernate.envers.RevisionType revisionType;

        public EntityRevision(T entity, RevisionEntity revision,
                            org.hibernate.envers.RevisionType revisionType) {
            this.entity = entity;
            this.revision = revision;
            this.revisionType = revisionType;
        }

        public T getEntity() {
            return entity;
        }

        public RevisionEntity getRevision() {
            return revision;
        }

        public org.hibernate.envers.RevisionType getRevisionType() {
            return revisionType;
        }

        public String getRevisionTypeString() {
            return RevisionListener.getRevisionTypeString(revisionType);
        }
    }
}
