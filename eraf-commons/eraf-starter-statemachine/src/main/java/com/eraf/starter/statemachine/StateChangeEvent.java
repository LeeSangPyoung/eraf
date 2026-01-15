package com.eraf.starter.statemachine;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Map;

/**
 * 상태 변경 이벤트
 */
public class StateChangeEvent extends ApplicationEvent {

    private final String machineId;
    private final String entityId;
    private final String fromState;
    private final String toState;
    private final String event;
    private final Instant eventTime;
    private final Map<String, Object> context;

    public StateChangeEvent(Object source, String machineId, String entityId,
                            String fromState, String toState, String event,
                            Map<String, Object> context) {
        super(source);
        this.machineId = machineId;
        this.entityId = entityId;
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.eventTime = Instant.now();
        this.context = context;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getFromState() {
        return fromState;
    }

    public String getToState() {
        return toState;
    }

    public String getEvent() {
        return event;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
