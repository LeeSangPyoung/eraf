package com.eraf.test.mock;

import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Mock Helper Utilities
 *
 * <p>Common mocking patterns for tests</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Repository Mock
 * UserRepository mockRepo = MockHelper.mockRepository(UserRepository.class);
 * MockHelper.mockFindById(mockRepo, 1L, user);
 * MockHelper.mockSave(mockRepo, user);
 *
 * // Page Mock
 * Page&lt;User&gt; page = MockHelper.mockPage(users, pageable);
 * </pre>
 */
public class MockHelper {

    /**
     * Create mock of given class
     */
    public static <T> T mock(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

    /**
     * Create spy of given object
     */
    public static <T> T spy(T object) {
        return Mockito.spy(object);
    }

    // ==================== Repository Mocks ====================

    /**
     * Mock repository with common defaults
     */
    public static <T, ID> org.springframework.data.repository.CrudRepository<T, ID> mockRepository(
            Class<? extends org.springframework.data.repository.CrudRepository<T, ID>> repoClass) {
        return Mockito.mock(repoClass);
    }

    /**
     * Mock findById to return entity
     */
    public static <T, ID> void mockFindById(
            org.springframework.data.repository.CrudRepository<T, ID> repository,
            ID id,
            T entity) {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
    }

    /**
     * Mock findById to return empty
     */
    public static <T, ID> void mockFindByIdEmpty(
            org.springframework.data.repository.CrudRepository<T, ID> repository,
            ID id) {
        when(repository.findById(id)).thenReturn(Optional.empty());
    }

    /**
     * Mock save to return entity
     */
    public static <T, ID> void mockSave(
            org.springframework.data.repository.CrudRepository<T, ID> repository,
            T entity) {
        when(repository.save(any())).thenReturn(entity);
    }

    /**
     * Mock existsById
     */
    public static <T, ID> void mockExistsById(
            org.springframework.data.repository.CrudRepository<T, ID> repository,
            ID id,
            boolean exists) {
        when(repository.existsById(id)).thenReturn(exists);
    }

    /**
     * Mock findAll with list
     */
    public static <T, ID> void mockFindAll(
            org.springframework.data.repository.CrudRepository<T, ID> repository,
            List<T> entities) {
        when(repository.findAll()).thenReturn(entities);
    }

    // ==================== Page Mocks ====================

    /**
     * Create mock Page from list
     */
    public static <T> Page<T> mockPage(List<T> content) {
        return new PageImpl<>(content);
    }

    /**
     * Create mock Page from list with pageable
     */
    public static <T> Page<T> mockPage(List<T> content, Pageable pageable) {
        return new PageImpl<>(content, pageable, content.size());
    }

    /**
     * Create mock Page with total elements
     */
    public static <T> Page<T> mockPage(List<T> content, Pageable pageable, long total) {
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Create empty Page
     */
    public static <T> Page<T> emptyPage() {
        return Page.empty();
    }

    /**
     * Create empty Page with pageable
     */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }

    // ==================== Optional Mocks ====================

    /**
     * Create Optional with value
     */
    public static <T> Optional<T> optionalOf(T value) {
        return Optional.of(value);
    }

    /**
     * Create empty Optional
     */
    public static <T> Optional<T> optionalEmpty() {
        return Optional.empty();
    }

    // ==================== Verification Helpers ====================

    /**
     * Verify method called once
     */
    public static <T> T verifyOnce(T mock) {
        return Mockito.verify(mock, Mockito.times(1));
    }

    /**
     * Verify method called N times
     */
    public static <T> T verifyTimes(T mock, int times) {
        return Mockito.verify(mock, Mockito.times(times));
    }

    /**
     * Verify method never called
     */
    public static <T> T verifyNever(T mock) {
        return Mockito.verify(mock, Mockito.never());
    }

    /**
     * Verify no more interactions
     */
    public static void verifyNoMoreInteractions(Object... mocks) {
        Mockito.verifyNoMoreInteractions(mocks);
    }

    /**
     * Reset mock
     */
    public static void reset(Object... mocks) {
        Mockito.reset(mocks);
    }
}
