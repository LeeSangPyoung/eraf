package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MapUtils - Map 유틸리티")
class MapUtilsTest {

    @Nested
    @DisplayName("Null/Empty 체크")
    class NullEmpty {

        @Test
        @DisplayName("isEmpty / isNotEmpty")
        void isEmptyIsNotEmpty() {
            assertTrue(MapUtils.isEmpty(null));
            assertTrue(MapUtils.isEmpty(Map.of()));
            assertFalse(MapUtils.isEmpty(Map.of("k", "v")));

            assertFalse(MapUtils.isNotEmpty(null));
            assertTrue(MapUtils.isNotEmpty(Map.of("k", "v")));
        }

        @Test
        @DisplayName("size")
        void size() {
            assertEquals(0, MapUtils.size(null));
            assertEquals(2, MapUtils.size(Map.of("a", 1, "b", 2)));
        }
    }

    @Nested
    @DisplayName("안전한 조회")
    class SafeGet {

        @Test
        @DisplayName("get")
        void get() {
            Map<String, Object> map = Map.of("key", "value");
            assertEquals("value", MapUtils.get(map, "key"));
            assertNull(MapUtils.get(map, "missing"));
            assertNull(MapUtils.get(null, "key"));
        }

        @Test
        @DisplayName("get 기본값")
        void getDefault() {
            assertEquals("default", MapUtils.get(null, "key", "default"));
            assertEquals("value", MapUtils.get(Map.of("key", "value"), "key", "default"));
        }

        @Test
        @DisplayName("getString")
        void getString() {
            Map<String, Object> map = Map.of("name", "test", "num", 123);
            assertEquals("test", MapUtils.getString(map, "name"));
            assertEquals("123", MapUtils.getString(map, "num"));
            assertNull(MapUtils.getString(map, "missing"));
            assertEquals("default", MapUtils.getString(map, "missing", "default"));
        }

        @Test
        @DisplayName("getInteger")
        void getInteger() {
            Map<String, Object> map = new HashMap<>();
            map.put("int", 42);
            map.put("str", "123");
            map.put("bad", "abc");

            assertEquals(42, MapUtils.getInteger(map, "int"));
            assertEquals(123, MapUtils.getInteger(map, "str"));
            assertNull(MapUtils.getInteger(map, "bad"));
            assertEquals(99, MapUtils.getInteger(map, "bad", 99));
            assertEquals(10, MapUtils.getInt(map, "missing", 10));
        }

        @Test
        @DisplayName("getLong")
        void getLong() {
            Map<String, Object> map = Map.of("val", 100L, "str", "200");
            assertEquals(100L, MapUtils.getLong(map, "val"));
            assertEquals(200L, MapUtils.getLong(map, "str"));
            assertNull(MapUtils.getLong(null, "val"));
        }

        @Test
        @DisplayName("getDouble")
        void getDouble() {
            Map<String, Object> map = Map.of("val", 3.14, "str", "2.71");
            assertEquals(3.14, MapUtils.getDouble(map, "val"), 0.001);
            assertEquals(2.71, MapUtils.getDouble(map, "str"), 0.001);
        }

        @Test
        @DisplayName("getBoolean")
        void getBoolean() {
            Map<String, Object> map = new HashMap<>();
            map.put("bool", true);
            map.put("str", "yes");
            map.put("num", "1");
            map.put("bad", "maybe");

            assertTrue(MapUtils.getBoolean(map, "bool"));
            assertTrue(MapUtils.getBoolean(map, "str"));
            assertTrue(MapUtils.getBoolean(map, "num"));
            assertNull(MapUtils.getBoolean(map, "bad"));
        }

        @Test
        @DisplayName("getBigDecimal")
        void getBigDecimal() {
            Map<String, Object> map = new HashMap<>();
            map.put("bd", new BigDecimal("123.45"));
            map.put("num", 100);
            map.put("str", "200.50");

            assertEquals(new BigDecimal("123.45"), MapUtils.getBigDecimal(map, "bd"));
            assertNotNull(MapUtils.getBigDecimal(map, "num"));
            assertEquals(new BigDecimal("200.50"), MapUtils.getBigDecimal(map, "str"));
        }
    }

    @Nested
    @DisplayName("포함 여부")
    class Contains {

