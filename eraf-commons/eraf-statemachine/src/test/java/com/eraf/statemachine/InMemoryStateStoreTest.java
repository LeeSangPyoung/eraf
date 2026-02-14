package com.eraf.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStateStoreTest {

    private InMemoryStateStore stateStore;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
    }

    private StateInfo createStateInfo(String machineId, String entityId, String currentState) {
        StateInfo info = new StateInfo();
        info.setMachineId(machineId);
        info.setEntityId(entityId);
        info.setCurrentState(currentState);
        info.setStateChangedAt(Instant.now());
        return info;
    }

    @Test
    @DisplayName("상태 저장 및 조회")
    void testSaveAndFind() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";
        StateInfo stateInfo = createStateInfo(machineId, entityId, "PENDING");

        // When
        stateStore.save(machineId, entityId, stateInfo);
        Optional<StateInfo> result = stateStore.find(machineId, entityId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("PENDING", result.get().getCurrentState());
    }

    @Test
    @DisplayName("존재하지 않는 상태 조회")
    void testFindNonExistent() {
        // When
        Optional<StateInfo> result = stateStore.find("order", "NON-EXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("상태 업데이트")
    void testUpdate() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";

        stateStore.save(machineId, entityId, createStateInfo(machineId, entityId, "PENDING"));

        // When
        stateStore.save(machineId, entityId, createStateInfo(machineId, entityId, "CONFIRMED"));
        Optional<StateInfo> result = stateStore.find(machineId, entityId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("CONFIRMED", result.get().getCurrentState());
    }

    @Test
    @DisplayName("상태 삭제")
    void testRemove() {
        // Given
        String machineId = "order";
        String entityId = "ORDER-123";
        stateStore.save(machineId, entityId, createStateInfo(machineId, entityId, "PENDING"));

        // When
        stateStore.remove(machineId, entityId);
        Optional<StateInfo> result = stateStore.find(machineId, entityId);

        // Then
        assertFalse(result.isPresent());
    }
}
