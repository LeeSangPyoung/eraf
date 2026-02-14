package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExceptionUtils - 예외 처리 유틸리티")
class ExceptionUtilsTest {

    @Nested
    @DisplayName("스택 트레이스")
    class StackTrace {

        @Test
        @DisplayName("getStackTrace")
        void getStackTrace() {
            Exception ex = new RuntimeException("test error");
            String trace = ExceptionUtils.getStackTrace(ex);
            assertNotNull(trace);
            assertTrue(trace.contains("test error"));
            assertTrue(trace.contains("RuntimeException"));
        }

        @Test
        @DisplayName("getStackTrace - maxLines")
        void getStackTraceMaxLines() {
            Exception ex = new RuntimeException("test");
            String trace = ExceptionUtils.getStackTrace(ex, 3);
            assertNotNull(trace);
        }

        @Test
        @DisplayName("getStackTrace - null")
        void getStackTraceNull() {
            assertNull(ExceptionUtils.getStackTrace(null));
            assertNull(ExceptionUtils.getStackTrace(null, 5));
        }

        @Test
        @DisplayName("getStackTraceElements")
        void getStackTraceElements() {
            Exception ex = new RuntimeException("test");
            assertTrue(ExceptionUtils.getStackTraceElements(ex).length > 0);
            assertEquals(0, ExceptionUtils.getStackTraceElements(null).length);
        }

        @Test
        @DisplayName("getStackTraceList")
        void getStackTraceList() {
            Exception ex = new RuntimeException("test");
            List<String> list = ExceptionUtils.getStackTraceList(ex);
            assertFalse(list.isEmpty());
            assertTrue(ExceptionUtils.getStackTraceList(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Root Cause")
    class RootCause {

        @Test
        @DisplayName("getRootCause")
        void getRootCause() {
            Exception root = new IllegalArgumentException("root");
            Exception middle = new RuntimeException("middle", root);
            Exception top = new Exception("top", middle);

            assertEquals(root, ExceptionUtils.getRootCause(top));
        }

        @Test
        @DisplayName("getRootCause - 단일 예외")
        void getRootCauseSingle() {
            Exception ex = new RuntimeException("single");
            assertEquals(ex, ExceptionUtils.getRootCause(ex));
        }

        @Test
        @DisplayName("getRootCause - null")
        void getRootCauseNull() {
            assertNull(ExceptionUtils.getRootCause(null));
        }

        @Test
        @DisplayName("getRootCauseMessage")
        void getRootCauseMessage() {
            Exception root = new IllegalArgumentException("root msg");
            Exception top = new RuntimeException("top msg", root);
            assertEquals("root msg", ExceptionUtils.getRootCauseMessage(top));
        }

        @Test
        @DisplayName("getRootCauseStackTrace")
        void getRootCauseStackTrace() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            String trace = ExceptionUtils.getRootCauseStackTrace(top);
            assertNotNull(trace);
            assertTrue(trace.contains("root"));
        }
    }

    @Nested
    @DisplayName("예외 체인")
    class ExceptionChain {

        @Test
        @DisplayName("getThrowableList")
        void getThrowableList() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            List<Throwable> list = ExceptionUtils.getThrowableList(top);
            assertEquals(2, list.size());
            assertEquals(top, list.get(0));
            assertEquals(root, list.get(1));
        }

        @Test
        @DisplayName("getThrowableCount")
        void getThrowableCount() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            assertEquals(2, ExceptionUtils.getThrowableCount(top));
            assertEquals(0, ExceptionUtils.getThrowableCount(null));
        }

        @Test
        @DisplayName("getThrowableByType")
        void getThrowableByType() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            assertNotNull(ExceptionUtils.getThrowableByType(top, IllegalArgumentException.class));
            assertNull(ExceptionUtils.getThrowableByType(top, IOException.class));
            assertNull(ExceptionUtils.getThrowableByType(null, RuntimeException.class));
        }

