package com.eraf.core.code;

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
}
