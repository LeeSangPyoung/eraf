package com.eraf.statemachine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineDefinitionTest {

    private TransitionInfo createTransition(String source, String target, String event) {
        TransitionInfo t = new TransitionInfo();
        t.setSource(source);
        t.setTarget(target);
        t.setEvent(event);
        return t;
    }

    @Test
    @DisplayName("StateMachine 정의 생성")
    void testCreate() {
        // Given & When
        StateMachineDefinition definition = new StateMachineDefinition();
        definition.setId("orderStateMachine");
        definition.setInitialState("CREATED");
        definition.setStates(Set.of("CREATED", "CONFIRMED", "SHIPPED", "DELIVERED"));

        // Then
        assertNotNull(definition);
        assertEquals("orderStateMachine", definition.getId());
        assertEquals("CREATED", definition.getInitialState());
    }

    @Test
    @DisplayName("전이 추가")
    void testAddTransition() {
        // Given
        StateMachineDefinition definition = new StateMachineDefinition();
        definition.setId("orderStateMachine");
        definition.setInitialState("CREATED");

        // When
        definition.addTransition(createTransition("CREATED", "CONFIRMED", "confirm"));
        definition.addTransition(createTransition("CONFIRMED", "SHIPPED", "ship"));

        // Then
        assertTrue(definition.findTransition("CREATED", "confirm").isPresent());
        assertTrue(definition.findTransition("CONFIRMED", "ship").isPresent());
        assertFalse(definition.findTransition("CREATED", "ship").isPresent());
    }

    @Test
    @DisplayName("다음 상태 조회")
    void testGetNextState() {
        // Given
        StateMachineDefinition definition = new StateMachineDefinition();
        definition.setId("orderStateMachine");
        definition.setInitialState("CREATED");
        definition.addTransition(createTransition("CREATED", "CONFIRMED", "confirm"));

        // When
        Optional<TransitionInfo> transition = definition.findTransition("CREATED", "confirm");

        // Then
        assertTrue(transition.isPresent());
        assertEquals("CONFIRMED", transition.get().getTarget());
    }

    @Test
    @DisplayName("허용된 이벤트 목록 조회")
    void testGetAllowedEvents() {
        // Given
        StateMachineDefinition definition = new StateMachineDefinition();
        definition.setId("orderStateMachine");
        definition.setInitialState("CREATED");
        definition.addTransition(createTransition("CREATED", "CONFIRMED", "confirm"));
        definition.addTransition(createTransition("CREATED", "CANCELLED", "cancel"));

        // When
        List<String> allowedEvents = definition.getTransitionsFrom("CREATED").stream()
                .map(TransitionInfo::getEvent)
                .toList();

        // Then
        assertEquals(2, allowedEvents.size());
        assertTrue(allowedEvents.contains("confirm"));
        assertTrue(allowedEvents.contains("cancel"));
    }
}
