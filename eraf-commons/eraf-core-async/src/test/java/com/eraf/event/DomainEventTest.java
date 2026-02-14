package com.eraf.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DomainEvent - 도메인 이벤트 베이스")
class DomainEventTest {

    static class OrderCreatedEvent extends DomainEvent {
        private final String orderId;

        OrderCreatedEvent(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderId() {
            return orderId;
        }
    }

    static class UserRegisteredEvent extends DomainEvent {
    }

    @Test
    @DisplayName("이벤트 ID 자동 생성")
    void eventIdGenerated() {
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-001");
        assertNotNull(event.getEventId());
        assertFalse(event.getEventId().isEmpty());
    }

    @Test
    @DisplayName("이벤트 ID 유일성")
    void eventIdUnique() {
        OrderCreatedEvent e1 = new OrderCreatedEvent("ORD-001");
        OrderCreatedEvent e2 = new OrderCreatedEvent("ORD-002");
        assertNotEquals(e1.getEventId(), e2.getEventId());
    }

    @Test
    @DisplayName("타임스탬프 자동 설정")
    void timestampSet() {
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-001");
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("이벤트 타입 = 클래스 이름")
    void eventType() {
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-001");
        assertEquals("OrderCreatedEvent", event.getEventType());

        UserRegisteredEvent event2 = new UserRegisteredEvent();
        assertEquals("UserRegisteredEvent", event2.getEventType());
    }

    @Test
    @DisplayName("커스텀 필드 접근")
    void customField() {
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-001");
        assertEquals("ORD-001", event.getOrderId());
    }
}
