package com.eraf.starter.statemachine;

import java.util.*;

/**
 * 상태 머신 정의
 */
public class StateMachineDefinition {

    private String id;
    private String initialState;
    private Set<String> states = new LinkedHashSet<>();
    private Set<String> endStates = new LinkedHashSet<>();
    private String description;
    private List<TransitionInfo> transitions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInitialState() {
        return initialState;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public Set<String> getStates() {
        return states;
    }

    public void setStates(Set<String> states) {
        this.states = states;
    }

    public Set<String> getEndStates() {
        return endStates;
    }

    public void setEndStates(Set<String> endStates) {
        this.endStates = endStates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TransitionInfo> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<TransitionInfo> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(TransitionInfo transition) {
        this.transitions.add(transition);
    }

    /**
     * 특정 상태에서 가능한 전이 목록 조회
     */
    public List<TransitionInfo> getTransitionsFrom(String state) {
        return transitions.stream()
                .filter(t -> t.getSource().equals(state))
                .toList();
    }

    /**
     * 특정 이벤트로 가능한 전이 조회
     */
    public Optional<TransitionInfo> findTransition(String currentState, String event) {
        return transitions.stream()
                .filter(t -> t.getSource().equals(currentState) && t.getEvent().equals(event))
                .findFirst();
    }

    /**
     * 종료 상태 여부 확인
     */
    public boolean isEndState(String state) {
        return endStates.contains(state);
    }

    /**
     * 유효한 상태인지 확인
     */
    public boolean isValidState(String state) {
        return states.contains(state);
    }
}
