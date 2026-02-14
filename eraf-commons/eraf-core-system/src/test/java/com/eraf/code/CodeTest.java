package com.eraf.code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CodeTest {

    // ========== CodeItem ==========
    @Nested
    @DisplayName("CodeItem")
    class CodeItemTest {

        @Test
        @DisplayName("기본 생성자")
        void defaultConstructor() {
            CodeItem item = new CodeItem();
            assertNull(item.getGroup());
            assertNull(item.getCode());
            assertNull(item.getName());
            assertFalse(item.isEnabled());
        }

        @Test
        @DisplayName("3개 인자 생성자")
        void threeArgConstructor() {
            CodeItem item = new CodeItem("STATUS", "ACTIVE", "활성");

            assertEquals("STATUS", item.getGroup());
            assertEquals("ACTIVE", item.getCode());
            assertEquals("활성", item.getName());
            assertTrue(item.isEnabled());
        }

        @Test
        @DisplayName("빌더 패턴")
        void builder() {
            CodeItem item = CodeItem.builder()
                    .group("ROLE")
                    .code("ADMIN")
                    .name("관리자")
                    .description("시스템 관리자")
                    .sortOrder(1)
                    .enabled(true)
                    .build();

            assertEquals("ROLE", item.getGroup());
            assertEquals("ADMIN", item.getCode());
            assertEquals("관리자", item.getName());
            assertEquals("시스템 관리자", item.getDescription());
            assertEquals(1, item.getSortOrder());
            assertTrue(item.isEnabled());
        }

        @Test
        @DisplayName("setter 동작")
        void setters() {
            CodeItem item = new CodeItem();
            item.setGroup("G");
            item.setCode("C");
            item.setName("N");
            item.setDescription("D");
            item.setSortOrder(5);
            item.setEnabled(true);

            assertEquals("G", item.getGroup());
            assertEquals("C", item.getCode());
            assertEquals("N", item.getName());
            assertEquals("D", item.getDescription());
            assertEquals(5, item.getSortOrder());
            assertTrue(item.isEnabled());
        }
    }

    // ========== InMemoryCodeRepository ==========
    @Nested
    @DisplayName("InMemoryCodeRepository")
    class InMemoryCodeRepositoryTest {

        private InMemoryCodeRepository repository;

        @BeforeEach
        void setUp() {
            repository = new InMemoryCodeRepository();
        }

        @Test
        @DisplayName("코드 추가 및 조회")
        void addAndFind() {
            CodeItem item = new CodeItem("STATUS", "ACTIVE", "활성");
            repository.addCode(item);

            Optional<CodeItem> found = repository.findByGroupAndCode("STATUS", "ACTIVE");
            assertTrue(found.isPresent());
            assertEquals("활성", found.get().getName());
        }

        @Test
        @DisplayName("존재하지 않는 코드 조회")
        void findNonExistent() {
            Optional<CodeItem> found = repository.findByGroupAndCode("NONE", "NONE");
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("그룹 전체 조회")
        void findByGroup() {
            repository.addCode(CodeItem.builder().group("STATUS").code("A").name("A").sortOrder(2).build());
            repository.addCode(CodeItem.builder().group("STATUS").code("B").name("B").sortOrder(1).build());
            repository.addCode(CodeItem.builder().group("ROLE").code("C").name("C").sortOrder(1).build());

            List<CodeItem> statusCodes = repository.findByGroup("STATUS");
            assertEquals(2, statusCodes.size());
            // sortOrder 기준 정렬 확인
            assertEquals("B", statusCodes.get(0).getCode());
            assertEquals("A", statusCodes.get(1).getCode());
        }

        @Test
        @DisplayName("존재하지 않는 그룹 조회 시 빈 리스트")
        void findByGroupEmpty() {
            List<CodeItem> result = repository.findByGroup("UNKNOWN");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("활성 코드만 조회")
        void findEnabledByGroup() {
            repository.addCode(CodeItem.builder().group("STATUS").code("A").name("A").enabled(true).build());
            repository.addCode(CodeItem.builder().group("STATUS").code("B").name("B").enabled(false).build());

            List<CodeItem> enabled = repository.findEnabledByGroup("STATUS");
            assertEquals(1, enabled.size());
            assertEquals("A", enabled.get(0).getCode());
        }

        @Test
        @DisplayName("existsByGroupAndCode - checkEnabled=false")
        void existsWithoutCheckEnabled() {
            CodeItem item = CodeItem.builder().group("G").code("C").name("N").enabled(false).build();
            repository.addCode(item);

            assertTrue(repository.existsByGroupAndCode("G", "C", false));
        }

        @Test
        @DisplayName("existsByGroupAndCode - checkEnabled=true, 비활성")
        void existsCheckEnabledDisabled() {
            CodeItem item = CodeItem.builder().group("G").code("C").name("N").enabled(false).build();
            repository.addCode(item);

            assertFalse(repository.existsByGroupAndCode("G", "C", true));
        }

        @Test
        @DisplayName("existsByGroupAndCode - checkEnabled=true, 활성")
        void existsCheckEnabledActive() {
            CodeItem item = CodeItem.builder().group("G").code("C").name("N").enabled(true).build();
            repository.addCode(item);

            assertTrue(repository.existsByGroupAndCode("G", "C", true));
        }

        @Test
        @DisplayName("존재하지 않는 그룹/코드")
        void existsNotFound() {
            assertFalse(repository.existsByGroupAndCode("NONE", "NONE", false));
        }

        @Test
        @DisplayName("코드 목록 벌크 추가")
        void addCodes() {
            List<CodeItem> items = List.of(
                    new CodeItem("G", "A", "A"),
                    new CodeItem("G", "B", "B")
            );
            repository.addCodes(items);

            assertEquals(2, repository.findByGroup("G").size());
        }

        @Test
        @DisplayName("코드 제거")
        void removeCode() {
            repository.addCode(new CodeItem("G", "A", "A"));
            repository.removeCode("G", "A");

            assertFalse(repository.findByGroupAndCode("G", "A").isPresent());
        }

        @Test
        @DisplayName("존재하지 않는 그룹에서 코드 제거 (에러 없음)")
        void removeCodeFromNonExistentGroup() {
            assertDoesNotThrow(() -> repository.removeCode("NONE", "NONE"));
        }

        @Test
        @DisplayName("그룹 제거")
        void removeGroup() {
            repository.addCode(new CodeItem("G", "A", "A"));
            repository.addCode(new CodeItem("G", "B", "B"));
            repository.removeGroup("G");

            assertTrue(repository.findByGroup("G").isEmpty());
        }

        @Test
        @DisplayName("전체 초기화")
        void clear() {
            repository.addCode(new CodeItem("G1", "A", "A"));
            repository.addCode(new CodeItem("G2", "B", "B"));
            repository.clear();

            assertEquals(0, repository.getGroupCount());
            assertEquals(0, repository.getTotalCodeCount());
        }

        @Test
        @DisplayName("카운트 조회")
        void counts() {
            repository.addCode(new CodeItem("G1", "A", "A"));
            repository.addCode(new CodeItem("G1", "B", "B"));
            repository.addCode(new CodeItem("G2", "C", "C"));

            assertEquals(2, repository.getGroupCount());
            assertEquals(3, repository.getTotalCodeCount());
        }
    }

    // ========== CodeService ==========
    @Nested
    @DisplayName("CodeService")
    class CodeServiceTest {

        private InMemoryCodeRepository repository;
        private CodeService service;

        @BeforeEach
        void setUp() {
            repository = new InMemoryCodeRepository();
            service = new CodeService(repository);

            repository.addCode(CodeItem.builder().group("STATUS").code("ACTIVE").name("활성").enabled(true).build());
            repository.addCode(CodeItem.builder().group("STATUS").code("INACTIVE").name("비활성").enabled(false).build());
        }

        @Test
        @DisplayName("그룹별 조회")
        void getByGroup() {
            List<CodeItem> items = service.getByGroup("STATUS");
            assertEquals(2, items.size());
        }

        @Test
        @DisplayName("활성 코드만 조회")
        void getEnabledByGroup() {
            List<CodeItem> items = service.getEnabledByGroup("STATUS");
            assertEquals(1, items.size());
            assertEquals("ACTIVE", items.get(0).getCode());
        }

        @Test
        @DisplayName("단건 조회")
        void get() {
            Optional<CodeItem> item = service.get("STATUS", "ACTIVE");
            assertTrue(item.isPresent());
            assertEquals("활성", item.get().getName());
        }

        @Test
        @DisplayName("코드명 조회")
        void getName() {
            assertEquals("활성", service.getName("STATUS", "ACTIVE"));
            assertNull(service.getName("STATUS", "UNKNOWN"));
        }

        @Test
        @DisplayName("존재 여부 확인")
        void exists() {
            assertTrue(service.exists("STATUS", "ACTIVE"));
            assertTrue(service.exists("STATUS", "INACTIVE"));
            assertFalse(service.exists("STATUS", "NONE"));
        }

        @Test
        @DisplayName("활성 여부 확인")
        void isEnabled() {
            assertTrue(service.isEnabled("STATUS", "ACTIVE"));
            assertFalse(service.isEnabled("STATUS", "INACTIVE"));
        }
    }

    // ========== CodeValidator ==========
    @Nested
    @DisplayName("CodeValidator")
    class CodeValidatorTest {

        @Test
        @DisplayName("null, 빈값은 유효")
        void nullAndBlankAreValid() {
            CodeValidator validator = new CodeValidator();
            assertTrue(validator.isValid(null, null));
            assertTrue(validator.isValid("", null));
            assertTrue(validator.isValid("   ", null));
        }

        @Test
        @DisplayName("CodeRepository가 없으면 항상 유효")
        void noRepositoryAlwaysValid() {
            CodeValidator validator = new CodeValidator();
            // codeRepository is null (no @Autowired injection in test)
            assertTrue(validator.isValid("ANY_VALUE", null));
        }
    }
}
