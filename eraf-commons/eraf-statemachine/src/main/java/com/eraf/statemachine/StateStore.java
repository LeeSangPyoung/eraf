package com.eraf.statemachine;

import java.util.Optional;

/**
 * 상태 저장소 인터페이스
 * 분산 환경을 위한 추상화 계층
 */
public interface StateStore {

    /**
     * 상태 저장
     *
     * @param machineId 상태 머신 ID
     * @param entityId  엔티티 ID
     * @param stateInfo 상태 정보
     */
    void save(String machineId, String entityId, StateInfo stateInfo);

    /**
     * 상태 조회
     *
     * @param machineId 상태 머신 ID
     * @param entityId  엔티티 ID
     * @return 상태 정보 (Optional)
     */
    Optional<StateInfo> find(String machineId, String entityId);

    /**
     * 상태 삭제
     *
     * @param machineId 상태 머신 ID
     * @param entityId  엔티티 ID
     */
    void remove(String machineId, String entityId);

    /**
     * 상태 존재 여부 확인
     *
     * @param machineId 상태 머신 ID
     * @param entityId  엔티티 ID
     * @return 존재 여부
     */
    boolean exists(String machineId, String entityId);

    /**
     * CAS(Compare-And-Swap) 기반 상태 저장
     *
     * <p>현재 저장된 상태가 expectedState와 일치할 때만 저장합니다.
     * 동시 전이 충돌을 감지하여 데이터 무결성을 보장합니다.</p>
     *
     * @param machineId     상태 머신 ID
     * @param entityId      엔티티 ID
     * @param stateInfo     저장할 상태 정보
     * @param expectedState 기대하는 이전 상태
     * @return 저장 성공 여부 (false면 동시 전이 충돌)
     */
    default boolean saveIfCurrentState(String machineId, String entityId, StateInfo stateInfo, String expectedState) {
        // 기본 구현: 낙관적 검증 후 저장
        Optional<StateInfo> current = find(machineId, entityId);
        if (current.isPresent() && !expectedState.equals(current.get().getCurrentState())) {
            return false;
        }
        save(machineId, entityId, stateInfo);
        return true;
    }

    /**
     * 저장소 키 생성
     */
    default String createKey(String machineId, String entityId) {
        return machineId + ":" + entityId;
    }
}
