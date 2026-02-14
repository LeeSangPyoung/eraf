package com.eraf.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionUtils 테스트
 */
class CollectionUtilsTest {

    @Test
    void isEmpty_ShouldReturnTrueForNullOrEmpty() {
        assertThat(CollectionUtils.isEmpty((Collection<?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(List.of())).isTrue();
        assertThat(CollectionUtils.isEmpty(List.of("item"))).isFalse();
    }

    @Test
    void isNotEmpty_ShouldReturnTrueForNonEmpty() {
        assertThat(CollectionUtils.isNotEmpty((Collection<String>) null)).isFalse();
        assertThat(CollectionUtils.isNotEmpty(Collections.<String>emptyList())).isFalse();
        assertThat(CollectionUtils.isNotEmpty(List.of("item"))).isTrue();
    }

    @Test
    void isEmpty_ForMap_ShouldWork() {
        assertThat(CollectionUtils.isEmpty((Map<?, ?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(Map.of())).isTrue();
        assertThat(CollectionUtils.isEmpty(Map.of("key", "value"))).isFalse();
    }

    @Test
    void size_ShouldReturnSizeOrZero() {
        assertThat(CollectionUtils.size((Collection<?>) null)).isZero();
        assertThat(CollectionUtils.size(List.of())).isZero();
        assertThat(CollectionUtils.size(List.of(1, 2, 3))).isEqualTo(3);
    }

    @Test
    void first_ShouldReturnFirstElement() {
        assertThat(CollectionUtils.<Integer>first(null)).isNull();
        assertThat(CollectionUtils.<Integer>first(List.of())).isNull();
        assertThat(CollectionUtils.first(List.of(1, 2, 3))).isEqualTo(1);
        assertThat(CollectionUtils.first(Set.of(5, 3, 1))).isNotNull();
    }

    @Test
    void first_WithDefault_ShouldReturnDefaultWhenNull() {
        assertThat(CollectionUtils.first(null, 99)).isEqualTo(99);
        assertThat(CollectionUtils.first(List.of(), 99)).isEqualTo(99);
        assertThat(CollectionUtils.first(List.of(1, 2), 99)).isEqualTo(1);
    }

    @Test
    void last_ShouldReturnLastElement() {
        assertThat(CollectionUtils.<Integer>last(null)).isNull();
        assertThat(CollectionUtils.<Integer>last(List.of())).isNull();
        assertThat(CollectionUtils.last(List.of(1, 2, 3))).isEqualTo(3);
    }

    @Test
    void last_WithDefault_ShouldReturnDefaultWhenNull() {
        assertThat(CollectionUtils.last(null, 99)).isEqualTo(99);
        assertThat(CollectionUtils.last(List.of(), 99)).isEqualTo(99);
        assertThat(CollectionUtils.last(List.of(1, 2, 3), 99)).isEqualTo(3);
    }

    @Test
    void get_ShouldReturnElementAtIndex() {
        List<String> list = List.of("a", "b", "c");

        assertThat(CollectionUtils.get(list, 0)).isEqualTo("a");
        assertThat(CollectionUtils.get(list, 2)).isEqualTo("c");
        assertThat(CollectionUtils.get(list, 5)).isNull();
        assertThat(CollectionUtils.get(list, -1)).isNull();
        assertThat(CollectionUtils.<String>get(null, 0)).isNull();
    }

    @Test
    void get_WithDefault_ShouldReturnDefaultWhenOutOfBounds() {
        List<String> list = List.of("a", "b", "c");

        assertThat(CollectionUtils.get(list, 0, "default")).isEqualTo("a");
        assertThat(CollectionUtils.get(list, 5, "default")).isEqualTo("default");
        assertThat(CollectionUtils.get(null, 0, "default")).isEqualTo("default");
    }

    @Test
    void contains_ShouldCheckIfElementExists() {
        List<String> list = List.of("a", "b", "c");

        assertThat(CollectionUtils.contains(list, "b")).isTrue();
        assertThat(CollectionUtils.contains(list, "d")).isFalse();
        assertThat(CollectionUtils.contains(null, "a")).isFalse();
    }

    @Test
    void containsAll_ShouldCheckIfAllExist() {
        List<String> list = List.of("a", "b", "c", "d");

        assertThat(CollectionUtils.containsAll(list, List.of("a", "c"))).isTrue();
        assertThat(CollectionUtils.containsAll(list, List.of("a", "e"))).isFalse();
        assertThat(CollectionUtils.containsAll(null, List.of("a"))).isFalse();
    }

    @Test
    void containsAny_ShouldCheckIfAnyExists() {
        List<String> list = List.of("a", "b", "c");

        assertThat(CollectionUtils.containsAny(list, List.of("c", "d"))).isTrue();
        assertThat(CollectionUtils.containsAny(list, List.of("x", "y"))).isFalse();
        assertThat(CollectionUtils.containsAny(null, List.of("a"))).isFalse();
    }

    @Test
    void filter_ShouldFilterByPredicate() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        List<Integer> evens = CollectionUtils.filter(numbers, n -> n % 2 == 0);

        assertThat(evens).containsExactly(2, 4);
    }

    @Test
    void filterNotNull_ShouldRemoveNulls() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null, "c"));

        List<String> filtered = CollectionUtils.filterNotNull(list);

        assertThat(filtered).containsExactly("a", "b", "c");
    }

