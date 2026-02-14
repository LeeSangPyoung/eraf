package com.eraf.jpa.cursor;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 페이지 빌더
 * <p>
 * 조회된 결과를 커서 페이지로 변환합니다.
 *
 * @param <T> 엔티티 타입
 * @param <C> 커서 타입
 */
public class CursorPageBuilder<T, C> {

    /**
     * 조회된 결과를 커서 페이지로 변환
     *
     * @param results         조회된 결과 (size+1 개수로 조회)
     * @param request         페이지 요청
     * @param cursorExtractor 커서 추출 함수 (예: User::getId)
     * @return 커서 페이지
     */
    public static <T, C> CursorPage<T, C> build(
            List<T> results,
            CursorPageRequest<C> request,
            Function<T, C> cursorExtractor) {

        if (results.isEmpty()) {
            return CursorPage.empty(request.getSize());
        }

        // 다음 페이지 존재 여부 확인
        boolean hasMore = results.size() > request.getSize();

        // 실제 반환할 데이터 (size만큼만)
        List<T> content = hasMore
                ? results.subList(0, request.getSize())
                : results;

        // 다음 커서 추출
        C nextCursor = hasMore
                ? cursorExtractor.apply(results.get(request.getSize() - 1))
                : null;

        return new CursorPage<>(content, nextCursor, hasMore, request.getSize());
    }

    /**
     * 빈 페이지 생성
     */
    public static <T, C> CursorPage<T, C> empty(int size) {
        return CursorPage.empty(size);
    }
}
