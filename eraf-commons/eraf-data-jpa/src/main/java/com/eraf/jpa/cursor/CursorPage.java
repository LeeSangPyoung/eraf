package com.eraf.jpa.cursor;

import java.util.List;

/**
 * 커서 기반 페이지 응답
 *
 * @param <T> 엔티티 타입
 * @param <C> 커서 타입
 */
public class CursorPage<T, C> {

    private final List<T> content;
    private final C nextCursor;
    private final boolean hasMore;
    private final int size;

    public CursorPage(List<T> content, C nextCursor, boolean hasMore, int size) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.size = size;
    }

    /**
     * 현재 페이지의 데이터 목록
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * 다음 페이지 조회에 사용할 커서
     * (hasMore가 false면 null)
     */
    public C getNextCursor() {
        return nextCursor;
    }

    /**
     * 다음 페이지가 존재하는지 여부
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * 요청한 페이지 크기
     */
    public int getSize() {
        return size;
    }

    /**
     * 현재 페이지의 실제 데이터 개수
     */
    public int getContentSize() {
        return content.size();
    }

    /**
     * 빈 페이지 생성
     */
    public static <T, C> CursorPage<T, C> empty(int size) {
        return new CursorPage<>(List.of(), null, false, size);
    }

    @Override
    public String toString() {
        return "CursorPage{" +
                "contentSize=" + content.size() +
                ", nextCursor=" + nextCursor +
                ", hasMore=" + hasMore +
                ", size=" + size +
                '}';
    }
}
