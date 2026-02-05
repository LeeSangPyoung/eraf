package com.eraf.statemachine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StateChangeEventTest {

    @Test
    @DisplayName("상태 변경 이벤트 생성")
    void testCreate() {
        // Given & When
        StateChangeEvent event = StateChangeEvent.builder()
                .machineId("order")
                .entityId("ORDER-001")
                .fromState("CREATED")
                .toState("CONFIRMED")
                .event("confirm")
                .build();

        // Then
        assertNotNull(event);
        assertEquals("order", event.getMachineId());
        assertEquals("ORDER-001", event.getEntityId());
        assertEquals("CREATED", event.getFromState());
        assertEquals("CONFIRMED", event.getToState());
        assertEquals("confirm", event.getEvent());
    }

    @Test
    @DisplayName("타임스탬프 자동 설정")
    void testTimestamp() {
        // Given
        Instant before = Instant.now();

        // When
        StateChangeEvent event = StateChangeEvent.builder()
                .machineId("order")
                .entityId("ORDER-001")
                .fromState("CREATED")
                .toState("CONFIRMED")
                .event("confirm")
                .build();

        Instant after = Instant.now();

        // Then
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(before) || event.getTimestamp().equals(before));
        assertTrue(event.getTimestamp().isBefore(after) || event.getTimestamp().equals(after));
    }
}
