package com.eraf.statemachine;

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
        StateInfo existing = store.get(key);
        if (existing == null) {
            return Optional.empty();
        }
        // 방어적 복사: 호출자가 객체를 수정해도 저장소의 원본에 영향을 주지 않도록 함
        // CAS(saveIfCurrentState) 비교 시 올바르게 동작하기 위해 필요
        StateInfo copy = new StateInfo();
        copy.setMachineId(existing.getMachineId());
        copy.setEntityId(existing.getEntityId());
        copy.setCurrentState(existing.getCurrentState());
        copy.setPreviousState(existing.getPreviousState());
        copy.setStateChangedAt(existing.getStateChangedAt());
        copy.setContext(new java.util.concurrent.ConcurrentHashMap<>(existing.getContext()));
        return Optional.of(copy);
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

    @Override
    public boolean saveIfCurrentState(String machineId, String entityId, StateInfo stateInfo, String expectedState) {
        String key = createKey(machineId, entityId);
        // ConcurrentHashMap의 compute를 사용한 원자적 CAS
        boolean[] success = {false};
        store.compute(key, (k, existing) -> {
            if (existing == null || expectedState.equals(existing.getCurrentState())) {
                success[0] = true;
                return stateInfo;
            }
            success[0] = false;
            return existing;
        });
        return success[0];
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
