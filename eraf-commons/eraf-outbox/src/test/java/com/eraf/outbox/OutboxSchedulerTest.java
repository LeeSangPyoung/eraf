package com.eraf.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxScheduler 테스트")
class OutboxSchedulerTest {

    @Mock
    private OutboxRepository repository;

    private ErafOutboxProperties properties;
    private OutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        properties = new ErafOutboxProperties();
        properties.setBatchSize(100);
        properties.setMaxRetries(3);
        properties.setCleanupDays(7);
        scheduler = new OutboxScheduler(repository, properties);
    }

    @Test
    @DisplayName("messagePublisher 미설정 시 pollAndPublish 무시")
    void pollAndPublish_WithoutPublisher_ShouldDoNothing() {
        // when
        scheduler.pollAndPublish();

        // then
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("PENDING 메시지 없을 때 pollAndPublish 무시")
    void pollAndPublish_NoPendingMessages_ShouldDoNothing() {
        // given
        scheduler.setMessagePublisher(msg -> {});
        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // when
        scheduler.pollAndPublish();

        // then
        verify(repository).findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.PENDING, PageRequest.of(0, 100));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("PENDING 메시지 정상 발행 및 PROCESSED 마킹")
    void pollAndPublish_ShouldProcessAndMarkMessages() {
        // given
        AtomicInteger publishCount = new AtomicInteger(0);
        scheduler.setMessagePublisher(msg -> publishCount.incrementAndGet());

        OutboxMessage msg1 = new OutboxMessage();
        msg1.setId(1L);
        msg1.setEventType("OrderCreated");
        OutboxMessage msg2 = new OutboxMessage();
        msg2.setId(2L);
        msg2.setEventType("OrderUpdated");

        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(msg1, msg2));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        scheduler.pollAndPublish();

        // then
        assertThat(publishCount.get()).isEqualTo(2);
        assertThat(msg1.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PROCESSED);
        assertThat(msg1.getProcessedAt()).isNotNull();
        assertThat(msg2.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PROCESSED);
        verify(repository, times(2)).save(any());
    }

    @Test
    @DisplayName("발행 실패 시 retryCount 증가")
    void pollAndPublish_OnFailure_ShouldIncrementRetryCount() {
        // given
        scheduler.setMessagePublisher(msg -> {
            throw new RuntimeException("Publish failed");
        });

        OutboxMessage msg = new OutboxMessage();
        msg.setId(1L);
        msg.setRetryCount(0);

        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(msg));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        scheduler.pollAndPublish();

        // then
        assertThat(msg.getRetryCount()).isEqualTo(1);
        assertThat(msg.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        verify(repository).save(msg);
    }

    @Test
    @DisplayName("maxRetries 초과 시 FAILED 상태로 변경")
    void pollAndPublish_ExceedMaxRetries_ShouldMarkFailed() {
        // given
        scheduler.setMessagePublisher(msg -> {
            throw new RuntimeException("Publish failed");
        });

        OutboxMessage msg = new OutboxMessage();
        msg.setId(1L);
        msg.setRetryCount(2); // maxRetries = 3, retryCount will be 3 after this

        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(msg));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        scheduler.pollAndPublish();

        // then
        assertThat(msg.getRetryCount()).isEqualTo(3);
        assertThat(msg.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("FAILED 메시지 재시도 - PENDING으로 복원")
    void retryFailedMessages_ShouldResetToPending() {
        // given
        OutboxMessage msg1 = new OutboxMessage();
        msg1.setStatus(OutboxMessage.OutboxStatus.FAILED);
        msg1.setRetryCount(2);
        OutboxMessage msg2 = new OutboxMessage();
        msg2.setStatus(OutboxMessage.OutboxStatus.FAILED);
        msg2.setRetryCount(1);

        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.FAILED), any(PageRequest.class)))
                .thenReturn(List.of(msg1, msg2));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        int retried = scheduler.retryFailedMessages();

        // then
        assertThat(retried).isEqualTo(2);
        assertThat(msg1.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        assertThat(msg1.getRetryCount()).isEqualTo(0);
        assertThat(msg2.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        assertThat(msg2.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("오래된 PROCESSED 메시지 정리")
    void cleanupProcessedMessages_ShouldDeleteOldMessages() {
        // given
        OutboxMessage oldMsg = new OutboxMessage();
        oldMsg.setStatus(OutboxMessage.OutboxStatus.PROCESSED);
        oldMsg.setProcessedAt(Instant.now().minusSeconds(86400L * 10)); // 10일 전

        when(repository.findByStatusAndProcessedAtBefore(
                eq(OutboxMessage.OutboxStatus.PROCESSED), any(Instant.class)))
                .thenReturn(List.of(oldMsg));

        // when
        scheduler.cleanupProcessedMessages();

        // then
        verify(repository).deleteAll(List.of(oldMsg));
    }

    @Test
    @DisplayName("정리할 PROCESSED 메시지 없을 때")
    void cleanupProcessedMessages_NoOldMessages_ShouldNotDelete() {
        // given
        when(repository.findByStatusAndProcessedAtBefore(
                eq(OutboxMessage.OutboxStatus.PROCESSED), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // when
        scheduler.cleanupProcessedMessages();

        // then
        verify(repository, never()).deleteAll(any());
    }
}
