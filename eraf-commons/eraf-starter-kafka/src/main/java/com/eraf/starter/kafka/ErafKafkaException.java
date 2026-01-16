package com.eraf.starter.kafka;

/**
 * ERAF Kafka 예외
 */
public class ErafKafkaException extends RuntimeException {

    public ErafKafkaException(String message) {
        super(message);
    }

    public ErafKafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
