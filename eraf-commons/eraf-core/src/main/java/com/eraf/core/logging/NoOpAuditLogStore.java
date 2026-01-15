package com.eraf.core.logging;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * No-Op 감사 로그 저장소 (로깅만, DB 저장 없음)
 * 기본 구현체
 */
public class NoOpAuditLogStore implements AuditLogStore {

    @Override
    public void save(AuditLogEntry entry) {
        // No-op: 로깅은 AuditLogger에서 처리
    }

    @Override
    public Optional<AuditLogEntry> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<AuditLogEntry> findByUserId(String userId, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<AuditLogEntry> findByResource(String resource, String resourceId, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<AuditLogEntry> findByPeriod(Instant from, Instant to, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<AuditLogEntry> findByAction(String action, int limit) {
        return Collections.emptyList();
    }
}
