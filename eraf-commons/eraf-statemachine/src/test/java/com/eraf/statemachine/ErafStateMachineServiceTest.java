package com.eraf.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafStateMachineServiceTest {

    private ErafStateMachineService service;
    private ErafStateMachineRegistry registry;
    private StateStore stateStore;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        registry = new ErafStateMachineRegistry();
        service = new ErafStateMachineService(registry, stateStore);

        // 주문 상태머신 등록
        StateMachineDefinition orderDef = StateMachineDefinition.builder()
                .name("order")
                .initialState("CREATED")
                .build();
        orderDef.addTransition("CREATED", "CONFIRMED", "confirm");
        orderDef.addTransition("CONFIRMED", "SHIPPED", "ship");
        orderDef.addTransition("SHIPPED", "DELIVERED", "deliver");
        orderDef.addTransition("CREATED", "CANCELLED", "cancel");
        registry.register(orderDef);
    }

    @Test
    @DisplayName("초기 상태 생성")
    void testInitialize() {
        // When
        String state = service.initialize("order", "ORDER-001");

        // Then
        assertEquals("CREATED", state);
        assertEquals("CREATED", service.getState("order", "ORDER-001"));
    }

    @Test
    @DisplayName("상태 전이")
    void testTransition() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        String newState = service.transition("order", "ORDER-001", "confirm");

        // Then
        assertEquals("CONFIRMED", newState);
        assertEquals("CONFIRMED", service.getState("order", "ORDER-001"));
    }

    @Test
    @DisplayName("여러 단계 전이")
    void testMultipleTransitions() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        service.transition("order", "ORDER-001", "confirm");
        service.transition("order", "ORDER-001", "ship");
        String finalState = service.transition("order", "ORDER-001", "deliver");

        // Then
        assertEquals("DELIVERED", finalState);
    }

    @Test
    @DisplayName("잘못된 전이 시 예외")
    void testInvalidTransition() {
        // Given
        service.initialize("order", "ORDER-001");

        // When & Then
        assertThrows(StateMachineException.class, () -> {
            service.transition("order", "ORDER-001", "ship"); // CREATED -> SHIPPED 불가
        });
    }

    @Test
    @DisplayName("전이 가능 여부 확인")
    void testCanTransition() {
        // Given
        service.initialize("order", "ORDER-001");

        // Then
        assertTrue(service.canTransition("order", "ORDER-001", "confirm"));
        assertTrue(service.canTransition("order", "ORDER-001", "cancel"));
        assertFalse(service.canTransition("order", "ORDER-001", "ship"));
    }

    @Test
    @DisplayName("허용된 이벤트 목록")
    void testGetAllowedEvents() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        var events = service.getAllowedEvents("order", "ORDER-001");

        // Then
        assertEquals(2, events.size());
        assertTrue(events.contains("confirm"));
        assertTrue(events.contains("cancel"));
    }
}
