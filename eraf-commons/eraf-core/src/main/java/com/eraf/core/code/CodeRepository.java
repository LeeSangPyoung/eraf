package com.eraf.core.code;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 저장소 인터페이스
 * 프로젝트에서 구현하여 사용
 */
public interface CodeRepository {

    /**
     * 코드 그룹과 코드로 존재 여부 확인
     *
     * @param group        코드 그룹
     * @param code         코드
     * @param checkEnabled 사용 여부 확인
     * @return 존재 여부
     */
    boolean existsByGroupAndCode(String group, String code, boolean checkEnabled);

    /**
     * 코드 그룹의 모든 코드 목록 조회
     *
     * @param group 코드 그룹
     * @return 코드 목록
     */
    List<CodeItem> findByGroup(String group);

    /**
     * 코드 그룹의 사용 가능한 코드 목록 조회
     *
     * @param group 코드 그룹
     * @return 코드 목록
     */
    List<CodeItem> findEnabledByGroup(String group);

    /**
     * 단건 코드 조회
     *
     * @param group 코드 그룹
     * @param code  코드
     * @return 코드 정보
     */
    Optional<CodeItem> findByGroupAndCode(String group, String code);
}
