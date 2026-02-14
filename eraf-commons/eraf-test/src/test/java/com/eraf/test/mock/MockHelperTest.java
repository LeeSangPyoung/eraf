package com.eraf.test.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MockHelper 테스트
 */
@ExtendWith(MockitoExtension.class)
class MockHelperTest {

    @Mock
    private TestUserRepository repository;

    @Test
    void mockFindById_ShouldReturnEntity() {
        // given
        TestUser user = new TestUser(1L, "John");
        MockHelper.mockFindById(repository, 1L, user);

        // when
        Optional<TestUser> result = repository.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void mockFindByIdEmpty_ShouldReturnEmpty() {
        // given
        MockHelper.mockFindByIdEmpty(repository, 1L);

        // when
        Optional<TestUser> result = repository.findById(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void mockFindAll_ShouldReturnList() {
        // given
        List<TestUser> users = List.of(
                new TestUser(1L, "John"),
                new TestUser(2L, "Jane")
        );
        MockHelper.mockFindAll(repository, users);

        // when
        List<TestUser> result = repository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(users);
    }

    @Test
    void mockSave_ShouldReturnEntity() {
        // given
        TestUser user = new TestUser(1L, "John");
        MockHelper.mockSave(repository, user);

        // when
        TestUser result = repository.save(user);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void mockSave_WithAny_ShouldReturnForAnyArgument() {
        // given
        TestUser savedUser = new TestUser(1L, "Saved");
        MockHelper.mockSave(repository, savedUser);

        // when
        TestUser result1 = repository.save(new TestUser(null, "New"));
        TestUser result2 = repository.save(new TestUser(2L, "Another"));

        // then - 모든 save 호출이 같은 객체를 반환
        assertThat(result1).isEqualTo(savedUser);
        assertThat(result2).isEqualTo(savedUser);
    }

    @Test
    void mockPage_ShouldCreatePageWithContent() {
        // given
        List<TestUser> content = List.of(
                new TestUser(1L, "John"),
                new TestUser(2L, "Jane"),
                new TestUser(3L, "Bob")
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<TestUser> page = MockHelper.mockPage(content, pageable);

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
    }

    @Test
    void mockPage_WithEmptyContent_ShouldCreateEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<TestUser> page = MockHelper.mockPage(List.of(), pageable);

        // then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    void mockPage_WithMultiplePages_ShouldCalculateCorrectly() {
        // given - 25개 컨텐츠, 페이지 크기 10
        List<TestUser> allContent = List.of(
                new TestUser(1L, "User1"),
                new TestUser(2L, "User2"),
                new TestUser(3L, "User3"),
                new TestUser(4L, "User4"),
                new TestUser(5L, "User5")
        );
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<TestUser> page = MockHelper.mockPage(allContent, pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3); // 5 / 2 = 2.5 → 3 pages
    }

    @Test
    void verifyOnce_ShouldVerifyExactlyOnce() {
        // given
        TestUser user = new TestUser(1L, "John");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        // when
        repository.findById(1L);

        // then - verifyOnce는 verify(mock, times(1))과 동일
        MockHelper.verifyOnce(repository).findById(1L);
    }

    @Test
    void verifyNever_ShouldVerifyNeverCalled() {
        // when - repository 메서드 하나 호출
        repository.findAll();

        // then - findById는 호출되지 않음
        MockHelper.verifyNever(repository).findById(any());
    }

    @Test
    void verifyTimes_ShouldVerifyExactCount() {
        // when
        repository.findById(1L);
        repository.findById(1L);
        repository.findById(1L);

        // then
        MockHelper.verifyTimes(repository, 3).findById(1L);
    }

    @Test
    void reset_ShouldClearAllInteractions() {
        // given
        TestUser user = new TestUser(1L, "John");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        repository.findById(1L);

        // when
        MockHelper.reset(repository);

        // then - 이전 stub과 호출 기록이 모두 삭제됨
        Optional<TestUser> result = repository.findById(1L);
        assertThat(result).isEmpty(); // stub이 초기화되어 기본값 반환
    }

    @Test
    void mockFindById_MultipleEntities_ShouldReturnCorrectOnes() {
        // given
        TestUser user1 = new TestUser(1L, "John");
        TestUser user2 = new TestUser(2L, "Jane");
        MockHelper.mockFindById(repository, 1L, user1);
        MockHelper.mockFindById(repository, 2L, user2);

        // when
        Optional<TestUser> result1 = repository.findById(1L);
        Optional<TestUser> result2 = repository.findById(2L);

        // then
        assertThat(result1).isPresent();
        assertThat(result1.get().getName()).isEqualTo("John");
        assertThat(result2).isPresent();
        assertThat(result2.get().getName()).isEqualTo("Jane");
    }

    // Test helper classes
    static class TestUser {
        private Long id;
        private String name;

        public TestUser(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    interface TestUserRepository extends JpaRepository<TestUser, Long> {
    }
}
