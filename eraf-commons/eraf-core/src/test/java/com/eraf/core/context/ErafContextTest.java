package com.eraf.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ErafContextTest {

    @BeforeEach
    void setUp() {
        ErafContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        ErafContextHolder.clearContext();
    }

    @Test
    @DisplayName("컨텍스트 기본 속성 설정 및 조회")
    void testBasicProperties() {
        // Given
        ErafContext context = new ErafContext();

        // When
        context.setUserId("user123");
        context.setUsername("testuser");
        context.setTraceId("trace-abc");
        context.setRequestId("req-123");
        context.setClientIp("192.168.1.1");
        context.setUserAgent("Mozilla/5.0");

        // Then
        assertEquals("user123", context.getUserId());
        assertEquals("testuser", context.getUsername());
        assertEquals("Mozilla/5.0", context.getUserAgent());
    }

    @Test
    @DisplayName("커스텀 속성 설정 및 조회")
    void testCustomAttributes() {
        // Given
        ErafContext context = new ErafContext();

        // When
        context.setAttribute("customKey", "customValue");
        context.setAttribute("intKey", 123);

        // Then
        Map<String, Object> attributes = context.getAttributes();
        assertEquals("customValue", attributes.get("customKey"));
        assertEquals(123, attributes.get("intKey"));
    }

    @Test
    @DisplayName("속성 제거")
    void testRemoveAttribute() {
        // Given
        ErafContext context = new ErafContext();
        context.setAttribute("key1", "value1");

        // When
        context.removeAttribute("key1");

        // Then
        assertNull(context.getAttributes().get("key1"));
    }

    @Test
    @DisplayName("컨텍스트 초기화")
    void testClear() {
        // Given
        ErafContext context = new ErafContext();
        context.setUserId("user123");
        context.setAttribute("key", "value");

        // When
        context.clear();

        // Then
        assertNull(context.getUserId());
        assertTrue(context.getAttributes().isEmpty());
    }

    @Test
    @DisplayName("ThreadLocal 컨텍스트 - 정적 메서드")
    void testStaticAccessors() {
        // Given
        ErafContext context = ErafContextHolder.createContext();
        context.setUserId("user456");
        context.setUsername("staticUser");
        context.setTraceId("trace-xyz");
        context.setRequestId("req-456");
        context.setClientIp("10.0.0.1");
        context.setAttribute("role", "ADMIN");

        // Then
        assertEquals("user456", ErafContext.getCurrentUserId());
        assertEquals("staticUser", ErafContext.getCurrentUsername());
        assertEquals("trace-xyz", ErafContext.getTraceId());
        assertEquals("req-456", ErafContext.getRequestId());
        assertEquals("10.0.0.1", ErafContext.getClientIp());
    }

    @Test
    @DisplayName("ThreadLocal 컨텍스트 - 타입 안전 속성 조회")
    void testTypeSafeAttributeAccess() {
        // Given
        ErafContext context = ErafContextHolder.createContext();
        context.setAttribute("stringAttr", "hello");
        context.setAttribute("intAttr", 42);

        // When
        Optional<String> stringValue = ErafContext.getAttribute("stringAttr", String.class);
        Optional<Integer> intValue = ErafContext.getAttribute("intAttr", Integer.class);
        Optional<Long> wrongType = ErafContext.getAttribute("intAttr", Long.class);

        // Then
        assertTrue(stringValue.isPresent());
        assertEquals("hello", stringValue.get());
        assertTrue(intValue.isPresent());
        assertEquals(42, intValue.get());
        assertFalse(wrongType.isPresent());
    }

    @Test
    @DisplayName("ThreadLocal 컨텍스트 - 존재하지 않는 속성 조회")
    void testNonExistentAttribute() {
        // Given
        ErafContextHolder.createContext();

        // When
        Optional<Object> result = ErafContext.getAttribute("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getAttributes는 방어적 복사본 반환")
    void testDefensiveCopy() {
        // Given
        ErafContext context = new ErafContext();
        context.setAttribute("key", "value");

        // When
        Map<String, Object> attributes = context.getAttributes();
        attributes.put("newKey", "newValue");

        // Then
        assertNull(context.getAttributes().get("newKey"));
    }
}
