package com.eraf.core.idempotent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryIdempotencyStoreTest {

    private InMemoryIdempotencyStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryIdempotencyStore();
    }

    @Test
    @DisplayName("키가 없을 때 setIfAbsent 성공")
    void testSetIfAbsentSuccess() {
        // When
        boolean result = store.setIfAbsent("key1", Duration.ofMinutes(5));

        // Then
        assertTrue(result);
        assertTrue(store.exists("key1"));
    }

    @Test
    @DisplayName("키가 존재할 때 setIfAbsent 실패")
    void testSetIfAbsentFails() {
        // Given
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // When
        boolean result = store.setIfAbsent("key1", Duration.ofMinutes(5));

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("키 존재 여부 확인")
    void testExists() {
        // Given
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // Then
        assertTrue(store.exists("key1"));
        assertFalse(store.exists("nonexistent"));
    }

    @Test
    @DisplayName("결과 저장 및 조회")
    void testSaveAndGetResult() {
        // Given
        String key = "key1";
        String result = "stored-result";

        // When
        store.saveResult(key, result, Duration.ofMinutes(5));
        Optional<Object> retrieved = store.getResult(key);

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(result, retrieved.get());
    }

    @Test
    @DisplayName("결과 없는 키 조회")
    void testGetResultNotFound() {
        // When
        Optional<Object> result = store.getResult("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("키 삭제")
    void testDelete() {
        // Given
        store.setIfAbsent("key1", Duration.ofMinutes(5));

        // When
        store.delete("key1");

        // Then
        assertFalse(store.exists("key1"));
    }

    @Test
    @DisplayName("만료된 키 존재 확인 시 false 반환")
    void testExpiredKeyNotExists() throws InterruptedException {
        // Given
        store.setIfAbsent("key1", Duration.ofMillis(50));

        // When - 만료 대기
        Thread.sleep(100);

        // Then
        assertFalse(store.exists("key1"));
    }

    @Test
    @DisplayName("만료된 키의 결과 조회 시 empty 반환")
    void testExpiredResultReturnsEmpty() throws InterruptedException {
        // Given
        store.saveResult("key1", "result", Duration.ofMillis(50));

        // When - 만료 대기
        Thread.sleep(100);

        // Then
        Optional<Object> result = store.getResult("key1");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("만료된 키 이후 setIfAbsent 성공")
    void testSetIfAbsentAfterExpiry() throws InterruptedException {
        // Given
        store.setIfAbsent("key1", Duration.ofMillis(50));

        // When - 만료 대기
        Thread.sleep(100);

        // Then - 새로 설정 가능
        boolean result = store.setIfAbsent("key1", Duration.ofMinutes(5));
        assertTrue(result);
    }

    @Test
    @DisplayName("다양한 타입의 결과 저장")
    void testSaveVariousTypes() {
        // Given
        store.saveResult("string", "text", Duration.ofMinutes(5));
        store.saveResult("integer", 123, Duration.ofMinutes(5));
        store.saveResult("object", new TestResult("test", 456), Duration.ofMinutes(5));

        // Then
        assertEquals("text", store.getResult("string").orElse(null));
        assertEquals(123, store.getResult("integer").orElse(null));
        assertTrue(store.getResult("object").orElse(null) instanceof TestResult);
    }

    @Test
    @DisplayName("null 결과 저장")
    void testSaveNullResult() {
        // When
        store.saveResult("key1", null, Duration.ofMinutes(5));

        // Then
        Optional<Object> result = store.getResult("key1");
        assertFalse(result.isPresent()); // null이 저장된 경우 empty 반환
    }

    @Test
    @DisplayName("결과 덮어쓰기")
    void testOverwriteResult() {
        // Given
        store.saveResult("key1", "first", Duration.ofMinutes(5));

        // When
        store.saveResult("key1", "second", Duration.ofMinutes(5));

        // Then
        assertEquals("second", store.getResult("key1").orElse(null));
    }

    // Test helper class
    public static class TestResult {
        private String name;
        private int value;

        public TestResult(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
