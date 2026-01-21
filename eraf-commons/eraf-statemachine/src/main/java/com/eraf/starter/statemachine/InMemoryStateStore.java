package com.eraf.starter.statemachine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 상태 저장소 (기본 구현)
 * 단일 인스턴스 환경에서 사용
 */
public class InMemoryStateStore implements StateStore {

    private final Map<String, StateInfo> store = new ConcurrentHashMap<>();

    @Override
    public void save(String machineId, String entityId, StateInfo stateInfo) {
        String key = createKey(machineId, entityId);
        store.put(key, stateInfo);
    }

    @Override
    public Optional<StateInfo> find(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void remove(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        store.remove(key);
    }

    @Override
    public boolean exists(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        return store.containsKey(key);
    }

    /**
     * 전체 상태 삭제 (테스트용)
     */
    public void clear() {
        store.clear();
    }

    /**
     * 저장된 상태 수 조회
     */
    public int size() {
        return store.size();
    }
}
