package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectUtils - 객체 유틸리티")
class ObjectUtilsTest {

    @Nested
    @DisplayName("Null 체크")
    class NullCheck {

        @Test
        @DisplayName("isNull / isNotNull")
        void basic() {
            assertTrue(ObjectUtils.isNull(null));
            assertFalse(ObjectUtils.isNull("test"));
            assertTrue(ObjectUtils.isNotNull("test"));
            assertFalse(ObjectUtils.isNotNull(null));
        }

        @Test
        @DisplayName("allNull")
        void allNull() {
            assertTrue(ObjectUtils.allNull(null, null));
            assertTrue(ObjectUtils.allNull((Object[]) null));
            assertFalse(ObjectUtils.allNull(null, "a"));
        }

        @Test
        @DisplayName("anyNull")
        void anyNull() {
            assertTrue(ObjectUtils.anyNull(null, "a"));
            assertTrue(ObjectUtils.anyNull((Object[]) null));
            assertFalse(ObjectUtils.anyNull("a", "b"));
        }

        @Test
        @DisplayName("allNotNull")
        void allNotNull() {
            assertTrue(ObjectUtils.allNotNull("a", "b"));
            assertFalse(ObjectUtils.allNotNull("a", null));
        }
    }

    @Nested
    @DisplayName("기본값 처리")
    class DefaultValues {

        @Test
        @DisplayName("defaultIfNull")
        void defaultIfNull() {
            assertEquals("default", ObjectUtils.defaultIfNull(null, "default"));
            assertEquals("value", ObjectUtils.defaultIfNull("value", "default"));
        }

        @Test
        @DisplayName("defaultIfNull (Supplier)")
        void defaultIfNullSupplier() {
            assertEquals("default", ObjectUtils.defaultIfNull(null, () -> "default"));
            assertEquals("value", ObjectUtils.defaultIfNull("value", () -> "default"));
        }

        @Test
        @DisplayName("defaultIfEmpty")
        void defaultIfEmpty() {
            assertEquals("default", ObjectUtils.defaultIfEmpty(null, "default"));
            assertEquals("default", ObjectUtils.defaultIfEmpty("", "default"));
            assertEquals("value", ObjectUtils.defaultIfEmpty("value", "default"));
        }

