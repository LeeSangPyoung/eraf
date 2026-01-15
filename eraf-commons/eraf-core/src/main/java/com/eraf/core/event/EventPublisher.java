package com.eraf.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 이벤트 발행기
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 이벤트 즉시 발행
     */
    public void publish(Object event) {
        logEvent(event, "PUBLISH");
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 도메인 이벤트 발행
     */
    public void publish(DomainEvent event) {
        log.info("도메인 이벤트 발행: eventId={}, type={}",
                event.getEventId(), event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 여러 이벤트 일괄 발행
     */
    public void publishAll(List<?> events) {
        for (Object event : events) {
            publish(event);
        }
    }

    /**
     * 트랜잭션 커밋 후 이벤트 발행
     */
    public void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            applicationEventPublisher.publishEvent(event);
                        }
                    }
            );
        } else {
            // 트랜잭션이 없으면 즉시 발행
            applicationEventPublisher.publishEvent(event);
        }
    }

    /**
     * 트랜잭션 완료 후 이벤트 발행 (성공/실패 무관)
     */
    public void publishAfterCompletion(Object event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            applicationEventPublisher.publishEvent(event);
                        }
                    }
            );
        } else {
            applicationEventPublisher.publishEvent(event);
        }
    }

    /**
     * 트랜잭션 커밋 후 여러 이벤트 발행
     */
    public void publishAllAfterCommit(List<?> events) {
        for (Object event : events) {
            publishAfterCommit(event);
        }
    }

    private void logEvent(Object event, String action) {
        if (event instanceof DomainEvent domainEvent) {
            log.debug("{}: eventId={}, type={}",
                    action, domainEvent.getEventId(), domainEvent.getEventType());
        } else {
            log.debug("{}: type={}", action, event.getClass().getSimpleName());
        }
    }
}
