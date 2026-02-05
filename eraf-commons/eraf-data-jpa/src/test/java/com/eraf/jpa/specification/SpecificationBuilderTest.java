package com.eraf.jpa.specification;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpecificationBuilder 테스트
 */
@ExtendWith(MockitoExtension.class)
class SpecificationBuilderTest {

    @Mock
    private Root<TestEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate combinedPredicate;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        lenient().when(criteriaBuilder.conjunction()).thenReturn(predicate);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
    }

    @Test
    @DisplayName("빈 Specification 생성")
    void shouldCreateEmptySpecification() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder().build();

        assertNotNull(spec);
    }

    @Test
    @DisplayName("equals 조건 추가")
    void shouldAddEqualsCondition() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .equals("name", "John")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(root).get("name");
        verify(criteriaBuilder).equal(any(), eq("John"));
    }

    @Test
    @DisplayName("null 값은 조건에서 제외")
    void shouldSkipNullValues() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .equals("name", null)
                .build();

        assertNotNull(spec);
    }

    @Test
    @DisplayName("like 조건 추가")
    void shouldAddLikeCondition() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .like("name", "John")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).like(any(), eq("%John%"));
    }

    @Test
    @DisplayName("startsWith 조건")
    void shouldAddStartsWithCondition() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .startsWith("name", "John")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).like(any(), eq("John%"));
    }

    @Test
    @DisplayName("endsWith 조건")
    void shouldAddEndsWithCondition() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .endsWith("name", "son")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).like(any(), eq("%son"));
    }

    @Test
    @DisplayName("in 조건 추가")
    void shouldAddInCondition() {
        when(path.in(anyCollection())).thenReturn(predicate);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .in("status", Arrays.asList("ACTIVE", "PENDING"))
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(path).in(anyCollection());
    }

    @Test
    @DisplayName("빈 컬렉션은 in 조건에서 제외")
    void shouldSkipEmptyCollectionInCondition() {
        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .in("status", Arrays.asList())
                .build();

        assertNotNull(spec);
    }

    @Test
    @DisplayName("greaterThan 조건")
    void shouldAddGreaterThanCondition() {
        when(criteriaBuilder.greaterThan(any(), any(Comparable.class))).thenReturn(predicate);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .greaterThan("age", 18)
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).greaterThan(any(), eq(18));
    }

    @Test
    @DisplayName("복합 조건 빌드")
    void shouldBuildCompositeConditions() {
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .equals("status", "ACTIVE")
                .like("name", "John")
                .greaterThanOrEqualTo("age", 18)
                .lessThanOrEqualTo("age", 65)
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(any(), eq("ACTIVE"));
        verify(criteriaBuilder).like(any(), contains("John"));
    }

    @Test
    @DisplayName("between 조건")
    void shouldAddBetweenCondition() {
        when(criteriaBuilder.between(any(), any(Comparable.class), any(Comparable.class))).thenReturn(predicate);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .between("createdDate", start, end)
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).between(any(), eq(start), eq(end));
    }

    @Test
    @DisplayName("isNull 조건")
    void shouldAddIsNullCondition() {
        when(criteriaBuilder.isNull(any())).thenReturn(predicate);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .isNull("deletedAt")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).isNull(any());
    }

    @Test
    @DisplayName("isNotNull 조건")
    void shouldAddIsNotNullCondition() {
        when(criteriaBuilder.isNotNull(any())).thenReturn(predicate);

        Specification<TestEntity> spec = SpecificationBuilder.<TestEntity>builder()
                .isNotNull("email")
                .build();

        assertNotNull(spec);
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).isNotNull(any());
    }

    // 테스트용 더미 엔티티
    static class TestEntity {
        private Long id;
        private String name;
        private String status;
        private int age;
        private LocalDate createdDate;
        private LocalDate deletedAt;
        private String email;
    }
}
