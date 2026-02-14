package com.eraf.idempotent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * In-Memory Idempotency Store Unit Tests
 */
class InMemoryIdempotencyStoreTest {

    private InMemoryIdempotencyStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryIdempotencyStore();
    }

    @Test
    void shouldSetIfAbsentReturnTrueForNewKey() {
        // when
        boolean result = store.setIfAbsent("key1", Duration.ofMinutes(5));

        // then
        assertThat(result).isTrue();
        assertThat(store.exists("key1")).isTrue();
    }

    @Test
    void shouldSetIfAbsentReturnFalseForExistingKey() {
        // given
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // when
        boolean result = store.setIfAbsent("key1", Duration.ofMinutes(5));

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCheckKeyExistence() {
        // given
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // when & then
        assertThat(store.exists("key1")).isTrue();
        assertThat(store.exists("nonexistent")).isFalse();
    }

    @Test
    void shouldSaveAndRetrieveResult() {
        // given
        String result = "test result";
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // when
        store.saveResult("key1", result, Duration.ofMinutes(5));
        Optional<Object> retrieved = store.getResult("key1");

        // then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(result);
    }

    @Test
    void shouldReturnEmptyForNonExistentResult() {
        // when
        Optional<Object> result = store.getResult("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteKey() {
        // given
        store.setIfAbsent("key1", Duration.ofMinutes(5));
        assertThat(store.exists("key1")).isTrue();

        // when
        store.delete("key1");

        // then
        assertThat(store.exists("key1")).isFalse();
    }

    @Test
    void shouldExpireKeysAfterTimeout() throws InterruptedException {
        // given
        store.setIfAbsent("key1", Duration.ofMillis(100));

        // when
        Thread.sleep(150);

        // then
        assertThat(store.exists("key1")).isFalse();
    }

    @Test
    void shouldReturnEmptyForExpiredResult() throws InterruptedException {
        // given
        store.setIfAbsent("key1", Duration.ofMillis(100));
        store.saveResult("key1", "test", Duration.ofMillis(100));

        // when
        Thread.sleep(150);
        Optional<Object> result = store.getResult("key1");

        // then
        assertThat(result).isEmpty();
    }
}
