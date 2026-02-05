package com.eraf.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafContextHolderTest {

    @AfterEach
    void tearDown() {
        ErafContextHolder.clearContext();
    }

    @Test
    @DisplayName("기본 컨텍스트 자동 생성")
    void testAutoCreateContext() {
        // When
        ErafContext context = ErafContextHolder.getContext();

        // Then
        assertNotNull(context);
    }

    @Test
    @DisplayName("컨텍스트 설정")
    void testSetContext() {
        // Given
        ErafContext context = new ErafContext();
        context.setUserId("user123");

        // When
        ErafContextHolder.setContext(context);

        // Then
        assertEquals(context, ErafContextHolder.getContext());
        assertEquals("user123", ErafContextHolder.getContext().getUserId());
    }

    @Test
    @DisplayName("null 컨텍스트 설정 시 초기화")
    void testSetNullContext() {
        // Given
        ErafContext context = new ErafContext();
        context.setUserId("user123");
        ErafContextHolder.setContext(context);

        // When
        ErafContextHolder.setContext(null);

        // Then - 새로운 빈 컨텍스트가 반환됨
        ErafContext newContext = ErafContextHolder.getContext();
        assertNotNull(newContext);
        assertNull(newContext.getUserId());
    }

    @Test
    @DisplayName("컨텍스트 초기화")
    void testClearContext() {
        // Given
        ErafContext context = new ErafContext();
        context.setUserId("user123");
        ErafContextHolder.setContext(context);

        // When
        ErafContextHolder.clearContext();

        // Then - 새로운 빈 컨텍스트가 반환됨
        ErafContext newContext = ErafContextHolder.getContext();
        assertNotNull(newContext);
        assertNull(newContext.getUserId());
    }

    @Test
    @DisplayName("새 컨텍스트 생성")
    void testCreateContext() {
        // When
        ErafContext context = ErafContextHolder.createContext();

        // Then
        assertNotNull(context);
        assertEquals(context, ErafContextHolder.getContext());
    }

    @Test
    @DisplayName("스레드 독립성")
    void testThreadIsolation() throws InterruptedException {
        // Given
        ErafContext mainContext = ErafContextHolder.createContext();
        mainContext.setUserId("mainUser");

        // When - 다른 스레드에서 컨텍스트 설정
        Thread thread = new Thread(() -> {
            ErafContext threadContext = ErafContextHolder.createContext();
            threadContext.setUserId("threadUser");
            assertEquals("threadUser", ErafContextHolder.getContext().getUserId());
        });
        thread.start();
        thread.join();

        // Then - 메인 스레드의 컨텍스트는 변경되지 않음
        assertEquals("mainUser", ErafContextHolder.getContext().getUserId());
    }
}
