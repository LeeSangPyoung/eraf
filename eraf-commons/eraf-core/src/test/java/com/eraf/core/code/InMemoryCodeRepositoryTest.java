package com.eraf.core.code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCodeRepositoryTest {

    private InMemoryCodeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCodeRepository();
    }

    @Test
    @DisplayName("코드 추가")
    void testAddCode() {
        // Given
        CodeItem item = CodeItem.builder()
                .group("TEST")
                .code("CODE1")
                .name("테스트 코드")
                .build();

        // When
        repository.addCode(item);

        // Then
        assertTrue(repository.existsByGroupAndCode("TEST", "CODE1", false));
    }

    @Test
    @DisplayName("코드 목록 추가")
    void testAddCodes() {
        // Given
        List<CodeItem> items = List.of(
                CodeItem.builder().group("TEST").code("A").name("A").build(),
                CodeItem.builder().group("TEST").code("B").name("B").build()
        );

        // When
        repository.addCodes(items);

        // Then
        assertEquals(2, repository.findByGroup("TEST").size());
    }

    @Test
    @DisplayName("그룹별 코드 조회 - 정렬 순서")
    void testFindByGroupSorted() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("C").name("C").sortOrder(3).enabled(true).build());
        repository.addCode(CodeItem.builder().group("TEST").code("A").name("A").sortOrder(1).enabled(true).build());
        repository.addCode(CodeItem.builder().group("TEST").code("B").name("B").sortOrder(2).enabled(true).build());

        // When
        List<CodeItem> items = repository.findByGroup("TEST");

        // Then
        assertEquals(3, items.size());
        assertEquals("A", items.get(0).getCode());
        assertEquals("B", items.get(1).getCode());
        assertEquals("C", items.get(2).getCode());
    }

    @Test
    @DisplayName("활성 코드만 조회")
    void testFindEnabledByGroup() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("A").name("A").enabled(true).build());
        repository.addCode(CodeItem.builder().group("TEST").code("B").name("B").enabled(false).build());
        repository.addCode(CodeItem.builder().group("TEST").code("C").name("C").enabled(true).build());

        // When
        List<CodeItem> enabled = repository.findEnabledByGroup("TEST");

        // Then
        assertEquals(2, enabled.size());
        assertTrue(enabled.stream().allMatch(CodeItem::isEnabled));
    }

    @Test
    @DisplayName("그룹과 코드로 조회")
    void testFindByGroupAndCode() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("CODE1").name("테스트").build());

        // When
        Optional<CodeItem> result = repository.findByGroupAndCode("TEST", "CODE1");

        // Then
        assertTrue(result.isPresent());
        assertEquals("테스트", result.get().getName());
    }

    @Test
    @DisplayName("존재하지 않는 코드 조회")
    void testFindByGroupAndCodeNotFound() {
        // When
        Optional<CodeItem> result = repository.findByGroupAndCode("TEST", "NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("코드 존재 여부 확인 - enabled 체크 없이")
    void testExistsWithoutEnabledCheck() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("DISABLED").name("비활성").enabled(false).build());

        // Then
        assertTrue(repository.existsByGroupAndCode("TEST", "DISABLED", false));
    }

    @Test
    @DisplayName("코드 존재 여부 확인 - enabled 체크")
    void testExistsWithEnabledCheck() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("ACTIVE").name("활성").enabled(true).build());
        repository.addCode(CodeItem.builder().group("TEST").code("DISABLED").name("비활성").enabled(false).build());

        // Then
        assertTrue(repository.existsByGroupAndCode("TEST", "ACTIVE", true));
        assertFalse(repository.existsByGroupAndCode("TEST", "DISABLED", true));
    }

    @Test
    @DisplayName("코드 제거")
    void testRemoveCode() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("CODE1").name("테스트").build());

        // When
        repository.removeCode("TEST", "CODE1");

        // Then
        assertFalse(repository.existsByGroupAndCode("TEST", "CODE1", false));
    }

    @Test
    @DisplayName("그룹 전체 제거")
    void testRemoveGroup() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST").code("A").name("A").build());
        repository.addCode(CodeItem.builder().group("TEST").code("B").name("B").build());

        // When
        repository.removeGroup("TEST");

        // Then
        assertTrue(repository.findByGroup("TEST").isEmpty());
    }

    @Test
    @DisplayName("전체 초기화")
    void testClear() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST1").code("A").name("A").build());
        repository.addCode(CodeItem.builder().group("TEST2").code("B").name("B").build());

        // When
        repository.clear();

        // Then
        assertEquals(0, repository.getGroupCount());
        assertEquals(0, repository.getTotalCodeCount());
    }

    @Test
    @DisplayName("그룹 수 조회")
    void testGetGroupCount() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST1").code("A").name("A").build());
        repository.addCode(CodeItem.builder().group("TEST2").code("B").name("B").build());
        repository.addCode(CodeItem.builder().group("TEST1").code("C").name("C").build());

        // Then
        assertEquals(2, repository.getGroupCount());
    }

    @Test
    @DisplayName("전체 코드 수 조회")
    void testGetTotalCodeCount() {
        // Given
        repository.addCode(CodeItem.builder().group("TEST1").code("A").name("A").build());
        repository.addCode(CodeItem.builder().group("TEST1").code("B").name("B").build());
        repository.addCode(CodeItem.builder().group("TEST2").code("C").name("C").build());

        // Then
        assertEquals(3, repository.getTotalCodeCount());
    }

    @Test
    @DisplayName("빈 그룹 조회")
    void testFindByEmptyGroup() {
        // When
        List<CodeItem> result = repository.findByGroup("NONEXISTENT");

        // Then
        assertTrue(result.isEmpty());
    }
}
