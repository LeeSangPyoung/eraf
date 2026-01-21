package com.eraf.starter.messaging;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 통합 메시지 퍼블리셔 인터페이스
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
}
