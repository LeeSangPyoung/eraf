package com.eraf.saga.event;

/**
 * Saga 이벤트 발행자 인터페이스
 */
public interface SagaEventPublisher {

    /**
     * Saga 이벤트 발행
     */
    void publish(SagaEvent event);

    /**
     * 특정 토픽으로 이벤트 발행
     */
    void publish(String topic, SagaEvent event);
}
