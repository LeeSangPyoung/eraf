package com.eraf.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Outbox 폴링 퍼블리셔
 *
 * 주기적으로 PENDING 상태의 메시지를 조회하고 발행합니다.
 * 메시지 발행이 실패하면 retry count를 증가시키고, 최대 횟수를 초과하면 FAILED로 변경합니다.
 */
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxRepository repository;
    private final ErafOutboxProperties properties;
    private final int batchSize;
    private final int maxRetries;
    private Consumer<OutboxMessage> messagePublisher;

    public OutboxScheduler(OutboxRepository repository, ErafOutboxProperties properties) {
        this.repository = repository;
        this.properties = properties;
        this.batchSize = properties.getBatchSize();
        this.maxRetries = properties.getMaxRetries();
    }

    /**
     * 메시지 발행 핸들러 등록
     */
    public void setMessagePublisher(Consumer<OutboxMessage> messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    /**
     * 주기적으로 PENDING 메시지를 처리 (5초 간격)
     */
    @Scheduled(fixedDelayString = "${eraf.outbox.poll-interval:5000}")
    @Transactional
    public void pollAndPublish() {
        if (messagePublisher == null) {
            return;
        }

        List<OutboxMessage> pendingMessages = repository.findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.PENDING,
                PageRequest.of(0, batchSize));

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox messages", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            processMessage(message);
        }
    }

    private void processMessage(OutboxMessage message) {
        try {
            messagePublisher.accept(message);
            message.setStatus(OutboxMessage.OutboxStatus.PROCESSED);
            message.setProcessedAt(Instant.now());
            repository.save(message);
            log.debug("Outbox message processed: id={}, type={}", message.getId(), message.getEventType());
        } catch (Exception e) {
            int retryCount = message.getRetryCount() + 1;
            message.setRetryCount(retryCount);

            if (retryCount >= maxRetries) {
                message.setStatus(OutboxMessage.OutboxStatus.FAILED);
                log.error("Outbox message permanently failed after {} retries: id={}, type={}",
                        retryCount, message.getId(), message.getEventType(), e);
            } else {
                log.warn("Outbox message publish failed (retry {}/{}): id={}, type={}",
                        retryCount, maxRetries, message.getId(), message.getEventType(), e);
            }
            repository.save(message);
        }
    }

    /**
     * FAILED 상태 메시지 재시도
     */
    @Transactional
    public int retryFailedMessages() {
        List<OutboxMessage> failedMessages = repository.findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.FAILED,
                PageRequest.of(0, batchSize));

        int retried = 0;
        for (OutboxMessage message : failedMessages) {
            message.setStatus(OutboxMessage.OutboxStatus.PENDING);
            message.setRetryCount(0);
            repository.save(message);
            retried++;
        }
        return retried;
    }

    /**
     * 처리 완료된 오래된 메시지 정리
     */
    @Scheduled(cron = "${eraf.outbox.cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupProcessedMessages() {
        int cleanupDays = properties.getCleanupDays();
        Instant cutoff = Instant.now().minusSeconds(86400L * cleanupDays);
        List<OutboxMessage> oldMessages = repository.findByStatusAndProcessedAtBefore(
                OutboxMessage.OutboxStatus.PROCESSED, cutoff);

        if (!oldMessages.isEmpty()) {
            repository.deleteAll(oldMessages);
            log.info("Cleaned up {} processed outbox messages older than {} days", oldMessages.size(), cleanupDays);
        }
    }
}
