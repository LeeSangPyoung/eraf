package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArrayUtils - 배열 유틸리티")
class ArrayUtilsTest {

    @Nested
    @DisplayName("Empty 체크")
    class EmptyCheck {

        @Test
        @DisplayName("Object 배열")
        void objectArray() {
            assertTrue(ArrayUtils.isEmpty((Object[]) null));
            assertTrue(ArrayUtils.isEmpty(new Object[0]));
            assertFalse(ArrayUtils.isEmpty(new Object[]{"a"}));

            assertTrue(ArrayUtils.isNotEmpty(new Object[]{"a"}));
            assertFalse(ArrayUtils.isNotEmpty((Object[]) null));
        }

        @Test
        @DisplayName("primitive 배열")
        void primitiveArrays() {
            assertTrue(ArrayUtils.isEmpty((int[]) null));
            assertTrue(ArrayUtils.isEmpty(new int[0]));
            assertFalse(ArrayUtils.isEmpty(new int[]{1}));

            assertTrue(ArrayUtils.isEmpty((long[]) null));
            assertTrue(ArrayUtils.isEmpty((double[]) null));
            assertTrue(ArrayUtils.isEmpty((boolean[]) null));
        }
    }

    @Nested
    @DisplayName("크기")
    class Length {

        @Test
        @DisplayName("length (null-safe)")
        void length() {
            assertEquals(0, ArrayUtils.length((Object[]) null));
            assertEquals(3, ArrayUtils.length(new String[]{"a", "b", "c"}));
            assertEquals(0, ArrayUtils.length((int[]) null));
            assertEquals(2, ArrayUtils.length(new int[]{1, 2}));
            assertEquals(0, ArrayUtils.length((long[]) null));
            assertEquals(0, ArrayUtils.length((double[]) null));
        }
    }

    @Nested
    @DisplayName("포함 여부")
    class Contains {

        @Test
        @DisplayName("Object contains")
        void objectContains() {
            String[] arr = {"a", "b", "c"};
            assertTrue(ArrayUtils.contains(arr, "b"));
            assertFalse(ArrayUtils.contains(arr, "d"));
            assertFalse(ArrayUtils.contains((String[]) null, "a"));
        }

        @Test
        @DisplayName("int / long / double contains")
        void primitiveContains() {
            assertTrue(ArrayUtils.contains(new int[]{1, 2, 3}, 2));
            assertFalse(ArrayUtils.contains(new int[]{1, 2, 3}, 4));
            assertFalse(ArrayUtils.contains((int[]) null, 1));

            assertTrue(ArrayUtils.contains(new long[]{1L, 2L}, 2L));
            assertFalse(ArrayUtils.contains((long[]) null, 1L));

            assertTrue(ArrayUtils.contains(new double[]{1.0, 2.0}, 2.0));
            assertFalse(ArrayUtils.contains((double[]) null, 1.0));
        }
    }

    @Nested
    @DisplayName("인덱스 찾기")
    class IndexOf {

        @Test
        @DisplayName("indexOf")
        void indexOf() {
            String[] arr = {"a", "b", "c", "b"};
            assertEquals(1, ArrayUtils.indexOf(arr, "b"));
            assertEquals(-1, ArrayUtils.indexOf(arr, "d"));
            assertEquals(-1, ArrayUtils.indexOf((String[]) null, "a"));
        }

        @Test
        @DisplayName("int indexOf")
        void intIndexOf() {
            assertEquals(1, ArrayUtils.indexOf(new int[]{10, 20, 30}, 20));
            assertEquals(-1, ArrayUtils.indexOf((int[]) null, 1));
        }

        @Test
        @DisplayName("lastIndexOf")
        void lastIndexOf() {
            String[] arr = {"a", "b", "c", "b"};
            assertEquals(3, ArrayUtils.lastIndexOf(arr, "b"));
            assertEquals(-1, ArrayUtils.lastIndexOf(arr, "d"));
            assertEquals(-1, ArrayUtils.lastIndexOf((String[]) null, "a"));
        }
    }

    @Nested
    @DisplayName("조회")
    class Access {

        @Test
        @DisplayName("first / last")
        void firstLast() {
            String[] arr = {"a", "b", "c"};
            assertEquals("a", ArrayUtils.first(arr));
            assertEquals("c", ArrayUtils.last(arr));
            assertNull(ArrayUtils.first((String[]) null));
            assertNull(ArrayUtils.last((String[]) null));
        }