        @Test
        @DisplayName("containsType")
        void containsType() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            assertTrue(ExceptionUtils.containsType(top, IllegalArgumentException.class));
            assertFalse(ExceptionUtils.containsType(top, IOException.class));
        }
    }

    @Nested
    @DisplayName("메시지 추출")
    class MessageExtraction {

        @Test
        @DisplayName("getMessage")
        void getMessage() {
            assertEquals("test", ExceptionUtils.getMessage(new RuntimeException("test")));
            assertNull(ExceptionUtils.getMessage(null));
        }

        @Test
        @DisplayName("getAllMessages")
        void getAllMessages() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            String messages = ExceptionUtils.getAllMessages(top);
            assertTrue(messages.contains("top"));
            assertTrue(messages.contains("root"));
            assertNull(ExceptionUtils.getAllMessages(null));
        }

        @Test
        @DisplayName("getMessageList")
        void getMessageList() {
            Exception root = new IllegalArgumentException("root");
            Exception top = new RuntimeException("top", root);
            List<String> messages = ExceptionUtils.getMessageList(top);
            assertEquals(2, messages.size());
            assertTrue(ExceptionUtils.getMessageList(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("래핑 해제")
    class Unwrap {

        @Test
        @DisplayName("unwrapInvocationTargetException")
        void unwrapITE() throws Exception {
            Exception inner = new RuntimeException("inner");
            InvocationTargetException ite = new InvocationTargetException(inner);
            assertEquals(inner, ExceptionUtils.unwrapInvocationTargetException(ite));

            Exception other = new RuntimeException("other");
            assertEquals(other, ExceptionUtils.unwrapInvocationTargetException(other));
        }

        @Test
        @DisplayName("unwrapUndeclaredThrowableException")
        void unwrapUTE() {
            Exception inner = new RuntimeException("inner");
            UndeclaredThrowableException ute = new UndeclaredThrowableException(inner);
            assertEquals(inner, ExceptionUtils.unwrapUndeclaredThrowableException(ute));
        }

        @Test
        @DisplayName("unwrap")
        void unwrap() throws Exception {
            Exception inner = new RuntimeException("inner");
            InvocationTargetException ite = new InvocationTargetException(inner);
            assertEquals(inner, ExceptionUtils.unwrap(ite));
        }
    }

    @Nested
    @DisplayName("예외 타입 체크")
    class TypeCheck {

        @Test
        @DisplayName("isRuntimeException / isCheckedException / isError")
        void typeChecks() {
            assertTrue(ExceptionUtils.isRuntimeException(new RuntimeException()));
            assertFalse(ExceptionUtils.isRuntimeException(new IOException()));

            assertTrue(ExceptionUtils.isCheckedException(new IOException()));
            assertFalse(ExceptionUtils.isCheckedException(new RuntimeException()));
            assertFalse(ExceptionUtils.isCheckedException(null));

            assertTrue(ExceptionUtils.isError(new Error()));
            assertFalse(ExceptionUtils.isError(new RuntimeException()));
        }
    }

    @Nested
    @DisplayName("예외 래핑")
    class Wrapping {

        @Test
        @DisplayName("wrap")
        void wrap() {
            RuntimeException re = new RuntimeException("test");
            assertSame(re, ExceptionUtils.wrap(re));

            IOException checked = new IOException("checked");
            RuntimeException wrapped = ExceptionUtils.wrap(checked);
            assertEquals(checked, wrapped.getCause());
        }

        @Test
        @DisplayName("wrap with message")
        void wrapWithMessage() {
            IOException checked = new IOException("inner");
            RuntimeException wrapped = ExceptionUtils.wrap("outer", checked);
            assertEquals("outer", wrapped.getMessage());
            assertEquals(checked, wrapped.getCause());
        }
    }

    @Nested
    @DisplayName("예외 정보")
    class ExceptionInfo {

        @Test
        @DisplayName("getClassName / getSimpleClassName")
        void className() {
            RuntimeException ex = new RuntimeException("test");
            assertEquals("java.lang.RuntimeException", ExceptionUtils.getClassName(ex));
            assertEquals("RuntimeException", ExceptionUtils.getSimpleClassName(ex));
            assertNull(ExceptionUtils.getClassName(null));
            assertNull(ExceptionUtils.getSimpleClassName(null));
        }

        @Test
        @DisplayName("getThrowingMethod")
        void getThrowingMethod() {
            RuntimeException ex = new RuntimeException("test");
            String method = ExceptionUtils.getThrowingMethod(ex);
            assertNotNull(method);
            assertNull(ExceptionUtils.getThrowingMethod(null));
        }

        @Test
        @DisplayName("getSummary")
        void getSummary() {
            RuntimeException ex = new RuntimeException("test error");
            String summary = ExceptionUtils.getSummary(ex);
            assertTrue(summary.contains("RuntimeException"));
            assertTrue(summary.contains("test error"));
            assertEquals("null", ExceptionUtils.getSummary(null));
        }

        @Test
        @DisplayName("isSameType / isSameMessage")
        void comparison() {
            RuntimeException ex1 = new RuntimeException("msg");
            RuntimeException ex2 = new RuntimeException("msg");
            IOException ex3 = new IOException("msg");

            assertTrue(ExceptionUtils.isSameType(ex1, ex2));
            assertFalse(ExceptionUtils.isSameType(ex1, ex3));
            assertTrue(ExceptionUtils.isSameType(null, null));

            assertTrue(ExceptionUtils.isSameMessage(ex1, ex2));
            assertTrue(ExceptionUtils.isSameMessage(null, null));
            assertFalse(ExceptionUtils.isSameMessage(ex1, null));
        }
    }

    @Nested
    @DisplayName("사용자/개발자 메시지")
    class FriendlyMessages {

        @Test
        @DisplayName("toUserFriendlyMessage")
        void userFriendly() {
            assertEquals("test error", ExceptionUtils.toUserFriendlyMessage(new RuntimeException("test error")));
            assertTrue(ExceptionUtils.toUserFriendlyMessage(null).contains("알 수 없는 오류"));
            assertTrue(ExceptionUtils.toUserFriendlyMessage(new RuntimeException()).contains("RuntimeException"));
        }

        @Test
        @DisplayName("toDeveloperMessage")
        void developerMessage() {
            String msg = ExceptionUtils.toDeveloperMessage(new RuntimeException("test"));
            assertTrue(msg.contains("java.lang.RuntimeException"));
            assertEquals("null", ExceptionUtils.toDeveloperMessage(null));
        }
    }

    @Nested
    @DisplayName("예외 무시 실행")
    class IgnoreException {

        @Test
        @DisplayName("ignoreException Supplier")
        void ignoreExceptionSupplier() {
            String result = ExceptionUtils.ignoreException(() -> "hello");
            assertEquals("hello", result);

            String result2 = ExceptionUtils.ignoreException(() -> {
                throw new Exception("error");
            });
            assertNull(result2);
        }

        @Test
        @DisplayName("ignoreException Supplier 기본값")
        void ignoreExceptionSupplierDefault() {
            String result = ExceptionUtils.ignoreException(() -> {
                throw new Exception("error");
            }, "default");
            assertEquals("default", result);
        }

        @Test
        @DisplayName("ignoreException Runnable")
        void ignoreExceptionRunnable() {
            assertDoesNotThrow(() -> ExceptionUtils.ignoreException(() -> {
                throw new Exception("error");
            }));
        }
    }
}
