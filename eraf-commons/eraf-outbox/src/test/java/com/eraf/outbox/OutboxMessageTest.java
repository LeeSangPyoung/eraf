package com.eraf.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * OutboxMessage 테스트
 */
class OutboxMessageTest {

    @Test
    void create_ShouldInitializeWithPendingStatus() {
        // given
        OutboxMessage message = new OutboxMessage();
        message.setAggregateType("User");
        message.setAggregateId("user-123");
        message.setEventType("UserCreated");
        message.setPayload("{\"userId\": 123}");

        // then - 기본값 PENDING
        assertThat(message.getAggregateType()).isEqualTo("User");
        assertThat(message.getAggregateId()).isEqualTo("user-123");
        assertThat(message.getEventType()).isEqualTo("UserCreated");
        assertThat(message.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        assertThat(message.getCreatedAt()).isNotNull();
    }

    @Test
    void markAsProcessed_ShouldUpdateStatus() {
        // given
        OutboxMessage message = new OutboxMessage();

        // when
        message.setStatus(OutboxMessage.OutboxStatus.PROCESSED);
        message.setProcessedAt(Instant.now());

        // then
        assertThat(message.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PROCESSED);
        assertThat(message.getProcessedAt()).isNotNull();
    }

    @Test
    void markAsFailed_ShouldUpdateStatusAndRetryCount() {
        // given
        OutboxMessage message = new OutboxMessage();

        // when
        message.setStatus(OutboxMessage.OutboxStatus.FAILED);
        message.setRetryCount(3);

        // then
        assertThat(message.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.FAILED);
        assertThat(message.getRetryCount()).isEqualTo(3);
    }

    @Test
    void incrementRetryCount_ShouldIncreaseCount() {
        // given
        OutboxMessage message = new OutboxMessage();
        assertThat(message.getRetryCount()).isEqualTo(0);

        // when
        message.setRetryCount(message.getRetryCount() + 1);
        message.setRetryCount(message.getRetryCount() + 1);

        // then
        assertThat(message.getRetryCount()).isEqualTo(2);
    }

    @Test
    void outboxStatus_Values_ShouldContainAllStatuses() {
        // when/then
        assertThat(OutboxMessage.OutboxStatus.values()).containsExactlyInAnyOrder(
            OutboxMessage.OutboxStatus.PENDING,
            OutboxMessage.OutboxStatus.PROCESSED,
            OutboxMessage.OutboxStatus.FAILED
        );
    }
}
