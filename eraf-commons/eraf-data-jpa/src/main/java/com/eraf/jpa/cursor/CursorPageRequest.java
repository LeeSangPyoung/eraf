package com.eraf.jpa.cursor;

/**
 * 커서 기반 페이지 요청
 *
 * @param <C> 커서 타입 (Long, String, Instant 등)
 */
public class CursorPageRequest<C> {

    private final C cursor;
    private final int size;
    private final SortDirection direction;

    public CursorPageRequest(C cursor, int size, SortDirection direction) {
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Size must be between 1 and 1000");
        }
        this.cursor = cursor;
        this.size = size;
        this.direction = direction != null ? direction : SortDirection.ASC;
    }

    public static <C> CursorPageRequest<C> of(C cursor, int size) {
        return new CursorPageRequest<>(cursor, size, SortDirection.ASC);
    }

    public static <C> CursorPageRequest<C> of(C cursor, int size, SortDirection direction) {
        return new CursorPageRequest<>(cursor, size, direction);
    }

    public static <C> CursorPageRequest<C> first(int size) {
        return new CursorPageRequest<>(null, size, SortDirection.ASC);
    }

    /**
     * 커서 (null이면 첫 페이지)
     */
    public C getCursor() {
        return cursor;
    }

    /**
     * 페이지 크기
     */
    public int getSize() {
        return size;
    }

    /**
     * 정렬 방향
     */
    public SortDirection getDirection() {
        return direction;
    }

    /**
     * 첫 페이지인지 여부
     */
    public boolean isFirst() {
        return cursor == null;
    }

    /**
     * 실제 조회할 크기 (다음 페이지 존재 여부 확인용 +1)
     */
    public int getFetchSize() {
        return size + 1;
    }

    public enum SortDirection {
        ASC,
        DESC
    }
}
