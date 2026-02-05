package com.eraf.core.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterTest {

    @Test
    @DisplayName("객체를 JSON 문자열로 변환")
    void testToJson() {
        // Given
        TestDto dto = new TestDto("test", 123);

        // When
        String json = JsonConverter.toJson(dto);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));
    }

    @Test
    @DisplayName("null 객체 JSON 변환")
    void testToJsonNull() {
        // When
        String json = JsonConverter.toJson(null);

        // Then
        assertNull(json);
    }

    @Test
    @DisplayName("Pretty Print JSON 변환")
    void testToJsonPretty() {
        // Given
        TestDto dto = new TestDto("test", 123);

        // When
        String json = JsonConverter.toJsonPretty(dto);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\n")); // 개행 문자 포함
    }

    @Test
    @DisplayName("JSON 문자열을 객체로 변환")
    void testFromJson() {
        // Given
        String json = "{\"name\":\"test\",\"value\":123}";

        // When
        TestDto dto = JsonConverter.fromJson(json, TestDto.class);

        // Then
        assertNotNull(dto);
        assertEquals("test", dto.getName());
        assertEquals(123, dto.getValue());
    }

    @Test
    @DisplayName("null/빈 JSON 파싱")
    void testFromJsonNullOrEmpty() {
        // Then
        assertNull(JsonConverter.fromJson(null, TestDto.class));
        assertNull(JsonConverter.fromJson("", TestDto.class));
        assertNull(JsonConverter.fromJson("   ", TestDto.class));
    }

    @Test
    @DisplayName("TypeReference를 사용한 제네릭 변환")
    void testFromJsonWithTypeReference() {
        // Given
        String json = "[{\"name\":\"test1\",\"value\":1},{\"name\":\"test2\",\"value\":2}]";

        // When
        List<TestDto> list = JsonConverter.fromJson(json, new TypeReference<List<TestDto>>() {});

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("test1", list.get(0).getName());
        assertEquals("test2", list.get(1).getName());
    }

    @Test
    @DisplayName("JSON 문자열을 List로 변환")
    void testFromJsonList() {
        // Given
        String json = "[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]";

        // When
        List<TestDto> list = JsonConverter.fromJsonList(json, TestDto.class);

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("JSON 문자열을 Map으로 변환")
    void testFromJsonMap() {
        // Given
        String json = "{\"key1\":\"value1\",\"key2\":123}";

        // When
        Map<String, Object> map = JsonConverter.fromJsonMap(json);

        // Then
        assertNotNull(map);
        assertEquals("value1", map.get("key1"));
        assertEquals(123, map.get("key2"));
    }

    @Test
    @DisplayName("객체 타입 변환")
    void testConvert() {
        // Given
        Map<String, Object> source = Map.of("name", "converted", "value", 999);

        // When
        TestDto dto = JsonConverter.convert(source, TestDto.class);

        // Then
        assertNotNull(dto);
        assertEquals("converted", dto.getName());
        assertEquals(999, dto.getValue());
    }

    @Test
    @DisplayName("null 객체 타입 변환")
    void testConvertNull() {
        // When
        TestDto dto = JsonConverter.convert(null, TestDto.class);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("알 수 없는 속성 무시")
    void testIgnoreUnknownProperties() {
        // Given
        String json = "{\"name\":\"test\",\"value\":123,\"unknownField\":\"ignored\"}";

        // When
        TestDto dto = JsonConverter.fromJson(json, TestDto.class);

        // Then
        assertNotNull(dto);
        assertEquals("test", dto.getName());
    }

    @Test
    @DisplayName("LocalDateTime 직렬화/역직렬화")
    void testLocalDateTime() {
        // Given
        DateTimeDto dto = new DateTimeDto();
        dto.setDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));

        // When
        String json = JsonConverter.toJson(dto);
        DateTimeDto restored = JsonConverter.fromJson(json, DateTimeDto.class);

        // Then
        assertNotNull(restored);
        assertEquals(dto.getDateTime(), restored.getDateTime());
    }

    @Test
    @DisplayName("잘못된 JSON 파싱 시 예외 발생")
    void testInvalidJson() {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        assertThrows(ConversionException.class, () ->
            JsonConverter.fromJson(invalidJson, TestDto.class));
    }

    @Test
    @DisplayName("ObjectMapper 인스턴스 조회")
    void testGetMapper() {
        // When
        var mapper = JsonConverter.getMapper();

        // Then
        assertNotNull(mapper);
    }

    // Test DTOs
    public static class TestDto {
        private String name;
        private int value;

        public TestDto() {}

        public TestDto(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class DateTimeDto {
        private LocalDateTime dateTime;

        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    }
}
