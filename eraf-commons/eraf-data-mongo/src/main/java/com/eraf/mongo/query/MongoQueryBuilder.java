package com.eraf.mongo.query;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * MongoDB 플루언트 쿼리 빌더
 * Spring Data MongoDB의 Query/Criteria를 감싸서 간결한 쿼리 작성을 지원합니다.
 *
 * <pre>{@code
 * Query query = MongoQueryBuilder.forClass(UserDocument.class)
 *     .eq("name", "홍길동")
 *     .like("email", "example.com")
 *     .gt("age", 20)
 *     .sort("createdAt", Sort.Direction.DESC)
 *     .page(0, 10)
 *     .build();
 *
 * List<UserDocument> users = mongoTemplate.find(query, UserDocument.class);
 * }</pre>
 */
public class MongoQueryBuilder<T> {

    private final Class<T> entityClass;
    private final List<Criteria> criteriaList = new ArrayList<>();
    private Pageable pageable;
    private Sort sort;

    private MongoQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 쿼리 빌더 생성
     */
    public static <T> MongoQueryBuilder<T> forClass(Class<T> clazz) {
        return new MongoQueryBuilder<>(clazz);
    }

    /**
     * 동등 조건 (field == value)
     */
    public MongoQueryBuilder<T> eq(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).is(value));
        }
        return this;
    }

    /**
     * 부등 조건 (field != value)
     */
    public MongoQueryBuilder<T> ne(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).ne(value));
        }
        return this;
    }

    /**
     * LIKE 조건 (정규표현식 기반, 대소문자 무시)
     */
    public MongoQueryBuilder<T> like(String field, String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            criteriaList.add(Criteria.where(field).regex(pattern, "i"));
        }
        return this;
    }

    /**
     * 초과 조건 (field > value)
     */
    public MongoQueryBuilder<T> gt(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).gt(value));
        }
        return this;
    }

    /**
     * 이상 조건 (field >= value)
     */
    public MongoQueryBuilder<T> gte(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).gte(value));
        }
        return this;
    }

    /**
     * 미만 조건 (field < value)
     */
    public MongoQueryBuilder<T> lt(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).lt(value));
        }
        return this;
    }

    /**
     * 이하 조건 (field <= value)
     */
    public MongoQueryBuilder<T> lte(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).lte(value));
        }
        return this;
    }

    /**
     * IN 조건 (field IN values)
     */
    public MongoQueryBuilder<T> in(String field, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            criteriaList.add(Criteria.where(field).in(values));
        }
        return this;
    }

    /**
     * BETWEEN 조건 (from <= field <= to)
     */
    public MongoQueryBuilder<T> between(String field, Object from, Object to) {
        if (from != null && to != null) {
            criteriaList.add(Criteria.where(field).gte(from).lte(to));
        }
        return this;
    }

    /**
     * EXISTS 조건 (필드 존재 여부)
     */
    public MongoQueryBuilder<T> exists(String field, boolean exists) {
        criteriaList.add(Criteria.where(field).exists(exists));
        return this;
    }

    /**
     * NULL 조건 (field IS NULL)
     */
    public MongoQueryBuilder<T> isNull(String field) {
        criteriaList.add(Criteria.where(field).is(null));
        return this;
    }

    /**
     * NOT NULL 조건 (field IS NOT NULL)
     */
    public MongoQueryBuilder<T> isNotNull(String field) {
        criteriaList.add(Criteria.where(field).ne(null));
        return this;
    }

    /**
     * 페이지네이션 설정
     */
    public MongoQueryBuilder<T> page(int page, int size) {
        this.pageable = PageRequest.of(page, size);
        return this;
    }

    /**
     * 정렬 설정
     */
    public MongoQueryBuilder<T> sort(String field, Sort.Direction direction) {
        Sort newSort = Sort.by(direction, field);
        this.sort = (this.sort != null) ? this.sort.and(newSort) : newSort;
        return this;
    }

    /**
     * 쿼리 빌드
     */
    public Query build() {
        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            Criteria combined = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(combined);
        }

        if (sort != null) {
            query.with(sort);
        }

        if (pageable != null) {
            query.with(pageable);
        }

        return query;
    }

    /**
     * 엔티티 클래스 반환
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }
}
