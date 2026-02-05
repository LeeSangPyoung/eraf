package com.eraf.jpa.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 동적 Specification 빌더
 *
 * 사용 예:
 * <pre>
 * Specification<User> spec = SpecificationBuilder.<User>builder()
 *     .equal("status", Status.ACTIVE)
 *     .like("name", searchKeyword)
 *     .greaterThan("age", 18)
 *     .in("role", roles)
 *     .between("createdAt", startDate, endDate)
 *     .build();
 *
 * userRepository.findAll(spec, pageable);
 * </pre>
 */
public class SpecificationBuilder<T> {

    private final List<Specification<T>> specifications = new ArrayList<>();

    public static <T> SpecificationBuilder<T> builder() {
        return new SpecificationBuilder<>();
    }

    /**
     * 조건이 true일 때만 Specification 추가
     */
    public SpecificationBuilder<T> when(boolean condition, Function<SpecificationBuilder<T>, SpecificationBuilder<T>> builder) {
        if (condition) {
            builder.apply(this);
        }
        return this;
    }

    /**
     * 값이 null이 아닐 때만 Specification 추가
     */
    public <V> SpecificationBuilder<T> ifNotNull(V value, Function<V, Specification<T>> specFunction) {
        if (value != null) {
            specifications.add(specFunction.apply(value));
        }
        return this;
    }

