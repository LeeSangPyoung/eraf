package com.eraf.starter.statemachine;

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
     * 저장소 키 생성
     */
    default String createKey(String machineId, String entityId) {
        return machineId + ":" + entityId;
    }
}
