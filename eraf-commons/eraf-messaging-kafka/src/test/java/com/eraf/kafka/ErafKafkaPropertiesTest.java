package com.eraf.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafKafkaPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // Then
        assertEquals("eraf-", properties.getTopicPrefix());
        assertEquals("eraf-group-", properties.getGroupIdPrefix());
        assertFalse(properties.isEnableAutoCommit());
        assertEquals("earliest", properties.getAutoOffsetReset());
        assertEquals(500, properties.getMaxPollRecords());
    }

    @Test
    @DisplayName("토픽 프리픽스 설정")
    void testTopicPrefix() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // When
        properties.setTopicPrefix("custom-");

        // Then
        assertEquals("custom-", properties.getTopicPrefix());
    }

    @Test
    @DisplayName("컨슈머 그룹 프리픽스 설정")
    void testGroupIdPrefix() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // When
        properties.setGroupIdPrefix("myapp-group-");

        // Then
        assertEquals("myapp-group-", properties.getGroupIdPrefix());
    }

    @Test
    @DisplayName("재시도 설정 기본값")
    void testRetryPropertiesDefaults() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.RetryProperties retry = properties.getRetry();

        // Then
        assertNotNull(retry);
        assertTrue(retry.isEnabled());
        assertEquals(3, retry.getMaxAttempts());
        assertEquals(1000, retry.getBackoffInterval());
        assertEquals(2.0, retry.getBackoffMultiplier());
        assertEquals(30000, retry.getMaxBackoffInterval());
    }

    @Test
    @DisplayName("재시도 설정 변경")
    void testRetryPropertiesCustom() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.RetryProperties retry = properties.getRetry();

        // When
        retry.setEnabled(false);
        retry.setMaxAttempts(5);
        retry.setBackoffInterval(2000);
        retry.setBackoffMultiplier(3.0);
        retry.setMaxBackoffInterval(60000);

        // Then
        assertFalse(retry.isEnabled());
        assertEquals(5, retry.getMaxAttempts());
        assertEquals(2000, retry.getBackoffInterval());
        assertEquals(3.0, retry.getBackoffMultiplier());
        assertEquals(60000, retry.getMaxBackoffInterval());
    }

    @Test
    @DisplayName("DLQ 설정 기본값")
    void testDlqPropertiesDefaults() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.DlqProperties dlq = properties.getDlq();

        // Then
        assertNotNull(dlq);
        assertTrue(dlq.isEnabled());
        assertEquals(".DLQ", dlq.getTopicSuffix());
        assertEquals(7, dlq.getRetentionDays());
    }

    @Test
    @DisplayName("DLQ 설정 변경")
    void testDlqPropertiesCustom() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.DlqProperties dlq = properties.getDlq();

        // When
        dlq.setEnabled(false);
        dlq.setTopicSuffix("-dead-letter");
        dlq.setRetentionDays(30);

        // Then
        assertFalse(dlq.isEnabled());
        assertEquals("-dead-letter", dlq.getTopicSuffix());
        assertEquals(30, dlq.getRetentionDays());
    }

    @Test
    @DisplayName("트랜잭션 설정 기본값")
    void testTransactionPropertiesDefaults() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.TransactionProperties tx = properties.getTransaction();

        // Then
        assertNotNull(tx);
        assertFalse(tx.isEnabled());
        assertEquals("eraf-tx-", tx.getIdPrefix());
        assertEquals(60, tx.getTimeoutSeconds());
    }

    @Test
    @DisplayName("트랜잭션 설정 변경")
    void testTransactionPropertiesCustom() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.TransactionProperties tx = properties.getTransaction();

        // When
        tx.setEnabled(true);
        tx.setIdPrefix("myapp-tx-");
        tx.setTimeoutSeconds(120);

        // Then
        assertTrue(tx.isEnabled());
        assertEquals("myapp-tx-", tx.getIdPrefix());
        assertEquals(120, tx.getTimeoutSeconds());
    }

    @Test
    @DisplayName("프로듀서 설정 기본값")
    void testProducerPropertiesDefaults() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.ProducerProperties producer = properties.getProducer();

        // Then
        assertNotNull(producer);
        assertEquals("none", producer.getCompressionType());
        assertEquals(16384, producer.getBatchSize());
        assertEquals(0, producer.getLingerMs());
        assertEquals(33554432, producer.getBufferMemory());
        assertEquals("all", producer.getAcks());
        assertTrue(producer.isIdempotence());
    }

    @Test
    @DisplayName("프로듀서 설정 변경")
    void testProducerPropertiesCustom() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.ProducerProperties producer = properties.getProducer();

        // When
        producer.setCompressionType("gzip");
        producer.setBatchSize(32768);
        producer.setLingerMs(5);
        producer.setBufferMemory(67108864);
        producer.setAcks("1");
        producer.setIdempotence(false);

        // Then
        assertEquals("gzip", producer.getCompressionType());
        assertEquals(32768, producer.getBatchSize());
        assertEquals(5, producer.getLingerMs());
        assertEquals(67108864, producer.getBufferMemory());
        assertEquals("1", producer.getAcks());
        assertFalse(producer.isIdempotence());
    }

    @Test
    @DisplayName("중첩 프로퍼티 교체")
    void testReplaceNestedProperties() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();
        ErafKafkaProperties.RetryProperties newRetry = new ErafKafkaProperties.RetryProperties();
        newRetry.setMaxAttempts(10);

        // When
        properties.setRetry(newRetry);

        // Then
        assertEquals(10, properties.getRetry().getMaxAttempts());
    }

    @Test
    @DisplayName("auto commit 설정")
    void testAutoCommitSetting() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // When
        properties.setEnableAutoCommit(true);

        // Then
        assertTrue(properties.isEnableAutoCommit());
    }

    @Test
    @DisplayName("max poll records 설정")
    void testMaxPollRecords() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // When
        properties.setMaxPollRecords(1000);

        // Then
        assertEquals(1000, properties.getMaxPollRecords());
    }

    @Test
    @DisplayName("auto offset reset 설정")
    void testAutoOffsetReset() {
        // Given
        ErafKafkaProperties properties = new ErafKafkaProperties();

        // When
        properties.setAutoOffsetReset("latest");

        // Then
        assertEquals("latest", properties.getAutoOffsetReset());
    }
}