        @Test
        @DisplayName("get")
        void get() {
            String[] arr = {"a", "b", "c"};
            assertEquals("b", ArrayUtils.get(arr, 1));
            assertNull(ArrayUtils.get(arr, -1));
            assertNull(ArrayUtils.get(arr, 5));
            assertNull(ArrayUtils.get(null, 0));
        }

        @Test
        @DisplayName("get 기본값")
        void getDefault() {
            assertEquals("default", ArrayUtils.get(null, 0, "default"));
            assertEquals("a", ArrayUtils.get(new String[]{"a"}, 0, "default"));
        }
    }

    @Nested
    @DisplayName("배열 생성")
    class Creation {

        @Test
        @DisplayName("empty / newArray")
        void emptyAndNew() {
            String[] empty = ArrayUtils.empty(String.class);
            assertEquals(0, empty.length);

            String[] arr = ArrayUtils.newArray(String.class, 5);
            assertEquals(5, arr.length);
        }

        @Test
        @DisplayName("of")
        void of() {
            String[] arr = ArrayUtils.of("a", "b", "c");
            assertEquals(3, arr.length);
        }
    }

    @Nested
    @DisplayName("추가/제거")
    class AddRemove {

        @Test
        @DisplayName("add 끝에")
        void addEnd() {
            String[] arr = {"a", "b"};
            String[] result = ArrayUtils.add(arr, "c");
            assertArrayEquals(new String[]{"a", "b", "c"}, result);
        }

        @Test
        @DisplayName("add null 배열에")
        void addToNull() {
            String[] result = ArrayUtils.add(null, "a");
            assertArrayEquals(new String[]{"a"}, result);
        }

        @Test
        @DisplayName("add 특정 인덱스에")
        void addAtIndex() {
            String[] arr = {"a", "c"};
            String[] result = ArrayUtils.add(arr, 1, "b");
            assertArrayEquals(new String[]{"a", "b", "c"}, result);
        }

        @Test
        @DisplayName("remove 인덱스로")
        void removeByIndex() {
            String[] arr = {"a", "b", "c"};
            String[] result = ArrayUtils.remove(arr, 1);
            assertArrayEquals(new String[]{"a", "c"}, result);
        }

        @Test
        @DisplayName("removeElement")
        void removeElement() {
            String[] arr = {"a", "b", "c"};
            String[] result = ArrayUtils.removeElement(arr, "b");
            assertArrayEquals(new String[]{"a", "c"}, result);

            // 없는 요소 제거 시 원본 반환
            assertSame(arr, ArrayUtils.removeElement(arr, "d"));
        }
    }

    @Nested
    @DisplayName("변환")
    class Conversion {

        @Test
        @DisplayName("toList")
        void toList() {
            List<String> list = ArrayUtils.toList(new String[]{"a", "b"});
            assertEquals(2, list.size());
            assertTrue(ArrayUtils.toList((String[]) null).isEmpty());
        }

        @Test
        @DisplayName("primitive toList")
        void primitiveToList() {
            assertEquals(List.of(1, 2, 3), ArrayUtils.toList(new int[]{1, 2, 3}));
            assertEquals(List.of(1L, 2L), ArrayUtils.toList(new long[]{1L, 2L}));
            assertEquals(List.of(1.0, 2.0), ArrayUtils.toList(new double[]{1.0, 2.0}));
        }

        @Test
        @DisplayName("toSet")
        void toSet() {
            Set<String> set = ArrayUtils.toSet(new String[]{"a", "b", "a"});
            assertEquals(2, set.size());
            assertTrue(ArrayUtils.toSet((String[]) null).isEmpty());
        }

        @Test
        @DisplayName("toPrimitive / toObject (int)")
        void intConversion() {
            int[] primitive = ArrayUtils.toPrimitive(new Integer[]{1, 2, null});
            assertArrayEquals(new int[]{1, 2, 0}, primitive);
            assertNull(ArrayUtils.toPrimitive((Integer[]) null));

            Integer[] boxed = ArrayUtils.toObject(new int[]{1, 2});
            assertArrayEquals(new Integer[]{1, 2}, boxed);
            assertNull(ArrayUtils.toObject((int[]) null));
        }

