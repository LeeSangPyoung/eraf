package com.eraf.statemachine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateChangeEventTest {

    @Test
    @DisplayName("상태 변경 이벤트 생성")
    void testCreate() {
        // Given & When
        StateChangeEvent event = new StateChangeEvent(
                this, "order", "ORDER-001",
                "CREATED", "CONFIRMED", "confirm",
                Map.of()
        );

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
        StateChangeEvent event = new StateChangeEvent(
                this, "order", "ORDER-001",
                "CREATED", "CONFIRMED", "confirm",
                Map.of()
        );

        Instant after = Instant.now();

        // Then
        assertNotNull(event.getEventTime());
        assertTrue(event.getEventTime().isAfter(before) || event.getEventTime().equals(before));
        assertTrue(event.getEventTime().isBefore(after) || event.getEventTime().equals(after));
    }
}
