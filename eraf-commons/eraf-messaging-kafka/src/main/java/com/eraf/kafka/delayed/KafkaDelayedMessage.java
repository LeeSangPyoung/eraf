package com.eraf.kafka.delayed;

import com.eraf.messaging.ErafMessage;

import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 지연 발행 메시지
 *
 * {@link java.util.concurrent.DelayQueue}에 저장되며,
 * scheduledAt 시간이 되면 원본 토픽으로 재발행됩니다.
 */
public class KafkaDelayedMessage implements Delayed {

    private final String topic;
    private final ErafMessage<Object> message;
    private final Map<String, Object> headers;
    private final long scheduledAt;

    public KafkaDelayedMessage(String topic, ErafMessage<Object> message,
                                Map<String, Object> headers, long scheduledAt) {
        this.topic = topic;
        this.message = message;
        this.headers = headers;
        this.scheduledAt = scheduledAt;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(scheduledAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other instanceof KafkaDelayedMessage otherMsg) {
            return Long.compare(this.scheduledAt, otherMsg.scheduledAt);
        }
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
    }

    public String getTopic() {
        return topic;
    }

    public ErafMessage<Object> getMessage() {
        return message;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }
}
