package com.eraf.saga.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

/**
 * Spring ApplicationEvent 기반 Saga 이벤트 발행자
 * 로컬 이벤트 발행용 (단일 인스턴스 환경)
 */
public class SpringSagaEventPublisher implements SagaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringSagaEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringSagaEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(SagaEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        log.debug("Publishing saga event: {} for saga {}", event.getEventType(), event.getSagaId());
        applicationEventPublisher.publishEvent(new SagaApplicationEvent(this, event));
    }

    @Override
    public void publish(String topic, SagaEvent event) {
        // Spring ApplicationEvent는 토픽 개념이 없으므로 기본 발행
        publish(event);
    }

    /**
     * Spring ApplicationEvent 래퍼
     */
    public static class SagaApplicationEvent extends org.springframework.context.ApplicationEvent {

        private final SagaEvent sagaEvent;

        public SagaApplicationEvent(Object source, SagaEvent sagaEvent) {
            super(source);
            this.sagaEvent = sagaEvent;
        }

        public SagaEvent getSagaEvent() {
            return sagaEvent;
        }
    }
}
