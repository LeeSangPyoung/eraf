package com.eraf.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonUtils 테스트
 */
class JsonUtilsTest {

    @Test
    void toJson_ShouldConvertObjectToJson() {
        // given
        TestUser user = new TestUser("John", 30);

        // when
        String json = JsonUtils.toJson(user);

        // then
        assertThat(json).contains("\"name\":\"John\"");
        assertThat(json).contains("\"age\":30");
    }

    @Test
    void toJson_WithNull_ShouldReturnNull() {
        assertThat(JsonUtils.toJson(null)).isNull();
    }

    @Test
    void fromJson_ShouldConvertJsonToObject() {
        // given
        String json = "{\"name\":\"Jane\",\"age\":25}";

        // when
        TestUser user = JsonUtils.fromJson(json, TestUser.class);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Jane");
        assertThat(user.getAge()).isEqualTo(25);
    }

    @Test
    void fromJson_WithNull_ShouldReturnNull() {
        assertThat(JsonUtils.fromJson((String) null, TestUser.class)).isNull();
        assertThat(JsonUtils.fromJson("", TestUser.class)).isNull();
        assertThat(JsonUtils.fromJson("   ", TestUser.class)).isNull();
    }

    @Test
    void fromJson_WithTypeReference_ShouldConvertToComplexType() {
        // given
        String json = "[{\"name\":\"A\",\"age\":10},{\"name\":\"B\",\"age\":20}]";

        // when
        List<TestUser> users = JsonUtils.fromJson(json, new TypeReference<List<TestUser>>() {});

        // then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("A");
        assertThat(users.get(1).getName()).isEqualTo("B");
    }

    @Test
    void toMap_ShouldConvertJsonToMap() {
        // given
        String json = "{\"key1\":\"value1\",\"key2\":123}";

        // when
        Map<String, Object> map = JsonUtils.toMap(json);

        // then
        assertThat(map).containsEntry("key1", "value1");
        assertThat(map).containsEntry("key2", 123);
    }

    @Test
    void toList_ShouldConvertJsonToList() {
        // given
        String json = "[{\"name\":\"A\",\"age\":10},{\"name\":\"B\",\"age\":20}]";

        // when
        List<TestUser> users = JsonUtils.toList(json, TestUser.class);

        // then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("A");
    }

    @Test
    void toPrettyJson_ShouldFormatWithIndentation() {
        // given
        TestUser user = new TestUser("Alice", 28);

        // when
        String prettyJson = JsonUtils.toPrettyJson(user);

        // then
        assertThat(prettyJson).contains("\n");
        assertThat(prettyJson).contains("  ");
    }

    @Test
    void isValidJson_ShouldValidateJsonString() {
        assertThat(JsonUtils.isValidJson("{\"key\":\"value\"}")).isTrue();
        assertThat(JsonUtils.isValidJson("[1,2,3]")).isTrue();
        assertThat(JsonUtils.isValidJson("invalid")).isFalse();
        assertThat(JsonUtils.isValidJson(null)).isFalse();
        assertThat(JsonUtils.isValidJson("")).isFalse();
    }

    @Test
    void isJsonObject_ShouldCheckIfJsonIsObject() {
        assertThat(JsonUtils.isJsonObject("{\"key\":\"value\"}")).isTrue();
        assertThat(JsonUtils.isJsonObject("[1,2,3]")).isFalse();
        assertThat(JsonUtils.isJsonObject("invalid")).isFalse();
    }

    @Test
    void isJsonArray_ShouldCheckIfJsonIsArray() {
        assertThat(JsonUtils.isJsonArray("[1,2,3]")).isTrue();
        assertThat(JsonUtils.isJsonArray("{\"key\":\"value\"}")).isFalse();
        assertThat(JsonUtils.isJsonArray("invalid")).isFalse();
    }

    @Test
    void convertValue_ShouldConvertObjectToAnotherType() {
        // given
        Map<String, Object> map = Map.of("name", "Bob", "age", 35);

        // when
        TestUser user = JsonUtils.convertValue(map, TestUser.class);

        // then
        assertThat(user.getName()).isEqualTo("Bob");
        assertThat(user.getAge()).isEqualTo(35);
    }

    @Test
    void objectToMap_ShouldConvertObjectToMap() {
        // given
        TestUser user = new TestUser("Charlie", 40);

        // when
        Map<String, Object> map = JsonUtils.objectToMap(user);

        // then
        assertThat(map).containsEntry("name", "Charlie");
        assertThat(map).containsEntry("age", 40);
    }

    @Test
    void mapToObject_ShouldConvertMapToObject() {
        // given
        Map<String, Object> map = Map.of("name", "David", "age", 45);

        // when
        TestUser user = JsonUtils.mapToObject(map, TestUser.class);

        // then
        assertThat(user.getName()).isEqualTo("David");
        assertThat(user.getAge()).isEqualTo(45);
    }

    @Test
    void toJsonQuietly_ShouldReturnNullOnError() {
        // given
        Object invalidObject = new Object() {
            public Object getSelf() {
                return this; // Circular reference
            }
        };

        // when
        String json = JsonUtils.toJsonQuietly(invalidObject);

        // then - circular reference로 직렬화 실패시 null 반환
        assertThat(json).isNull();
    }

    @Test
    void fromJsonQuietly_ShouldReturnNullOnError() {
        // given
        String invalidJson = "{invalid json}";

        // when
        TestUser user = JsonUtils.fromJsonQuietly(invalidJson, TestUser.class);

        // then
        assertThat(user).isNull();
    }

    @Test
    void deepCopy_ShouldCreateDeepCopy() {
        // given
        TestUser original = new TestUser("Original", 50);

        // when
        TestUser copy = JsonUtils.deepCopy(original, TestUser.class);

        // then
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getName()).isEqualTo("Original");
        assertThat(copy.getAge()).isEqualTo(50);

        // Modify original
        original.setName("Modified");
        assertThat(copy.getName()).isEqualTo("Original"); // Not affected
    }

    @Test
    void toJsonBytes_ShouldConvertToByteArray() {
        // given
        TestUser user = new TestUser("Bytes", 60);

        // when
        byte[] bytes = JsonUtils.toJsonBytes(user);

        // then
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(0);
    }

    @Test
    void fromJsonBytes_ShouldConvertFromByteArray() {
        // given
        TestUser user = new TestUser("FromBytes", 70);
        byte[] bytes = JsonUtils.toJsonBytes(user);

        // when
        TestUser result = JsonUtils.fromJson(bytes, TestUser.class);

        // then
        assertThat(result.getName()).isEqualTo("FromBytes");
        assertThat(result.getAge()).isEqualTo(70);
    }

    @Test
    void minify_ShouldRemoveWhitespace() {
        // given
        String prettyJson = "{\n  \"key\" : \"value\"\n}";

        // when
        String minified = JsonUtils.minify(prettyJson);

        // then
        assertThat(minified).doesNotContain("\n");
        assertThat(minified).contains("\"key\":\"value\"");
    }

    @Test
    void prettify_ShouldAddWhitespace() {
        // given
        String minifiedJson = "{\"key\":\"value\"}";

        // when
        String prettified = JsonUtils.prettify(minifiedJson);

        // then
        assertThat(prettified).contains("\n");
    }

    // Test Helper Class
    static class TestUser {
        private String name;
        private int age;

        public TestUser() {
        }

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
