package com.eraf.code;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 서비스
 */
public class CodeService {

    private final CodeRepository codeRepository;

    public CodeService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    /**
     * 코드 그룹의 모든 코드 목록 조회
     */
    public List<CodeItem> getByGroup(String group) {
        return codeRepository.findByGroup(group);
    }

    /**
     * 코드 그룹의 사용 가능한 코드 목록 조회
     */
    public List<CodeItem> getEnabledByGroup(String group) {
        return codeRepository.findEnabledByGroup(group);
    }

    /**
     * 단건 코드 조회
     */
    public Optional<CodeItem> get(String group, String code) {
        return codeRepository.findByGroupAndCode(group, code);
    }

    /**
     * 코드명 조회
     */
    public String getName(String group, String code) {
        return get(group, code)
                .map(CodeItem::getName)
                .orElse(null);
    }

    /**
     * 코드 존재 여부 확인
     */
    public boolean exists(String group, String code) {
        return codeRepository.existsByGroupAndCode(group, code, false);
    }

    /**
     * 코드 사용 가능 여부 확인
     */
    public boolean isEnabled(String group, String code) {
        return codeRepository.existsByGroupAndCode(group, code, true);
    }

    /**
     * 코드 생성
     *
     * @param codeItem 생성할 코드 아이템
     * @throws IllegalArgumentException 이미 존재하는 코드인 경우
     */
    public void create(CodeItem codeItem) {
        if (exists(codeItem.getGroup(), codeItem.getCode())) {
            throw new IllegalArgumentException(
                    "Code already exists: group=" + codeItem.getGroup() + ", code=" + codeItem.getCode());
        }
        codeRepository.save(codeItem);
    }

    /**
     * 코드 수정
     *
     * @param codeItem 수정할 코드 아이템
     * @throws IllegalArgumentException 존재하지 않는 코드인 경우
     */
    public void update(CodeItem codeItem) {
        if (!exists(codeItem.getGroup(), codeItem.getCode())) {
            throw new IllegalArgumentException(
                    "Code not found: group=" + codeItem.getGroup() + ", code=" + codeItem.getCode());
        }
        codeRepository.save(codeItem);
    }

    /**
     * 코드 저장 (존재하면 수정, 없으면 생성)
     *
     * @param codeItem 저장할 코드 아이템
     */
    public void saveOrUpdate(CodeItem codeItem) {
        codeRepository.save(codeItem);
    }

    /**
     * 코드 삭제
     *
     * @param group 코드 그룹
     * @param code  코드
     */
    public void delete(String group, String code) {
        codeRepository.delete(group, code);
    }

    /**
     * 코드 그룹 전체 삭제
     *
     * @param group 코드 그룹
     */
    public void deleteByGroup(String group) {
        codeRepository.deleteByGroup(group);
    }

    /**
     * 코드 활성/비활성 토글
     *
     * @param group   코드 그룹
     * @param code    코드
     * @param enabled 활성화 여부
     */
    public void setEnabled(String group, String code, boolean enabled) {
        CodeItem item = get(group, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Code not found: group=" + group + ", code=" + code));
        item.setEnabled(enabled);
        codeRepository.save(item);
    }
}
