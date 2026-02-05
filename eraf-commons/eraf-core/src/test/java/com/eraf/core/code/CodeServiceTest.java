package com.eraf.core.code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CodeServiceTest {

    private InMemoryCodeRepository repository;
    private CodeService codeService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCodeRepository();
        codeService = new CodeService(repository);

        // 테스트 데이터 추가
        repository.addCode(CodeItem.builder()
                .group("STATUS")
                .code("ACTIVE")
                .name("활성")
                .sortOrder(1)
                .enabled(true)
                .build());

        repository.addCode(CodeItem.builder()
                .group("STATUS")
                .code("INACTIVE")
                .name("비활성")
                .sortOrder(2)
                .enabled(true)
                .build());

        repository.addCode(CodeItem.builder()
                .group("STATUS")
                .code("DELETED")
                .name("삭제됨")
                .sortOrder(3)
                .enabled(false)
                .build());

        repository.addCode(CodeItem.builder()
                .group("ORDER_TYPE")
                .code("NORMAL")
                .name("일반주문")
                .sortOrder(1)
                .enabled(true)
                .build());
    }

    @Test
    @DisplayName("그룹별 코드 목록 조회")
    void testGetByGroup() {
        // When
        List<CodeItem> codes = codeService.getByGroup("STATUS");

        // Then
        assertEquals(3, codes.size());
    }

    @Test
    @DisplayName("그룹별 활성 코드만 조회")
    void testGetEnabledByGroup() {
        // When
        List<CodeItem> codes = codeService.getEnabledByGroup("STATUS");

        // Then
        assertEquals(2, codes.size());
        assertTrue(codes.stream().allMatch(CodeItem::isEnabled));
    }

    @Test
    @DisplayName("단건 코드 조회")
    void testGet() {
        // When
        Optional<CodeItem> code = codeService.get("STATUS", "ACTIVE");

        // Then
        assertTrue(code.isPresent());
        assertEquals("활성", code.get().getName());
    }

    @Test
    @DisplayName("존재하지 않는 코드 조회")
    void testGetNotFound() {
        // When
        Optional<CodeItem> code = codeService.get("STATUS", "NONEXISTENT");

        // Then
        assertFalse(code.isPresent());
    }

    @Test
    @DisplayName("코드명 조회")
    void testGetName() {
        // When
        String name = codeService.getName("STATUS", "ACTIVE");

        // Then
        assertEquals("활성", name);
    }

    @Test
    @DisplayName("존재하지 않는 코드명 조회")
    void testGetNameNotFound() {
        // When
        String name = codeService.getName("STATUS", "NONEXISTENT");

        // Then
        assertNull(name);
    }

    @Test
    @DisplayName("코드 존재 여부 확인")
    void testExists() {
        // Then
        assertTrue(codeService.exists("STATUS", "ACTIVE"));
        assertTrue(codeService.exists("STATUS", "DELETED")); // 비활성 코드도 존재함
        assertFalse(codeService.exists("STATUS", "NONEXISTENT"));
    }

    @Test
    @DisplayName("코드 사용 가능 여부 확인")
    void testIsEnabled() {
        // Then
        assertTrue(codeService.isEnabled("STATUS", "ACTIVE"));
        assertFalse(codeService.isEnabled("STATUS", "DELETED")); // 비활성
        assertFalse(codeService.isEnabled("STATUS", "NONEXISTENT")); // 존재하지 않음
    }

    @Test
    @DisplayName("존재하지 않는 그룹 조회")
    void testNonexistentGroup() {
        // When
        List<CodeItem> codes = codeService.getByGroup("NONEXISTENT");

        // Then
        assertTrue(codes.isEmpty());
    }
}