    @Test
    void map_ShouldTransformElements() {
        List<String> words = List.of("hello", "world");

        List<Integer> lengths = CollectionUtils.map(words, String::length);

        assertThat(lengths).containsExactly(5, 5);
    }

    @Test
    void distinct_ShouldRemoveDuplicates() {
        List<Integer> numbers = List.of(1, 2, 2, 3, 3, 3);

        List<Integer> unique = CollectionUtils.distinct(numbers);

        assertThat(unique).containsExactly(1, 2, 3);
    }

    @Test
    void distinctBy_ShouldRemoveDuplicatesByKey() {
        List<Person> people = List.of(
                new Person("John", 30),
                new Person("Jane", 25),
                new Person("John", 35) // Duplicate name
        );

        List<Person> unique = CollectionUtils.distinctBy(people, Person::getName);

        assertThat(unique).hasSize(2);
        assertThat(unique).extracting(Person::getName).containsExactly("John", "Jane");
    }

    @Test
    void toSet_ShouldConvertToSet() {
        List<Integer> list = List.of(1, 2, 2, 3);

        Set<Integer> set = CollectionUtils.toSet(list);

        assertThat(set).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void toList_FromSet_ShouldConvert() {
        Set<String> set = Set.of("a", "b", "c");

        List<String> list = CollectionUtils.toList(set);

        assertThat(list).hasSize(3);
        assertThat(list).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void toList_FromArray_ShouldConvert() {
        List<String> list = CollectionUtils.toList("a", "b", "c");

        assertThat(list).containsExactly("a", "b", "c");
    }

    @Test
    void toMap_WithKeyMapper_ShouldConvert() {
        List<Person> people = List.of(
                new Person("John", 30),
                new Person("Jane", 25)
        );

        Map<String, Person> map = CollectionUtils.toMap(people, Person::getName);

        assertThat(map).containsKeys("John", "Jane");
        assertThat(map.get("John").getAge()).isEqualTo(30);
    }

    @Test
    void toMap_WithKeyAndValueMapper_ShouldConvert() {
        List<Person> people = List.of(
                new Person("John", 30),
                new Person("Jane", 25)
        );

        Map<String, Integer> map = CollectionUtils.toMap(people, Person::getName, Person::getAge);

        assertThat(map).containsEntry("John", 30);
        assertThat(map).containsEntry("Jane", 25);
    }

    @Test
    void partition_ShouldSplitIntoChunks() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7);

        List<List<Integer>> partitions = CollectionUtils.partition(numbers, 3);

        assertThat(partitions).hasSize(3);
        assertThat(partitions.get(0)).containsExactly(1, 2, 3);
        assertThat(partitions.get(1)).containsExactly(4, 5, 6);
        assertThat(partitions.get(2)).containsExactly(7);
    }

    @Test
    void sort_ShouldSortInAscendingOrder() {
        List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9);

        List<Integer> sorted = CollectionUtils.sort(numbers);

