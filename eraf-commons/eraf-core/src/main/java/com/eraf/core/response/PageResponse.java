package com.eraf.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * 페이징 응답
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long total;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;
    private final boolean hasPrevious;

    private PageResponse(List<T> content, int page, int size, long total) {
        this.content = content != null ? content : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        return new PageResponse<>(content, page, size, total);
    }

    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(Collections.emptyList(), 0, 0, 0);
    }

    public static <T> PageResponse<T> empty(int size) {
        return new PageResponse<>(Collections.emptyList(), 0, size, 0);
    }

    // Getters
    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public int getNumberOfElements() {
        return content.size();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
