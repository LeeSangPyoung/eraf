package com.eraf.starter.database.audit;

import com.eraf.core.logging.AuditLogStore;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA 기반 감사 로그 저장소 구현
 */
public class JpaAuditLogStore implements AuditLogStore {

    private final AuditLogJpaRepository jpaRepository;

    public JpaAuditLogStore(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(AuditLogEntry entry) {
        AuditLogEntity entity = toEntity(entry);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<AuditLogEntry> findById(Long id) {
        return jpaRepository.findById(id).map(this::toEntry);
    }

    @Override
    public List<AuditLogEntry> findByUserId(String userId, int limit) {
        return jpaRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogEntry> findByResource(String resource, String resourceId, int limit) {
        if (resourceId != null && !resourceId.isEmpty()) {
            return jpaRepository.findByResourceAndResourceIdOrderByTimestampDesc(
                            resource, resourceId, PageRequest.of(0, limit))
                    .stream()
                    .map(this::toEntry)
                    .collect(Collectors.toList());
        } else {
            return jpaRepository.findByResourceOrderByTimestampDesc(resource, PageRequest.of(0, limit))
                    .stream()
                    .map(this::toEntry)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<AuditLogEntry> findByPeriod(Instant from, Instant to, int limit) {
        return jpaRepository.findByPeriod(from, to, PageRequest.of(0, limit))
                .stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogEntry> findByAction(String action, int limit) {
        return jpaRepository.findByActionOrderByTimestampDesc(action, PageRequest.of(0, limit))
                .stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private AuditLogEntity toEntity(AuditLogEntry entry) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setTimestamp(entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now());
        entity.setTraceId(entry.getTraceId());
        entity.setUserId(entry.getUserId());
        entity.setUsername(entry.getUsername());
        entity.setClientIp(entry.getClientIp());
        entity.setAction(entry.getAction());
        entity.setResource(entry.getResource());
        entity.setResourceId(entry.getResourceId());
        entity.setResult(entry.getResult());
        entity.setDetails(entry.getDetails());
        return entity;
    }

    private AuditLogEntry toEntry(AuditLogEntity entity) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setId(entity.getId());
        entry.setTimestamp(entity.getTimestamp());
        entry.setTraceId(entity.getTraceId());
        entry.setUserId(entity.getUserId());
        entry.setUsername(entity.getUsername());
        entry.setClientIp(entity.getClientIp());
        entry.setAction(entity.getAction());
        entry.setResource(entity.getResource());
        entry.setResourceId(entity.getResourceId());
        entry.setResult(entity.getResult());
        entry.setDetails(entity.getDetails());
        return entry;
    }
}
