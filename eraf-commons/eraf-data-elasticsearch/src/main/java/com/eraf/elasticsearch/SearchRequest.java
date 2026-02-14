package com.eraf.elasticsearch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch 검색 요청 빌더
 *
 * 쿼리, 필드, 페이징, 정렬, 필터 조건을 캡슐화합니다.
 */
public class SearchRequest {

    private String query;
    private String[] fields;
    private int page = 0;
    private int size = 20;
    private String sortField;
    private String sortDirection = "DESC";
    private Map<String, Object> filters = new HashMap<>();

    private SearchRequest() {
    }

    /**
     * Builder 생성
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getQuery() {
        return query;
    }

    public String[] getFields() {
        return fields;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSortField() {
        return sortField;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public Map<String, Object> getFilters() {
        return Collections.unmodifiableMap(filters);
    }

    /**
     * 오프셋 계산
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * 정렬이 오름차순인지 여부
     */
    public boolean isAscending() {
        return "ASC".equalsIgnoreCase(sortDirection);
    }

    // Setters
    public void setQuery(String query) {
        this.query = query;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters != null ? filters : new HashMap<>();
    }

    /**
     * SearchRequest Builder
     */
    public static class Builder {

        private final SearchRequest request = new SearchRequest();

        public Builder query(String query) {
            request.query = query;
            return this;
        }

        public Builder fields(String... fields) {
            request.fields = fields;
            return this;
        }

        public Builder page(int page) {
            request.page = page;
            return this;
        }

        public Builder size(int size) {
            request.size = size;
            return this;
        }

        public Builder sortField(String sortField) {
            request.sortField = sortField;
            return this;
        }

        public Builder sortDirection(String sortDirection) {
            request.sortDirection = sortDirection;
            return this;
        }

        public Builder filter(String key, Object value) {
            request.filters.put(key, value);
            return this;
        }

        public Builder filters(Map<String, Object> filters) {
            if (filters != null) {
                request.filters.putAll(filters);
            }
            return this;
        }

        public SearchRequest build() {
            return request;
        }
    }
}
