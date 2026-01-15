package com.eraf.core.code;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 공통코드 저장소 구현
 * 테스트/개발 환경에서 사용
 */
public class InMemoryCodeRepository implements CodeRepository {

    private final Map<String, Map<String, CodeItem>> codeStore = new ConcurrentHashMap<>();

    @Override
    public boolean existsByGroupAndCode(String group, String code, boolean checkEnabled) {
        Map<String, CodeItem> groupCodes = codeStore.get(group);
        if (groupCodes == null) {
            return false;
        }

        CodeItem item = groupCodes.get(code);
        if (item == null) {
            return false;
        }

        if (checkEnabled) {
            return item.isEnabled();
        }
        return true;
    }

    @Override
    public List<CodeItem> findByGroup(String group) {
        Map<String, CodeItem> groupCodes = codeStore.get(group);
        if (groupCodes == null) {
            return Collections.emptyList();
        }

        return groupCodes.values().stream()
                .sorted(Comparator.comparingInt(CodeItem::getSortOrder))
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeItem> findEnabledByGroup(String group) {
        return findByGroup(group).stream()
                .filter(CodeItem::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CodeItem> findByGroupAndCode(String group, String code) {
        Map<String, CodeItem> groupCodes = codeStore.get(group);
        if (groupCodes == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(groupCodes.get(code));
    }

    /**
     * 코드 추가
     */
    public void addCode(CodeItem codeItem) {
        codeStore.computeIfAbsent(codeItem.getGroup(), k -> new ConcurrentHashMap<>())
                .put(codeItem.getCode(), codeItem);
    }

    /**
     * 코드 목록 추가
     */
    public void addCodes(List<CodeItem> codeItems) {
        for (CodeItem item : codeItems) {
            addCode(item);
        }
    }

    /**
     * 코드 제거
     */
    public void removeCode(String group, String code) {
        Map<String, CodeItem> groupCodes = codeStore.get(group);
        if (groupCodes != null) {
            groupCodes.remove(code);
        }
    }

    /**
     * 그룹 전체 제거
     */
    public void removeGroup(String group) {
        codeStore.remove(group);
    }

    /**
     * 전체 초기화
     */
    public void clear() {
        codeStore.clear();
    }

    /**
     * 그룹 수 조회
     */
    public int getGroupCount() {
        return codeStore.size();
    }

    /**
     * 전체 코드 수 조회
     */
    public int getTotalCodeCount() {
        return codeStore.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
}
