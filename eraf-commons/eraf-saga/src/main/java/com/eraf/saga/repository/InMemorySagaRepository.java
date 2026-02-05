package com.eraf.saga.repository;

import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 Saga Repository 구현
 * 개발/테스트 환경용입니다. 프로덕션에서는 JPA 또는 Redis 구현을 사용하세요.
 */
public class InMemorySagaRepository implements SagaRepository {

    private final Map<String, SagaExecution> store = new ConcurrentHashMap<>();

    @Override
    public void save(SagaExecution execution) {
        store.put(execution.getId(), execution);
    }

    @Override
    public Optional<SagaExecution> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<SagaExecution> findByTraceId(String traceId) {
        return store.values().stream()
                .filter(e -> traceId.equals(e.getTraceId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SagaExecution> findBySagaNameAndStatus(String sagaName, SagaStatus status) {
        return store.values().stream()
                .filter(e -> sagaName.equals(e.getSagaName()) && status == e.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public List<SagaExecution> findByStatus(SagaStatus status) {
        return store.values().stream()
                .filter(e -> status == e.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void cleanupOlderThan(long ageMillis) {
        Instant cutoff = Instant.now().minusMillis(ageMillis);
        store.entrySet().removeIf(entry -> {
            SagaExecution execution = entry.getValue();
            if (execution.getCompletedAt() == null) {
                return false;
            }
            return execution.getCompletedAt().isBefore(cutoff) &&
                   (execution.getStatus() == SagaStatus.COMPLETED ||
                    execution.getStatus() == SagaStatus.COMPENSATED ||
                    execution.getStatus() == SagaStatus.FAILED);
        });
    }
}
