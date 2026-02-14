package com.eraf.jpa.cursor;

/**
 * 커서 기반 페이징 인터페이스
 * <p>
 * Repository에서 이 인터페이스를 구현하여 커서 기반 페이징을 제공합니다.
 *
 * @param <T> 엔티티 타입
 * @param <C> 커서 타입
 */
public interface CursorPageable<T, C> {

    /**
     * 커서 기반 페이징 조회
     *
     * @param request 페이지 요청
     * @return 커서 페이지
     */
    CursorPage<T, C> findAll(CursorPageRequest<C> request);
}
