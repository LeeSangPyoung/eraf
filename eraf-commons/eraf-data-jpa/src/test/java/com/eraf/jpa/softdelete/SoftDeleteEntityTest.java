package com.eraf.jpa.softdelete;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SoftDeleteEntity 테스트
 */
class SoftDeleteEntityTest {

    @Test
    @DisplayName("기본값은 삭제되지 않은 상태")
    void shouldNotBeDeletedByDefault() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();

        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());
        assertNull(entity.getDeletedBy());
    }

    @Test
    @DisplayName("소프트 삭제 수행")
    void shouldPerformSoftDelete() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();
        Instant beforeDelete = Instant.now();

        entity.softDelete("admin-user");

        assertTrue(entity.isDeleted());
        assertNotNull(entity.getDeletedAt());
        assertEquals("admin-user", entity.getDeletedBy());
        assertTrue(entity.getDeletedAt().isAfter(beforeDelete) || entity.getDeletedAt().equals(beforeDelete));
    }

    @Test
    @DisplayName("삭제자 없이 소프트 삭제")
    void shouldPerformSoftDeleteWithoutDeletedBy() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();

        entity.softDelete(null);

        assertTrue(entity.isDeleted());
        assertNotNull(entity.getDeletedAt());
        assertNull(entity.getDeletedBy());
    }

    @Test
    @DisplayName("소프트 삭제 복원")
    void shouldRestoreSoftDeletedEntity() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();
        entity.softDelete("admin-user");

        entity.restore();

        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());
        assertNull(entity.getDeletedBy());
    }

    @Test
    @DisplayName("이미 삭제된 엔티티 다시 삭제해도 시간 업데이트")
    void shouldUpdateTimeOnReDelete() throws InterruptedException {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();
        entity.softDelete("user1");
        Instant firstDeleteTime = entity.getDeletedAt();

        Thread.sleep(10); // 시간 차이를 두기 위해

        entity.softDelete("user2");

        assertTrue(entity.isDeleted());
        assertEquals("user2", entity.getDeletedBy());
        assertTrue(entity.getDeletedAt().isAfter(firstDeleteTime));
    }

    @Test
    @DisplayName("복원 후 다시 삭제")
    void shouldAllowReDeleteAfterRestore() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();

        entity.softDelete("user1");
        entity.restore();
        entity.softDelete("user2");

        assertTrue(entity.isDeleted());
        assertEquals("user2", entity.getDeletedBy());
    }

    @Test
    @DisplayName("Getter/Setter 동작 확인")
    void shouldWorkWithGettersAndSetters() {
        TestSoftDeleteEntity entity = new TestSoftDeleteEntity();
        Instant now = Instant.now();

        entity.setDeleted(true);
        entity.setDeletedAt(now);
        entity.setDeletedBy("system");

        assertTrue(entity.isDeleted());
        assertEquals(now, entity.getDeletedAt());
        assertEquals("system", entity.getDeletedBy());
    }

    // 테스트용 구현체
    static class TestSoftDeleteEntity extends SoftDeleteEntity {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
