package com.eraf.jpa.cursor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CursorPageBuilder 테스트
 */
class CursorPageBuilderTest {

    @Test
    void build_WithFullPage_ShouldHaveMore() {
        // given - results has size+1 items to indicate hasMore
        List<String> items = List.of("item1", "item2", "item3", "item4");
        int pageSize = 3;
        CursorPageRequest<String> request = CursorPageRequest.first(pageSize);

        // when
        CursorPage<String, String> page = CursorPageBuilder.build(items, request, item -> item);

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent()).containsExactly("item1", "item2", "item3");
        assertThat(page.hasMore()).isTrue();
        assertThat(page.getNextCursor()).isEqualTo("item3");
    }

    @Test
    void build_WithPartialPage_ShouldNotHaveMore() {
        // given - results fewer than pageSize, no next page
        List<String> items = List.of("item1", "item2");
        int pageSize = 3;
        CursorPageRequest<String> request = CursorPageRequest.first(pageSize);

        // when
        CursorPage<String, String> page = CursorPageBuilder.build(items, request, item -> item);

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.hasMore()).isFalse();
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    void build_WithExactPageSize_ShouldNotHaveMore() {
        // given - results exactly pageSize, no overflow means no next page
        List<String> items = List.of("item1", "item2", "item3");
        int pageSize = 3;
        CursorPageRequest<String> request = CursorPageRequest.first(pageSize);

        // when
        CursorPage<String, String> page = CursorPageBuilder.build(items, request, item -> item);

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.hasMore()).isFalse();
    }

    @Test
    void build_WithEmptyList_ShouldReturnEmptyPage() {
        // given
        List<String> items = List.of();
        int pageSize = 3;
        CursorPageRequest<String> request = CursorPageRequest.first(pageSize);

        // when
        CursorPage<String, String> page = CursorPageBuilder.build(items, request, item -> item);

        // then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.hasMore()).isFalse();
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    void build_WithLongCursor_ShouldExtractCursor() {
        // given
        record Item(Long id, String name) {}
        List<Item> items = List.of(
            new Item(1L, "A"),
            new Item(2L, "B"),
            new Item(3L, "C")  // size+1 to indicate hasMore
        );
        int pageSize = 2;
        CursorPageRequest<Long> request = CursorPageRequest.first(pageSize);

        // when
        CursorPage<Item, Long> page = CursorPageBuilder.build(items, request, Item::id);

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.hasMore()).isTrue();
        assertThat(page.getNextCursor()).isEqualTo(2L);
    }

    @Test
    void cursorPage_GetSize_ShouldReturnRequestedPageSize() {
        // given
        CursorPage<String, String> page = new CursorPage<>(
            List.of("a", "b", "c"),
            "c",
            true,
            10
        );

        // when/then
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getContentSize()).isEqualTo(3);
    }

    @Test
    void cursorPage_Empty_ShouldReturnEmptyPage() {
        // given
        CursorPage<String, String> emptyPage = CursorPage.empty(20);

        // when/then
        assertThat(emptyPage.getContent()).isEmpty();
        assertThat(emptyPage.getContentSize()).isEqualTo(0);
        assertThat(emptyPage.hasMore()).isFalse();
        assertThat(emptyPage.getNextCursor()).isNull();
        assertThat(emptyPage.getSize()).isEqualTo(20);
    }

    @Test
    void cursorPageBuilder_Empty_ShouldReturnEmptyPage() {
        // given
        CursorPage<String, String> emptyPage = CursorPageBuilder.empty(10);

        // when/then
        assertThat(emptyPage.getContent()).isEmpty();
        assertThat(emptyPage.hasMore()).isFalse();
        assertThat(emptyPage.getNextCursor()).isNull();
        assertThat(emptyPage.getSize()).isEqualTo(10);
    }
}
