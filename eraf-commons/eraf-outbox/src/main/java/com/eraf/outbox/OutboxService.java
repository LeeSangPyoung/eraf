package com.eraf.outbox;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(OutboxMessage.OutboxStatus status, Pageable pageable);
    List<OutboxMessage> findByStatusAndProcessedAtBefore(OutboxMessage.OutboxStatus status, Instant before);
    long countByStatus(OutboxMessage.OutboxStatus status);
    List<OutboxMessage> findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(String aggregateType, String aggregateId);
}

@Service
public class OutboxService {
    private final OutboxRepository repository;

    public OutboxService(OutboxRepository repository) {
        this.repository = repository;
    }

    /**
     * 아웃박스 메시지 저장 (destination 포함)
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboxMessage save(String destination, String aggregateType, String aggregateId,
                               String eventType, String payload) {
        OutboxMessage message = new OutboxMessage();
        message.setDestination(destination);
        message.setAggregateType(aggregateType);
        message.setAggregateId(aggregateId);
        message.setEventType(eventType);
        message.setPayload(payload);
        return repository.save(message);
    }

    /**
     * 아웃박스 메시지 저장 (하위 호환)
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboxMessage save(String aggregateType, String aggregateId, String eventType, String payload) {
        return save(null, aggregateType, aggregateId, eventType, payload);
    }

    /**
     * ID로 메시지 조회
     */
    @Transactional(readOnly = true)
    public Optional<OutboxMessage> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * PENDING 상태 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<OutboxMessage> findPending(int limit) {
        return repository.findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.PENDING, PageRequest.of(0, limit));
    }

    /**
     * FAILED 상태 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<OutboxMessage> findFailed(int limit) {
        return repository.findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.FAILED, PageRequest.of(0, limit));
    }

    /**
     * 특정 aggregate의 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<OutboxMessage> findByAggregate(String aggregateType, String aggregateId) {
        return repository.findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(aggregateType, aggregateId);
    }

    /**
     * 메시지를 PROCESSED로 마킹
     */
    @Transactional(rollbackFor = Exception.class)
    public void markProcessed(OutboxMessage message) {
        message.setStatus(OutboxMessage.OutboxStatus.PROCESSED);
        message.setProcessedAt(Instant.now());
        repository.save(message);
    }

    /**
     * 메시지를 FAILED로 마킹
     */
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(OutboxMessage message) {
        message.setRetryCount(message.getRetryCount() + 1);
        message.setStatus(OutboxMessage.OutboxStatus.FAILED);
        repository.save(message);
    }

    /**
     * FAILED 메시지를 PENDING으로 재설정 (재시도)
     */
    @Transactional(rollbackFor = Exception.class)
    public int retryFailed(int limit) {
        List<OutboxMessage> failedMessages = findFailed(limit);
        for (OutboxMessage message : failedMessages) {
            message.setStatus(OutboxMessage.OutboxStatus.PENDING);
            repository.save(message);
        }
        return failedMessages.size();
    }

    /**
     * 상태별 메시지 수 조회
     */
    @Transactional(readOnly = true)
    public long countByStatus(OutboxMessage.OutboxStatus status) {
        return repository.countByStatus(status);
    }
}
