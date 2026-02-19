package com.eraf.kafka.delayed;

import com.eraf.messaging.ErafMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 지연 메시지 디스패처
 *
 * <p>인-메모리 {@link DelayQueue}를 사용하여 지연 발행을 처리합니다.
 * 내부 스레드가 큐를 모니터링하다가 발행 시간이 도래하면 Kafka로 전송합니다.</p>
 *
 * <p><strong>주의:</strong> 인-메모리 방식이므로 애플리케이션 재시작 시 대기 중인 메시지는 소실됩니다.
 * 영속적 지연 메시지가 필요하다면 DB/Redis 기반 스케줄러와 조합하세요.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * messagePublisher.publishDelayed("my-topic", payload, 5000); // 5초 후 발행
 * </pre>
 *
 * <h3>설정</h3>
 * <pre>
 * eraf:
 *   kafka:
 *     delayed-messaging:
 *       enabled: true
 * </pre>
 */
public class KafkaDelayedMessageDispatcher implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaDelayedMessageDispatcher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DelayQueue<KafkaDelayedMessage> delayQueue = new DelayQueue<>();
    private ExecutorService dispatcher;
    private volatile boolean running = false;

    public KafkaDelayedMessageDispatcher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        running = true;
        dispatcher = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "eraf-kafka-delay-dispatcher");
            t.setDaemon(true);
            return t;
        });
        dispatcher.submit(this::dispatchLoop);
        log.info("KafkaDelayedMessageDispatcher started");
    }

    @Override
    public void destroy() {
        running = false;
        if (dispatcher != null) {
            dispatcher.shutdownNow();
            try {
                dispatcher.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("KafkaDelayedMessageDispatcher stopped. {} messages were pending.", delayQueue.size());
    }

    /**
     * 지연 메시지 등록
     *
     * @param topic        발행할 토픽
     * @param message      발행할 메시지
     * @param headers      추가 헤더 (null 허용)
     * @param delayMillis  지연 시간 (밀리초)
     */
    public void schedule(String topic, ErafMessage<Object> message,
                         Map<String, Object> headers, long delayMillis) {
        long scheduledAt = System.currentTimeMillis() + delayMillis;
        KafkaDelayedMessage delayed = new KafkaDelayedMessage(topic, message, headers, scheduledAt);
        delayQueue.offer(delayed);
        log.debug("Delayed message scheduled: topic={}, delay={}ms, scheduledAt={}", topic, delayMillis, scheduledAt);
    }

    /**
     * 대기 중인 지연 메시지 수 반환
     */
    public int pendingCount() {
        return delayQueue.size();
    }

    private void dispatchLoop() {
        while (running) {
            try {
                KafkaDelayedMessage delayed = delayQueue.poll(1, TimeUnit.SECONDS);
                if (delayed != null) {
                    dispatch(delayed);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in delayed message dispatch loop", e);
            }
        }
    }

    private void dispatch(KafkaDelayedMessage delayed) {
        try {
            ErafMessage<Object> message = delayed.getMessage();
            Headers kafkaHeaders = buildHeaders(message, delayed.getHeaders(), delayed.getScheduledAt());
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    delayed.getTopic(), null, null, message.getMessageId(), message, kafkaHeaders);
            kafkaTemplate.send(record);
            log.debug("Delayed message dispatched: topic={}, messageId={}", delayed.getTopic(), message.getMessageId());
        } catch (Exception e) {
            log.error("Failed to dispatch delayed message: topic={}, error={}", delayed.getTopic(), e.getMessage());
        }
    }

    private Headers buildHeaders(ErafMessage<Object> message, Map<String, Object> additionalHeaders, long scheduledAt) {
        Headers headers = new RecordHeaders();

        if (message.getTraceId() != null) {
            headers.add("X-Trace-Id", message.getTraceId().getBytes(StandardCharsets.UTF_8));
        }
        headers.add("X-Message-Id", message.getMessageId().getBytes(StandardCharsets.UTF_8));
        headers.add("X-Delayed-At", String.valueOf(scheduledAt).getBytes(StandardCharsets.UTF_8));

        if (additionalHeaders != null) {
            additionalHeaders.forEach((key, value) -> {
                if (value != null) {
                    headers.add(key, String.valueOf(value).getBytes(StandardCharsets.UTF_8));
                }
            });
        }
        return headers;
    }
}
