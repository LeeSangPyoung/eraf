package com.eraf.statemachine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineDefinitionTest {

    @Test
    @DisplayName("StateMachine 정의 생성")
    void testCreate() {
        // Given & When
        StateMachineDefinition definition = StateMachineDefinition.builder()
                .name("orderStateMachine")
                .initialState("CREATED")
                .build();

        // Then
        assertNotNull(definition);
        assertEquals("orderStateMachine", definition.getName());
        assertEquals("CREATED", definition.getInitialState());
    }

    @Test
    @DisplayName("전이 추가")
    void testAddTransition() {
        // Given
        StateMachineDefinition definition = StateMachineDefinition.builder()
                .name("orderStateMachine")
                .initialState("CREATED")
                .build();

        // When
        definition.addTransition("CREATED", "CONFIRMED", "confirm");
        definition.addTransition("CONFIRMED", "SHIPPED", "ship");

        // Then
        assertTrue(definition.canTransition("CREATED", "confirm"));
        assertTrue(definition.canTransition("CONFIRMED", "ship"));
        assertFalse(definition.canTransition("CREATED", "ship"));
    }

    @Test
    @DisplayName("다음 상태 조회")
    void testGetNextState() {
        // Given
        StateMachineDefinition definition = StateMachineDefinition.builder()
                .name("orderStateMachine")
                .initialState("CREATED")
                .build();
        definition.addTransition("CREATED", "CONFIRMED", "confirm");

        // When
        String nextState = definition.getNextState("CREATED", "confirm");

        // Then
        assertEquals("CONFIRMED", nextState);
    }

    @Test
    @DisplayName("허용된 이벤트 목록 조회")
    void testGetAllowedEvents() {
        // Given
        StateMachineDefinition definition = StateMachineDefinition.builder()
                .name("orderStateMachine")
                .initialState("CREATED")
                .build();
        definition.addTransition("CREATED", "CONFIRMED", "confirm");
        definition.addTransition("CREATED", "CANCELLED", "cancel");

        // When
        var allowedEvents = definition.getAllowedEvents("CREATED");

        // Then
        assertEquals(2, allowedEvents.size());
        assertTrue(allowedEvents.contains("confirm"));
        assertTrue(allowedEvents.contains("cancel"));
    }
}