    /**
     * 동등 조건
     */
    public SpecificationBuilder<T> equal(String fieldName, Object value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.equal(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * 동등 조건 (not)
     */
    public SpecificationBuilder<T> notEqual(String fieldName, Object value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.notEqual(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * LIKE 검색 (%value%)
     */
    public SpecificationBuilder<T> like(String fieldName, String value) {
        if (value != null && !value.isEmpty()) {
            specifications.add((root, query, cb) ->
                    cb.like(getPath(root, fieldName).as(String.class), "%" + value + "%"));
        }
        return this;
    }

    /**
     * LIKE 검색 (대소문자 무시)
     */
    public SpecificationBuilder<T> likeIgnoreCase(String fieldName, String value) {
        if (value != null && !value.isEmpty()) {
            specifications.add((root, query, cb) ->
                    cb.like(cb.lower(getPath(root, fieldName).as(String.class)), "%" + value.toLowerCase() + "%"));
        }
        return this;
    }

    /**
     * 시작 문자열 검색 (value%)
     */
    public SpecificationBuilder<T> startsWith(String fieldName, String value) {
        if (value != null && !value.isEmpty()) {
            specifications.add((root, query, cb) ->
                    cb.like(getPath(root, fieldName).as(String.class), value + "%"));
        }
        return this;
    }

    /**
     * 끝 문자열 검색 (%value)
     */
    public SpecificationBuilder<T> endsWith(String fieldName, String value) {
        if (value != null && !value.isEmpty()) {
            specifications.add((root, query, cb) ->
                    cb.like(getPath(root, fieldName).as(String.class), "%" + value));
        }
        return this;
    }

    /**
     * 크다 (>)
     */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> greaterThan(String fieldName, Y value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.greaterThan(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * 크거나 같다 (>=)
     */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> greaterThanOrEqual(String fieldName, Y value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.greaterThanOrEqualTo(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * 작다 (<)
     */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> lessThan(String fieldName, Y value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.lessThan(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * 작거나 같다 (<=)
     */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> lessThanOrEqual(String fieldName, Y value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.lessThanOrEqualTo(getPath(root, fieldName), value));
        }
        return this;
    }

    /**
     * 범위 (BETWEEN)
     */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> between(String fieldName, Y start, Y end) {
        if (start != null && end != null) {
            specifications.add((root, query, cb) -> cb.between(getPath(root, fieldName), start, end));
        } else if (start != null) {
            greaterThanOrEqual(fieldName, start);
        } else if (end != null) {
            lessThanOrEqual(fieldName, end);
        }
        return this;
    }

    /**
     * IN 조건
     */
    public SpecificationBuilder<T> in(String fieldName, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            specifications.add((root, query, cb) -> getPath(root, fieldName).in(values));
        }
        return this;
    }

    /**
     * NOT IN 조건
     */
    public SpecificationBuilder<T> notIn(String fieldName, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            specifications.add((root, query, cb) -> cb.not(getPath(root, fieldName).in(values)));
        }
        return this;
    }

    /**
     * NULL 체크
     */
    public SpecificationBuilder<T> isNull(String fieldName) {
        specifications.add((root, query, cb) -> cb.isNull(getPath(root, fieldName)));
        return this;
    }

    /**
     * NOT NULL 체크
     */
    public SpecificationBuilder<T> isNotNull(String fieldName) {
        specifications.add((root, query, cb) -> cb.isNotNull(getPath(root, fieldName)));
        return this;
    }

    /**
     * Boolean true 체크
     */
    public SpecificationBuilder<T> isTrue(String fieldName) {
        specifications.add((root, query, cb) -> cb.isTrue(getPath(root, fieldName)));
        return this;
    }

    /**
     * Boolean false 체크
     */
    public SpecificationBuilder<T> isFalse(String fieldName) {
        specifications.add((root, query, cb) -> cb.isFalse(getPath(root, fieldName)));
        return this;
    }

    /**
     * 날짜 범위 (LocalDate)
     */
    public SpecificationBuilder<T> dateBetween(String fieldName, LocalDate start, LocalDate end) {
        if (start != null) {
            specifications.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(getPath(root, fieldName), start));
        }
        if (end != null) {
            specifications.add((root, query, cb) ->
                    cb.lessThanOrEqualTo(getPath(root, fieldName), end));
        }
        return this;
    }

    /**
     * 날짜/시간 범위 (LocalDateTime)
     */
    public SpecificationBuilder<T> dateTimeBetween(String fieldName, LocalDateTime start, LocalDateTime end) {
        if (start != null) {
            specifications.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(getPath(root, fieldName), start));
        }
        if (end != null) {
            specifications.add((root, query, cb) ->
                    cb.lessThanOrEqualTo(getPath(root, fieldName), end));
        }
        return this;
    }

    /**
     * OR 조건
     */
    @SafeVarargs
    public final SpecificationBuilder<T> or(Specification<T>... specs) {
        specifications.add((root, query, cb) -> {
            Predicate[] predicates = new Predicate[specs.length];
            for (int i = 0; i < specs.length; i++) {
                predicates[i] = specs[i].toPredicate(root, query, cb);
            }
            return cb.or(predicates);
        });
        return this;
    }

    /**
     * AND 조건 (명시적)
     */
    @SafeVarargs
    public final SpecificationBuilder<T> and(Specification<T>... specs) {
        specifications.add((root, query, cb) -> {
            Predicate[] predicates = new Predicate[specs.length];
            for (int i = 0; i < specs.length; i++) {
                predicates[i] = specs[i].toPredicate(root, query, cb);
            }
            return cb.and(predicates);
        });
        return this;
    }

    /**
     * 커스텀 Specification 추가
     */
    public SpecificationBuilder<T> add(Specification<T> spec) {
        if (spec != null) {
            specifications.add(spec);
        }
        return this;
    }

    /**
     * Join 조건
     */
    public SpecificationBuilder<T> join(String fieldName, String joinFieldName, Object value) {
        if (value != null) {
            specifications.add((root, query, cb) -> {
                Join<Object, Object> join = root.join(fieldName);
                return cb.equal(join.get(joinFieldName), value);
            });
        }
        return this;
    }

    /**
     * Fetch Join
     */
    public SpecificationBuilder<T> fetchJoin(String fieldName) {
        specifications.add((root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch(fieldName, JoinType.LEFT);
            }
            return cb.conjunction();
        });
        return this;
    }

    /**
     * Distinct 적용
     */
    public SpecificationBuilder<T> distinct() {
        specifications.add((root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();
        });
        return this;
    }

    /**
     * Specification 빌드 (AND 조합)
     */
    public Specification<T> build() {
        if (specifications.isEmpty()) {
            return Specification.where(null);
        }

        Specification<T> result = specifications.get(0);
        for (int i = 1; i < specifications.size(); i++) {
            result = result.and(specifications.get(i));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <Y> Path<Y> getPath(Root<T> root, String fieldName) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            Path<?> path = root;
            for (String part : parts) {
                path = path.get(part);
            }
            return (Path<Y>) path;
        }
        return root.get(fieldName);
    }
}
