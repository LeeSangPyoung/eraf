package com.eraf.starter.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

/**
 * ERAF Kafka 에러 핸들러
 * 메시지 처리 실패 시 로깅 및 DLQ 전송 처리
 */
public class ErafKafkaErrorHandler implements CommonErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErafKafkaErrorHandler.class);

    private final ErafKafkaProducer producer;
    private final ErafKafkaProperties properties;

    public ErafKafkaErrorHandler(ErafKafkaProducer producer, ErafKafkaProperties properties) {
        this.producer = producer;
        this.properties = properties;
    }

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
                             Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("Error processing Kafka message: topic={}, partition={}, offset={}, error={}",
                record.topic(), record.partition(), record.offset(), thrownException.getMessage());

        if (properties.getDlq().isEnabled()) {
            sendToDlq(record, thrownException);
        }

        return true; // 에러 처리 완료, 다음 메시지 진행
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                     MessageListenerContainer container, boolean batchListener) {
        log.error("Kafka consumer error: {}", thrownException.getMessage(), thrownException);
    }

    @Override
    public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data,
                            Consumer<?, ?> consumer, MessageListenerContainer container,
                            Runnable invokeListener) {
        log.error("Error processing Kafka batch: size={}, error={}",
                data.count(), thrownException.getMessage());

        if (properties.getDlq().isEnabled()) {
            for (ConsumerRecord<?, ?> record : data) {
                sendToDlq(record, thrownException);
            }
        }
    }

    /**
     * Dead Letter Queue로 메시지 전송
     */
    private void sendToDlq(ConsumerRecord<?, ?> record, Exception exception) {
        try {
            String dlqTopic = record.topic() + properties.getDlq().getTopicSuffix();

            DlqMessage dlqMessage = new DlqMessage();
            dlqMessage.setOriginalTopic(record.topic());
            dlqMessage.setOriginalPartition(record.partition());
            dlqMessage.setOriginalOffset(record.offset());
            dlqMessage.setOriginalKey(record.key() != null ? record.key().toString() : null);
            dlqMessage.setOriginalValue(record.value() != null ? record.value().toString() : null);
            dlqMessage.setErrorMessage(exception.getMessage());
            dlqMessage.setErrorClass(exception.getClass().getName());
            dlqMessage.setTimestamp(System.currentTimeMillis());

            ErafKafkaEvent<DlqMessage> event = ErafKafkaEvent.of("DLQ_MESSAGE", dlqMessage);
            producer.send(dlqTopic, event);

            log.info("Message sent to DLQ: topic={}, originalTopic={}, offset={}",
                    dlqTopic, record.topic(), record.offset());

        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", e.getMessage(), e);
        }
    }

    /**
     * DLQ 메시지 구조
     */
    public static class DlqMessage {
        private String originalTopic;
        private int originalPartition;
        private long originalOffset;
        private String originalKey;
        private String originalValue;
        private String errorMessage;
        private String errorClass;
        private long timestamp;

        public String getOriginalTopic() {
            return originalTopic;
        }

        public void setOriginalTopic(String originalTopic) {
            this.originalTopic = originalTopic;
        }

        public int getOriginalPartition() {
            return originalPartition;
        }

        public void setOriginalPartition(int originalPartition) {
            this.originalPartition = originalPartition;
        }

        public long getOriginalOffset() {
            return originalOffset;
        }

        public void setOriginalOffset(long originalOffset) {
            this.originalOffset = originalOffset;
        }

        public String getOriginalKey() {
            return originalKey;
        }

        public void setOriginalKey(String originalKey) {
            this.originalKey = originalKey;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public void setOriginalValue(String originalValue) {
            this.originalValue = originalValue;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorClass() {
            return errorClass;
        }

        public void setErrorClass(String errorClass) {
            this.errorClass = errorClass;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
