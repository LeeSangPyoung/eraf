package com.eraf.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(OutboxMessage.OutboxStatus status, Pageable pageable);
    List<OutboxMessage> findByStatusAndProcessedAtBefore(OutboxMessage.OutboxStatus status, Instant before);
    long countByStatus(OutboxMessage.OutboxStatus status);
}

@Service
public class OutboxService {
    private final OutboxRepository repository;

    public OutboxService(OutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OutboxMessage save(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxMessage message = new OutboxMessage();
        message.setAggregateType(aggregateType);
        message.setAggregateId(aggregateId);
        message.setEventType(eventType);
        message.setPayload(payload);
        return repository.save(message);
    }

    /**
     * 상태별 메시지 수 조회
     */
    public long countByStatus(OutboxMessage.OutboxStatus status) {
        return repository.countByStatus(status);
    }
}
