package com.eraf.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxService 테스트")
class OutboxServiceTest {

    @Mock
    private OutboxRepository repository;

    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        outboxService = new OutboxService(repository);
    }

    @Test
    @DisplayName("destination 포함 메시지 저장")
    void save_WithDestination_ShouldSaveMessage() {
        // given
        when(repository.save(any(OutboxMessage.class))).thenAnswer(inv -> {
            OutboxMessage msg = inv.getArgument(0);
            msg.setId(1L);
            return msg;
        });

        // when
        OutboxMessage result = outboxService.save("orders-topic", "Order", "order-123",
                "OrderCreated", "{\"orderId\":\"order-123\"}");

        // then
        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(repository).save(captor.capture());

        OutboxMessage saved = captor.getValue();
        assertThat(saved.getDestination()).isEqualTo("orders-topic");
        assertThat(saved.getAggregateType()).isEqualTo("Order");
        assertThat(saved.getAggregateId()).isEqualTo("order-123");
        assertThat(saved.getEventType()).isEqualTo("OrderCreated");
        assertThat(saved.getPayload()).isEqualTo("{\"orderId\":\"order-123\"}");
        assertThat(saved.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("destination 없이 메시지 저장 (하위호환)")
    void save_WithoutDestination_ShouldSaveWithNullDestination() {
        // given
        when(repository.save(any(OutboxMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        OutboxMessage result = outboxService.save("User", "user-1", "UserCreated", "{}");

        // then
        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDestination()).isNull();
    }

    @Test
    @DisplayName("ID로 메시지 조회")
    void findById_ShouldReturnMessage() {
        // given
        OutboxMessage message = new OutboxMessage();
        message.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(message));

        // when
        Optional<OutboxMessage> result = outboxService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PENDING 상태 메시지 조회")
    void findPending_ShouldReturnPendingMessages() {
        // given
        OutboxMessage msg1 = new OutboxMessage();
        OutboxMessage msg2 = new OutboxMessage();
        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(msg1, msg2));

        // when
        List<OutboxMessage> result = outboxService.findPending(10);

        // then
        assertThat(result).hasSize(2);
        verify(repository).findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.PENDING, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("FAILED 상태 메시지 조회")
    void findFailed_ShouldReturnFailedMessages() {
        // given
        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.FAILED), any(PageRequest.class)))
                .thenReturn(List.of(new OutboxMessage()));

        // when
        List<OutboxMessage> result = outboxService.findFailed(5);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("aggregate별 메시지 조회")
    void findByAggregate_ShouldReturnMessages() {
        // given
        OutboxMessage msg = new OutboxMessage();
        msg.setAggregateType("Order");
        msg.setAggregateId("order-1");
        when(repository.findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc("Order", "order-1"))
                .thenReturn(List.of(msg));

        // when
        List<OutboxMessage> result = outboxService.findByAggregate("Order", "order-1");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAggregateType()).isEqualTo("Order");
    }

    @Test
    @DisplayName("메시지 PROCESSED 마킹")
    void markProcessed_ShouldUpdateStatusAndTimestamp() {
        // given
        OutboxMessage message = new OutboxMessage();
        when(repository.save(any())).thenReturn(message);

        // when
        outboxService.markProcessed(message);

        // then
        assertThat(message.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PROCESSED);
        assertThat(message.getProcessedAt()).isNotNull();
        verify(repository).save(message);
    }

    @Test
    @DisplayName("메시지 FAILED 마킹 시 retryCount 증가")
    void markFailed_ShouldIncrementRetryCountAndUpdateStatus() {
        // given
        OutboxMessage message = new OutboxMessage();
        assertThat(message.getRetryCount()).isEqualTo(0);
        when(repository.save(any())).thenReturn(message);

        // when
        outboxService.markFailed(message);

        // then
        assertThat(message.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.FAILED);
        assertThat(message.getRetryCount()).isEqualTo(1);
        verify(repository).save(message);
    }

    @Test
    @DisplayName("FAILED 메시지 재시도")
    void retryFailed_ShouldResetStatusToPending() {
        // given
        OutboxMessage msg1 = new OutboxMessage();
        msg1.setStatus(OutboxMessage.OutboxStatus.FAILED);
        OutboxMessage msg2 = new OutboxMessage();
        msg2.setStatus(OutboxMessage.OutboxStatus.FAILED);

        when(repository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxMessage.OutboxStatus.FAILED), any(PageRequest.class)))
                .thenReturn(List.of(msg1, msg2));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        int count = outboxService.retryFailed(10);

        // then
        assertThat(count).isEqualTo(2);
        assertThat(msg1.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        assertThat(msg2.getStatus()).isEqualTo(OutboxMessage.OutboxStatus.PENDING);
        verify(repository, times(2)).save(any());
    }

    @Test
    @DisplayName("상태별 메시지 수 조회")
    void countByStatus_ShouldReturnCount() {
        // given
        when(repository.countByStatus(OutboxMessage.OutboxStatus.PENDING)).thenReturn(5L);

        // when
        long count = outboxService.countByStatus(OutboxMessage.OutboxStatus.PENDING);

        // then
        assertThat(count).isEqualTo(5L);
    }
}
