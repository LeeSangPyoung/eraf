package com.eraf.starter.statemachine;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 상태 정보
 */
public class StateInfo {

    private String machineId;
    private String entityId;
    private String currentState;
    private String previousState;
    private Instant stateChangedAt;
    private Map<String, Object> context = new HashMap<>();

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public Instant getStateChangedAt() {
        return stateChangedAt;
    }

    public void setStateChangedAt(Instant stateChangedAt) {
        this.stateChangedAt = stateChangedAt;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key) {
        return (T) context.get(key);
    }

    public void setContextValue(String key, Object value) {
        this.context.put(key, value);
    }
}
