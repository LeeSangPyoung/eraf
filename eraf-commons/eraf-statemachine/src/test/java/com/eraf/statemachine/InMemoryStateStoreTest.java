package com.eraf.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStateStoreTest {

    private InMemoryStateStore stateStore;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
    }

    @Test
    @DisplayName("상태 저장 및 조회")
    void testSaveAndGet() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";
        String state = "PENDING";

        // When
        stateStore.save(machineId, entityId, state);
        Optional<String> result = stateStore.get(machineId, entityId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(state, result.get());
    }

    @Test
    @DisplayName("존재하지 않는 상태 조회")
    void testGetNonExistent() {
        // When
        Optional<String> result = stateStore.get("order", "NON-EXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("상태 업데이트")
    void testUpdate() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";

        stateStore.save(machineId, entityId, "PENDING");

        // When
        stateStore.save(machineId, entityId, "CONFIRMED");
        Optional<String> result = stateStore.get(machineId, entityId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("CONFIRMED", result.get());
    }

    @Test
    @DisplayName("상태 삭제")
    void testDelete() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";
        stateStore.save(machineId, entityId, "PENDING");

        // When
        stateStore.delete(machineId, entityId);
        Optional<String> result = stateStore.get(machineId, entityId);

        // Then
        assertFalse(result.isPresent());
    }
}
