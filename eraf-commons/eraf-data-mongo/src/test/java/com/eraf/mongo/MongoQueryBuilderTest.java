package com.eraf.mongo;

import com.eraf.mongo.query.MongoQueryBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MongoDB Query Builder 테스트")
class MongoQueryBuilderTest {

    static class TestDocument {
        private String id;
        private String status;
        private String name;
        private int price;
        private LocalDate createdAt;
    }

    @Test
    @DisplayName("기본 equals 쿼리 생성")
    void shouldBuildEqualsQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .eq("status", "ACTIVE")
            .build();

        // Then
        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("status");
        assertThat(query.toString()).contains("ACTIVE");
    }

    @Test
    @DisplayName("다중 조건 AND 쿼리 생성")
    void shouldBuildMultipleConditionsWithAnd() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .eq("status", "ACTIVE")
            .eq("type", "PREMIUM")
            .build();

        // Then
        assertThat(query.getQueryObject()).isNotNull();
    }

    @Test
    @DisplayName("IN 조건 쿼리 생성")
    void shouldBuildInQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .in("status", List.of("ACTIVE", "PENDING", "APPROVED"))
            .build();

        // Then
        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("status");
    }

    @Test
    @DisplayName("범위 조건 쿼리 생성 (gt, lt)")
    void shouldBuildRangeQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .gt("price", 1000)
            .lt("price", 5000)
            .build();

        // Then
        assertThat(query).isNotNull();
    }

    @Test
    @DisplayName("날짜 범위 쿼리 생성")
    void shouldBuildDateRangeQuery() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .gte("createdAt", startDate)
            .lte("createdAt", endDate)
            .build();

        // Then
        assertThat(query).isNotNull();
    }

    @Test
    @DisplayName("정규식 패턴 매칭 쿼리")
    void shouldBuildRegexQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .like("name", "John")
            .build();

        // Then
        assertThat(query).isNotNull();
    }

    @Test
    @DisplayName("페이지네이션 쿼리 생성")
    void shouldBuildPaginationQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .eq("status", "ACTIVE")
            .page(2, 10)
            .sort("createdAt", Sort.Direction.DESC)
            .build();

        // Then
        assertThat(query).isNotNull();
        assertThat(query.toString()).contains("status");
    }

    @Test
    @DisplayName("복합 조건 쿼리 생성")
    void shouldBuildComplexQuery() {
        // When
        Query query = MongoQueryBuilder.forClass(TestDocument.class)
            .eq("status", "ACTIVE")
            .in("type", List.of("A", "B", "C"))
            .gt("price", 100)
            .lt("price", 1000)
            .like("name", "test")
            .page(0, 20)
            .sort("createdAt", Sort.Direction.DESC)
            .build();

        // Then
        assertThat(query).isNotNull();
    }
}
