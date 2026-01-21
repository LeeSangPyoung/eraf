package com.eraf.starter.database.code;

import com.eraf.core.code.CodeItem;
import com.eraf.core.code.CodeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA 기반 공통코드 저장소 구현
 */
public class JpaCodeRepository implements CodeRepository {

    private final CommonCodeJpaRepository jpaRepository;

    public JpaCodeRepository(CommonCodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByGroupAndCode(String group, String code, boolean checkEnabled) {
        if (checkEnabled) {
            return jpaRepository.existsByCodeGroupAndCodeAndEnabledTrue(group, code);
        }
        return jpaRepository.existsByCodeGroupAndCode(group, code);
    }

    @Override
    public List<CodeItem> findByGroup(String group) {
        return jpaRepository.findByCodeGroupOrderBySortOrderAsc(group)
                .stream()
                .map(this::toCodeItem)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeItem> findEnabledByGroup(String group) {
        return jpaRepository.findByCodeGroupAndEnabledTrueOrderBySortOrderAsc(group)
                .stream()
                .map(this::toCodeItem)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CodeItem> findByGroupAndCode(String group, String code) {
        return jpaRepository.findByCodeGroupAndCode(group, code)
                .map(this::toCodeItem);
    }

    private CodeItem toCodeItem(CommonCodeEntity entity) {
        return CodeItem.builder()
                .group(entity.getCodeGroup())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder() != null ? entity.getSortOrder() : 0)
                .enabled(entity.getEnabled() != null ? entity.getEnabled() : true)
                .build();
    }
}