        @Test
        @DisplayName("firstNonNull")
        void firstNonNull() {
            assertEquals("a", ObjectUtils.firstNonNull(null, "a", "b"));
            assertEquals("b", ObjectUtils.firstNonNull(null, null, "b"));
            assertNull(ObjectUtils.firstNonNull(null, null));
            assertNull(ObjectUtils.firstNonNull((Object[]) null));
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("equals / deepEquals")
        void equalsTest() {
            assertTrue(ObjectUtils.equals("a", "a"));
            assertTrue(ObjectUtils.equals(null, null));
            assertFalse(ObjectUtils.equals("a", "b"));
            assertFalse(ObjectUtils.equals("a", null));

            assertTrue(ObjectUtils.deepEquals(new int[]{1, 2}, new int[]{1, 2}));
        }

        @Test
        @DisplayName("isSame / notSame")
        void sameTest() {
            String a = "test";
            assertTrue(ObjectUtils.isSame(a, a));
            assertTrue(ObjectUtils.notSame(new Object(), new Object()));
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("toString")
        void toStringTest() {
            assertEquals("123", ObjectUtils.toString(123));
            assertNull(ObjectUtils.toString(null));
        }

        @Test
        @DisplayName("toString 기본값")
        void toStringDefault() {
            assertEquals("default", ObjectUtils.toString(null, "default"));
            assertEquals("123", ObjectUtils.toString(123, "default"));
        }

        @Test
        @DisplayName("toStringOrEmpty")
        void toStringOrEmpty() {
            assertEquals("", ObjectUtils.toStringOrEmpty(null));
            assertEquals("123", ObjectUtils.toStringOrEmpty(123));
        }
    }

    @Nested
    @DisplayName("타입 체크")
    class TypeCheck {

        @Test
        @DisplayName("isInstanceOf")
        void isInstanceOf() {
            assertTrue(ObjectUtils.isInstanceOf("test", String.class));
            assertFalse(ObjectUtils.isInstanceOf("test", Integer.class));
            assertFalse(ObjectUtils.isInstanceOf(null, String.class));
            assertFalse(ObjectUtils.isInstanceOf("test", null));
        }

        @Test
        @DisplayName("isInstanceOfAny")
        void isInstanceOfAny() {
            assertTrue(ObjectUtils.isInstanceOfAny("test", Integer.class, String.class));
            assertFalse(ObjectUtils.isInstanceOfAny("test", Integer.class, Long.class));
            assertFalse(ObjectUtils.isInstanceOfAny(null, String.class));
        }
    }

    @Nested
    @DisplayName("타입 변환")
    class TypeCast {

        @Test
        @DisplayName("cast")
        void cast() {
            String result = ObjectUtils.cast("test", String.class);
            assertEquals("test", result);
            assertNull(ObjectUtils.cast("test", Integer.class));
            assertNull(ObjectUtils.cast(null, String.class));
        }

        @Test
        @DisplayName("cast 기본값")
        void castDefault() {
            assertEquals("test", ObjectUtils.cast("test", String.class, "default"));
            assertEquals("default", ObjectUtils.cast(123, String.class, "default"));
        }
    }

    @Nested
    @DisplayName("Empty 체크")
    class EmptyCheck {

        @Test
        @DisplayName("isEmpty - 다양한 타입")
        void isEmpty() {
            assertTrue(ObjectUtils.isEmpty(null));
            assertTrue(ObjectUtils.isEmpty(""));
            assertTrue(ObjectUtils.isEmpty(new ArrayList<>()));
            assertTrue(ObjectUtils.isEmpty(new HashMap<>()));
            assertTrue(ObjectUtils.isEmpty(new int[0]));
            assertTrue(ObjectUtils.isEmpty(Optional.empty()));

            assertFalse(ObjectUtils.isEmpty("text"));
            assertFalse(ObjectUtils.isEmpty(List.of("a")));
            assertFalse(ObjectUtils.isEmpty(Map.of("k", "v")));
            assertFalse(ObjectUtils.isEmpty(new int[]{1}));
            assertFalse(ObjectUtils.isEmpty(Optional.of("x")));
            assertFalse(ObjectUtils.isEmpty(123));
        }

        @Test
        @DisplayName("isNotEmpty")
        void isNotEmpty() {
            assertTrue(ObjectUtils.isNotEmpty("text"));
            assertFalse(ObjectUtils.isNotEmpty(null));
        }
    }

    @Nested
    @DisplayName("최대/최소/범위")
    class MinMaxRange {

        @Test
        @DisplayName("max / min (2인자)")
        void maxMin2() {
            assertEquals(5, ObjectUtils.max(3, 5));
            assertEquals(3, ObjectUtils.min(3, 5));
            assertEquals(5, ObjectUtils.max(null, 5));
            assertEquals(3, ObjectUtils.min(null, 3));
        }

        @Test
        @DisplayName("max / min (varargs)")
        void maxMinVarargs() {
            assertEquals(10, ObjectUtils.max(3, 10, 5));
            assertEquals(3, ObjectUtils.min(3, 10, 5));
            assertNull(ObjectUtils.max((Integer[]) null));
        }

        @Test
        @DisplayName("between")
        void between() {
            assertTrue(ObjectUtils.between(5, 1, 10));
            assertTrue(ObjectUtils.between(1, 1, 10));
            assertFalse(ObjectUtils.between(0, 1, 10));
            assertFalse(ObjectUtils.between(null, 1, 10));
        }

        @Test
        @DisplayName("clamp")
        void clamp() {
            assertEquals(5, ObjectUtils.clamp(5, 1, 10));
            assertEquals(1, ObjectUtils.clamp(0, 1, 10));
            assertEquals(10, ObjectUtils.clamp(15, 1, 10));
            assertEquals(1, ObjectUtils.clamp(null, 1, 10));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("ifNotNull / ifNull")
        void conditionalAction() {
            AtomicBoolean executed = new AtomicBoolean(false);
            ObjectUtils.ifNotNull("value", v -> executed.set(true));
            assertTrue(executed.get());

            executed.set(false);
            ObjectUtils.ifNotNull(null, v -> executed.set(true));
            assertFalse(executed.get());

            executed.set(false);
            ObjectUtils.ifNull(null, () -> executed.set(true));
            assertTrue(executed.get());
        }

        @Test
        @DisplayName("getClassName / getSimpleClassName")
        void className() {
            assertEquals("java.lang.String", ObjectUtils.getClassName("test"));
            assertEquals("String", ObjectUtils.getSimpleClassName("test"));
            assertNull(ObjectUtils.getClassName(null));
            assertNull(ObjectUtils.getSimpleClassName(null));
        }

        @Test
        @DisplayName("optional")
        void optional() {
            assertTrue(ObjectUtils.optional("test").isPresent());
            assertFalse(ObjectUtils.optional(null).isPresent());
        }

        @Test
        @DisplayName("getOrDefault")
        void getOrDefault() {
            assertEquals("test", ObjectUtils.getOrDefault(Optional.of("test"), "default"));
            assertEquals("default", ObjectUtils.getOrDefault(Optional.empty(), "default"));
            assertEquals("default", ObjectUtils.getOrDefault(null, "default"));
        }

        @Test
        @DisplayName("hashCode / hash")
        void hashCodeTest() {
            assertEquals(0, ObjectUtils.hashCode(null));
            assertNotEquals(0, ObjectUtils.hashCode("test"));
            assertNotEquals(0, ObjectUtils.hash("a", "b"));
        }

        @Test
        @DisplayName("toDebugString")
        void toDebugString() {
            assertEquals("null", ObjectUtils.toDebugString(null));
            assertTrue(ObjectUtils.toDebugString("test").startsWith("String@"));
        }

        @Test
        @DisplayName("isSameInstance / identityHashCode")
        void identityTests() {
            Object obj = new Object();
            assertTrue(ObjectUtils.isSameInstance(obj, obj));
            assertFalse(ObjectUtils.isSameInstance(obj, new Object()));
            assertTrue(ObjectUtils.identityHashCode(obj) > 0 || ObjectUtils.identityHashCode(obj) == 0);
        }

        @Test
        @DisplayName("clone")
        void cloneTest() {
            assertNull(ObjectUtils.clone(null));
            // String is not Cloneable
            assertNull(ObjectUtils.clone("test"));
        }
    }
}