        @Test
        @DisplayName("toPrimitive / toObject (long)")
        void longConversion() {
            long[] primitive = ArrayUtils.toPrimitive(new Long[]{1L, 2L, null});
            assertArrayEquals(new long[]{1L, 2L, 0L}, primitive);
            assertNull(ArrayUtils.toPrimitive((Long[]) null));

            Long[] boxed = ArrayUtils.toObject(new long[]{1L, 2L});
            assertArrayEquals(new Long[]{1L, 2L}, boxed);
            assertNull(ArrayUtils.toObject((long[]) null));
        }
    }

    @Nested
    @DisplayName("병합/서브배열")
    class ConcatSubarray {

        @Test
        @DisplayName("concat")
        void concat() {
            String[] a = {"a", "b"};
            String[] b = {"c", "d"};
            assertArrayEquals(new String[]{"a", "b", "c", "d"}, ArrayUtils.concat(a, b));
            assertNull(ArrayUtils.concat((String[][]) null));
        }

        @Test
        @DisplayName("subarray")
        void subarray() {
            String[] arr = {"a", "b", "c", "d"};
            assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(arr, 1, 3));
            assertNull(ArrayUtils.subarray(null, 0, 1));
        }
    }

    @Nested
    @DisplayName("정렬")
    class Sorting {

        @Test
        @DisplayName("sort (원본 유지)")
        void sort() {
            Integer[] arr = {3, 1, 2};
            Integer[] sorted = ArrayUtils.sort(arr);
            assertArrayEquals(new Integer[]{1, 2, 3}, sorted);
            assertArrayEquals(new Integer[]{3, 1, 2}, arr); // 원본 유지

            assertNull(ArrayUtils.sort((Integer[]) null));
        }

        @Test
        @DisplayName("int sort")
        void intSort() {
            int[] arr = {3, 1, 2};
            int[] sorted = ArrayUtils.sort(arr);
            assertArrayEquals(new int[]{1, 2, 3}, sorted);
            assertNull(ArrayUtils.sort((int[]) null));
        }

        @Test
        @DisplayName("reverse")
        void reverse() {
            String[] arr = {"a", "b", "c"};
            assertArrayEquals(new String[]{"c", "b", "a"}, ArrayUtils.reverse(arr));
            assertNull(ArrayUtils.reverse((String[]) null));
        }

        @Test
        @DisplayName("int reverse")
        void intReverse() {
            int[] arr = {1, 2, 3};
            assertArrayEquals(new int[]{3, 2, 1}, ArrayUtils.reverse(arr));
            assertNull(ArrayUtils.reverse((int[]) null));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("join")
        void join() {
            assertEquals("a,b,c", ArrayUtils.join(new String[]{"a", "b", "c"}, ","));
            assertEquals("", ArrayUtils.join((String[]) null, ","));
            assertEquals("1-2-3", ArrayUtils.join(new int[]{1, 2, 3}, "-"));
            assertEquals("", ArrayUtils.join((int[]) null, ","));
        }

        @Test
        @DisplayName("clone")
        void cloneTest() {
            String[] arr = {"a", "b"};
            assertArrayEquals(arr, ArrayUtils.clone(arr));
            assertNull(ArrayUtils.clone((String[]) null));

            int[] intArr = {1, 2};
            assertArrayEquals(intArr, ArrayUtils.clone(intArr));
            assertNull(ArrayUtils.clone((int[]) null));
        }

        @Test
        @DisplayName("equals")
        void equalsTest() {
            assertTrue(ArrayUtils.equals(new String[]{"a"}, new String[]{"a"}));
            assertFalse(ArrayUtils.equals(new String[]{"a"}, new String[]{"b"}));

            assertTrue(ArrayUtils.equals(new int[]{1}, new int[]{1}));
            assertFalse(ArrayUtils.equals(new int[]{1}, new int[]{2}));
        }

        @Test
        @DisplayName("toString")
        void toStringTest() {
            assertEquals("[a, b]", ArrayUtils.toString(new String[]{"a", "b"}));
            assertEquals("[1, 2]", ArrayUtils.toString(new int[]{1, 2}));
        }

        @Test
        @DisplayName("nullToEmpty / emptyToNull")
        void nullEmpty() {
            assertEquals(0, ArrayUtils.nullToEmpty(null, String.class).length);
            assertEquals(2, ArrayUtils.nullToEmpty(new String[]{"a", "b"}, String.class).length);

            assertNull(ArrayUtils.emptyToNull(new String[0]));
            assertNull(ArrayUtils.emptyToNull(null));
            assertNotNull(ArrayUtils.emptyToNull(new String[]{"a"}));
        }
    }
}