        @Test
        @DisplayName("containsKey / containsValue")
        void containsKeyValue() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);
            assertTrue(MapUtils.containsKey(map, "a"));
            assertFalse(MapUtils.containsKey(null, "a"));
            assertTrue(MapUtils.containsValue(map, 1));
            assertFalse(MapUtils.containsValue(null, 1));
        }

        @Test
        @DisplayName("containsAllKeys / containsAnyKey")
        void containsAllAny() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
            assertTrue(MapUtils.containsAllKeys(map, "a", "b"));
            assertFalse(MapUtils.containsAllKeys(map, "a", "d"));
            assertTrue(MapUtils.containsAnyKey(map, "a", "d"));
            assertFalse(MapUtils.containsAnyKey(map, "x", "y"));
        }
    }

    @Nested
    @DisplayName("안전한 추가")
    class SafePut {

        @Test
        @DisplayName("putSafely")
        void putSafely() {
            Map<String, String> result = MapUtils.putSafely(null, "key", "value");
            assertEquals("value", result.get("key"));
        }

        @Test
        @DisplayName("putIfNotNull")
        void putIfNotNull() {
            Map<String, String> map = new HashMap<>();
            MapUtils.putIfNotNull(map, "key", "value");
            MapUtils.putIfNotNull(map, "null", null);
            assertEquals(1, map.size());
        }

        @Test
        @DisplayName("putIfAbsent")
        void putIfAbsent() {
            Map<String, String> map = new HashMap<>();
            map.put("key", "old");
            MapUtils.putIfAbsent(map, "key", "new");
            assertEquals("old", map.get("key"));
        }
    }

    @Nested
    @DisplayName("변환")
    class Transformation {

        @Test
        @DisplayName("invert")
        void invert() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);
            Map<Integer, String> inverted = MapUtils.invert(map);
            assertEquals("a", inverted.get(1));
            assertEquals("b", inverted.get(2));
            assertTrue(MapUtils.invert((Map<Object, Object>) null).isEmpty());
        }

        @Test
        @DisplayName("mapValues")
        void mapValues() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);
            Map<String, String> result = MapUtils.mapValues(map, v -> "val" + v);
            assertEquals("val1", result.get("a"));
        }

        @Test
        @DisplayName("mapKeys")
        void mapKeys() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);
            Map<String, Integer> result = MapUtils.mapKeys(map, k -> k.toUpperCase());
            assertEquals(1, result.get("A"));
        }

        @Test
        @DisplayName("filter")
        void filter() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
            Map<String, Integer> filtered = MapUtils.filter(map, (k, v) -> v > 1);
            assertEquals(2, filtered.size());
            assertFalse(filtered.containsKey("a"));
        }

        @Test
        @DisplayName("filterKeys")
        void filterKeys() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
            Map<String, Integer> filtered = MapUtils.filterKeys(map, "a", "c");
            assertEquals(2, filtered.size());
        }

        @Test
        @DisplayName("filterNotNullValues")
        void filterNotNull() {
            Map<String, String> map = new HashMap<>();
            map.put("a", "1");
            map.put("b", null);
            Map<String, String> filtered = MapUtils.filterNotNullValues(map);
            assertEquals(1, filtered.size());
        }
    }

    @Nested
    @DisplayName("병합/정렬")
    class MergeSort {

        @Test
        @DisplayName("merge (varargs)")
        void mergeVarargs() {
            Map<String, Integer> m1 = Map.of("a", 1);
            Map<String, Integer> m2 = Map.of("b", 2);
            Map<String, Integer> merged = MapUtils.merge(m1, m2);
            assertEquals(2, merged.size());
        }

        @Test
        @DisplayName("merge with function")
        void mergeWithFunction() {
            Map<String, Integer> m1 = new HashMap<>(Map.of("a", 1));
            Map<String, Integer> m2 = new HashMap<>(Map.of("a", 2, "b", 3));
            Map<String, Integer> merged = MapUtils.merge(m1, m2, Integer::sum);
            assertEquals(3, merged.get("a"));
            assertEquals(3, merged.get("b"));
        }

        @Test
        @DisplayName("sortByKey")
        void sortByKey() {
            Map<String, Integer> map = new HashMap<>();
            map.put("c", 3);
            map.put("a", 1);
            map.put("b", 2);
            Map<String, Integer> sorted = MapUtils.sortByKey(map);
            assertEquals("a", sorted.keySet().iterator().next());
        }

        @Test
        @DisplayName("sortByValue / sortByValueReverse")
        void sortByValue() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 3);
            map.put("b", 1);
            map.put("c", 2);

            Map<String, Integer> sorted = MapUtils.sortByValue(map);
            assertEquals("b", sorted.keySet().iterator().next());

            Map<String, Integer> reversed = MapUtils.sortByValueReverse(map);
            assertEquals("a", reversed.keySet().iterator().next());
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("nullToEmpty / emptyToNull")
        void nullEmpty() {
            assertNotNull(MapUtils.nullToEmpty(null));
            assertNull(MapUtils.emptyToNull(Map.of()));
            assertNull(MapUtils.emptyToNull(null));
            assertNotNull(MapUtils.emptyToNull(Map.of("k", "v")));
        }

        @Test
        @DisplayName("copy / unmodifiable")
        void copyUnmodifiable() {
            Map<String, Integer> map = Map.of("a", 1);
            Map<String, Integer> copy = MapUtils.copy(map);
            assertEquals(map, copy);
            assertNull(MapUtils.copy(null));

            Map<String, Integer> unmod = MapUtils.unmodifiable(map);
            assertThrows(UnsupportedOperationException.class, () -> unmod.put("b", 2));
        }

        @Test
        @DisplayName("toString")
        void toStringTest() {
            assertEquals("null", MapUtils.toString(null));
            assertEquals("{}", MapUtils.toString(Map.of()));
            String str = MapUtils.toString(Map.of("a", 1));
            assertTrue(str.contains("a=1"));
        }

        @Test
        @DisplayName("of 헬퍼")
        void ofHelpers() {
            assertEquals(1, MapUtils.of("a", 1).size());
            assertEquals(2, MapUtils.of("a", 1, "b", 2).size());
            assertEquals(3, MapUtils.of("a", 1, "b", 2, "c", 3).size());
        }
    }
}
