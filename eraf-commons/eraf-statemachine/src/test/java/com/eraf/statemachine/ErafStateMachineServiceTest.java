package com.eraf.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErafStateMachineServiceTest {

    private ErafStateMachineService service;
    private ErafStateMachineRegistry registry;
    private StateStore stateStore;
    private ApplicationEventPublisher eventPublisher;

    private TransitionInfo createTransition(String source, String target, String event) {
        TransitionInfo t = new TransitionInfo();
        t.setSource(source);
        t.setTarget(target);
        t.setEvent(event);
        return t;
    }

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        registry = new ErafStateMachineRegistry();
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ErafStateMachineService(registry, eventPublisher, stateStore);

        // 주문 상태머신 등록
        StateMachineDefinition orderDef = new StateMachineDefinition();
        orderDef.setId("order");
        orderDef.setInitialState("CREATED");
        orderDef.setStates(Set.of("CREATED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"));
        orderDef.setEndStates(Set.of("DELIVERED", "CANCELLED"));
        orderDef.addTransition(createTransition("CREATED", "CONFIRMED", "confirm"));
        orderDef.addTransition(createTransition("CONFIRMED", "SHIPPED", "ship"));
        orderDef.addTransition(createTransition("SHIPPED", "DELIVERED", "deliver"));
        orderDef.addTransition(createTransition("CREATED", "CANCELLED", "cancel"));
        registry.register(orderDef);
    }

    @Test
    @DisplayName("초기 상태 생성")
    void testInitialize() {
        // When
        StateInfo stateInfo = service.initialize("order", "ORDER-001");

        // Then
        assertEquals("CREATED", stateInfo.getCurrentState());
        assertEquals("CREATED", service.getCurrentState("order", "ORDER-001").orElse(null));
    }

    @Test
    @DisplayName("상태 전이")
    void testTransition() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        StateInfo stateInfo = service.sendEvent("order", "ORDER-001", "confirm");

        // Then
        assertEquals("CONFIRMED", stateInfo.getCurrentState());
        assertEquals("CONFIRMED", service.getCurrentState("order", "ORDER-001").orElse(null));
    }

    @Test
    @DisplayName("여러 단계 전이")
    void testMultipleTransitions() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        service.sendEvent("order", "ORDER-001", "confirm");
        service.sendEvent("order", "ORDER-001", "ship");
        StateInfo finalState = service.sendEvent("order", "ORDER-001", "deliver");

        // Then
        assertEquals("DELIVERED", finalState.getCurrentState());
    }

    @Test
    @DisplayName("잘못된 전이 시 예외")
    void testInvalidTransition() {
        // Given
        service.initialize("order", "ORDER-001");

        // When & Then
        assertThrows(StateMachineException.class, () -> {
            service.sendEvent("order", "ORDER-001", "ship"); // CREATED -> SHIPPED 불가
        });
    }

    @Test
    @DisplayName("전이 가능 여부 확인")
    void testCanSendEvent() {
        // Given
        service.initialize("order", "ORDER-001");

        // Then
        assertTrue(service.canSendEvent("order", "ORDER-001", "confirm"));
        assertTrue(service.canSendEvent("order", "ORDER-001", "cancel"));
        assertFalse(service.canSendEvent("order", "ORDER-001", "ship"));
    }

    @Test
    @DisplayName("허용된 이벤트 목록")
    void testGetAvailableEvents() {
        // Given
        service.initialize("order", "ORDER-001");

        // When
        List<String> events = service.getAvailableEvents("order", "ORDER-001");

        // Then
        assertEquals(2, events.size());
        assertTrue(events.contains("confirm"));
        assertTrue(events.contains("cancel"));
    }
}
