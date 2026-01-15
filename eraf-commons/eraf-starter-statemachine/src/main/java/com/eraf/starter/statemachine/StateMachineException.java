package com.eraf.starter.statemachine;

/**
 * 상태 머신 예외
 */
public class StateMachineException extends RuntimeException {

    private final String machineId;
    private final String currentState;
    private final String event;

    public StateMachineException(String message) {
        super(message);
        this.machineId = null;
        this.currentState = null;
        this.event = null;
    }

    public StateMachineException(String message, String machineId, String currentState, String event) {
        super(message);
        this.machineId = machineId;
        this.currentState = currentState;
        this.event = event;
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
        this.machineId = null;
        this.currentState = null;
        this.event = null;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getEvent() {
        return event;
    }

    public static StateMachineException machineNotFound(String machineId) {
        return new StateMachineException("State machine not found: " + machineId);
    }

    public static StateMachineException invalidState(String machineId, String state) {
        return new StateMachineException("Invalid state '" + state + "' for machine: " + machineId);
    }

    public static StateMachineException invalidTransition(String machineId, String currentState, String event) {
        return new StateMachineException(
                "No valid transition for event '" + event + "' from state '" + currentState + "'",
                machineId, currentState, event
        );
    }

    public static StateMachineException guardFailed(String machineId, String currentState, String event, String guard) {
        return new StateMachineException(
                "Guard condition failed: " + guard,
                machineId, currentState, event
        );
    }
}
