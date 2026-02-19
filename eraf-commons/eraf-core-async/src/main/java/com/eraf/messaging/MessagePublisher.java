package com.eraf.messaging;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 통합 메시지 퍼블리셔 인터페이스
 *
 * <p>Kafka, RabbitMQ 등 다양한 메시지 브로커에 대한 통합 발행 API를 제공합니다.</p>
 */
public interface MessagePublisher {

    /**
     * 동기 메시지 발행
     */
    void publish(String destination, Object message);

    /**
     * 동기 메시지 발행 (헤더 포함)
     */
    void publish(String destination, Object message, Map<String, Object> headers);

    /**
     * 키 기반 동기 메시지 발행
     *
     * <p>Kafka 파티셔닝 등 키 기반 라우팅이 필요한 경우 사용합니다.
     * 동일 키의 메시지는 동일 파티션으로 전송되어 순서가 보장됩니다.</p>
     *
     * @param destination 대상 토픽/큐
     * @param key         메시지 키 (파티셔닝용)
     * @param message     발행할 메시지
     */
    default void publish(String destination, String key, Object message) {
        publish(destination, message);
    }

    /**
     * 키 기반 동기 메시지 발행 (헤더 포함)
     */
    default void publish(String destination, String key, Object message, Map<String, Object> headers) {
        publish(destination, message, headers);
    }

    /**
     * 비동기 메시지 발행
     */
    CompletableFuture<Void> publishAsync(String destination, Object message);

    /**
     * 비동기 메시지 발행 (헤더 포함)
     */
    CompletableFuture<Void> publishAsync(String destination, Object message, Map<String, Object> headers);

    /**
     * 지연 메시지 발행
     */
    void publishDelayed(String destination, Object message, long delayMillis);

    /**
     * 지연 메시지 발행 (헤더 포함)
     */
    default void publishDelayed(String destination, Object message, long delayMillis, Map<String, Object> headers) {
        publishDelayed(destination, message, delayMillis);
    }
}
