package com.eraf.code;

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

    /**
     * 코드 저장 (생성/수정)
     *
     * @param codeItem 저장할 코드 아이템
     */
    default void save(CodeItem codeItem) {
        throw new UnsupportedOperationException("save() is not implemented");
    }

    /**
     * 코드 삭제
     *
     * @param group 코드 그룹
     * @param code  코드
     */
    default void delete(String group, String code) {
        throw new UnsupportedOperationException("delete() is not implemented");
    }

    /**
     * 코드 그룹 전체 삭제
     *
     * @param group 코드 그룹
     */
    default void deleteByGroup(String group) {
        throw new UnsupportedOperationException("deleteByGroup() is not implemented");
    }
}