        assertThat(sorted).containsExactly(1, 1, 3, 4, 5, 9);
    }

    @Test
    void sortReverse_ShouldSortInDescendingOrder() {
        List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9);

        List<Integer> sorted = CollectionUtils.sortReverse(numbers);

        assertThat(sorted).containsExactly(9, 5, 4, 3, 1, 1);
    }

    @Test
    void reverse_ShouldReverseOrder() {
        List<String> words = List.of("a", "b", "c");

        List<String> reversed = CollectionUtils.reverse(words);

        assertThat(reversed).containsExactly("c", "b", "a");
    }

    @Test
    void count_ShouldCountMatchingElements() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

        long count = CollectionUtils.count(numbers, n -> n % 2 == 0);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void allMatch_ShouldCheckIfAllMatch() {
        List<Integer> numbers = List.of(2, 4, 6);

        assertThat(CollectionUtils.allMatch(numbers, n -> n % 2 == 0)).isTrue();
        assertThat(CollectionUtils.allMatch(numbers, n -> n > 10)).isFalse();
    }

    @Test
    void anyMatch_ShouldCheckIfAnyMatches() {
        List<Integer> numbers = List.of(1, 3, 5, 6);

        assertThat(CollectionUtils.anyMatch(numbers, n -> n % 2 == 0)).isTrue();
        assertThat(CollectionUtils.anyMatch(numbers, n -> n > 10)).isFalse();
    }

    @Test
    void noneMatch_ShouldCheckIfNoneMatch() {
        List<Integer> numbers = List.of(1, 3, 5);

        assertThat(CollectionUtils.noneMatch(numbers, n -> n % 2 == 0)).isTrue();
        assertThat(CollectionUtils.noneMatch(numbers, n -> n > 0)).isFalse();
    }

    @Test
    void join_ShouldJoinElements() {
        List<String> words = List.of("hello", "world", "java");

        String joined = CollectionUtils.join(words, ", ");

        assertThat(joined).isEqualTo("hello, world, java");
    }

    @Test
    void join_WithPrefixAndSuffix_ShouldJoin() {
        List<Integer> numbers = List.of(1, 2, 3);

        String joined = CollectionUtils.join(numbers, ", ", "[", "]");

        assertThat(joined).isEqualTo("[1, 2, 3]");
    }

    @Test
    void intersection_ShouldReturnCommonElements() {
        List<Integer> list1 = List.of(1, 2, 3, 4);
        List<Integer> list2 = List.of(3, 4, 5, 6);

        Set<Integer> intersection = CollectionUtils.intersection(list1, list2);

        assertThat(intersection).containsExactlyInAnyOrder(3, 4);
    }

    @Test
    void union_ShouldReturnAllElements() {
        List<Integer> list1 = List.of(1, 2, 3);
        List<Integer> list2 = List.of(3, 4, 5);

        Set<Integer> union = CollectionUtils.union(list1, list2);

        assertThat(union).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }

    @Test
    void difference_ShouldReturnDifference() {
        List<Integer> list1 = List.of(1, 2, 3, 4);
        List<Integer> list2 = List.of(3, 4, 5, 6);

        Set<Integer> diff = CollectionUtils.difference(list1, list2);

        assertThat(diff).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void nullToEmpty_ShouldConvertNullToEmpty() {
        assertThat(CollectionUtils.nullToEmpty((List<String>) null)).isEmpty();
        assertThat(CollectionUtils.nullToEmpty((Set<String>) null)).isEmpty();
        assertThat(CollectionUtils.nullToEmpty((Map<String, String>) null)).isEmpty();

        List<String> list = List.of("a");
        assertThat(CollectionUtils.nullToEmpty(list)).isSameAs(list);
    }

    @Test
    void subList_ShouldExtractRange() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        List<Integer> sub = CollectionUtils.subList(numbers, 1, 4);

        assertThat(sub).containsExactly(2, 3, 4);
    }

    @Test
    void subList_WithInvalidRange_ShouldReturnEmpty() {
        List<Integer> numbers = List.of(1, 2, 3);

        assertThat(CollectionUtils.subList(numbers, -1, 1)).hasSize(1);
        assertThat(CollectionUtils.subList(numbers, 1, 10)).hasSize(2);
        assertThat(CollectionUtils.subList(numbers, 5, 10)).isEmpty();
    }

    // Test Helper Class
    static class Person {
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }
    }
}
