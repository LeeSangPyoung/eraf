package com.eraf.starter.statemachine;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ERAF 상태 머신 레지스트리
 * 등록된 상태 머신 정의 관리
 */
public class ErafStateMachineRegistry {

    private final Map<String, StateMachineDefinition> definitions = new ConcurrentHashMap<>();

    /**
     * 상태 머신 정의 등록
     */
    public void register(StateMachineDefinition definition) {
        definitions.put(definition.getId(), definition);
    }

    /**
     * 상태 머신 정의 조회
     */
    public Optional<StateMachineDefinition> getDefinition(String machineId) {
        return Optional.ofNullable(definitions.get(machineId));
    }

    /**
     * 모든 상태 머신 정의 조회
     */
    public Collection<StateMachineDefinition> getAllDefinitions() {
        return definitions.values();
    }

    /**
     * 상태 머신 정의 존재 여부
     */
    public boolean exists(String machineId) {
        return definitions.containsKey(machineId);
    }

    /**
     * 상태 머신 정의 제거
     */
    public void unregister(String machineId) {
        definitions.remove(machineId);
    }

    /**
     * 등록된 상태 머신 수
     */
    public int count() {
        return definitions.size();
    }
}
