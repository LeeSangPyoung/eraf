package com.eraf.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * JSON 유틸리티 (Jackson 기반)
 * REST API 개발의 필수 유틸리티
 */
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = createDefaultObjectMapper();
    private static final ObjectMapper PRETTY_MAPPER = createPrettyObjectMapper();

    private JsonUtils() {
    }

    /**
     * 기본 ObjectMapper 생성
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Pretty Print용 ObjectMapper 생성
     */
    private static ObjectMapper createPrettyObjectMapper() {
        ObjectMapper mapper = createDefaultObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    // ===== 객체 → JSON =====

    /**
     * 객체를 JSON 문자열로 변환
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * 객체를 JSON 문자열로 변환 (예외 무시)
     */
    public static String toJsonQuietly(Object obj) {
        try {
            return toJson(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 객체를 보기 좋은 JSON 문자열로 변환 (Pretty Print)
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return PRETTY_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to pretty JSON", e);
        }
    }

    /**
     * 객체를 JSON 바이트 배열로 변환
     */
    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON bytes", e);
        }
    }

    // ===== JSON → 객체 =====

    /**
     * JSON 문자열을 객체로 변환
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환 (예외 무시)
     */
    public static <T> T fromJsonQuietly(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON 문자열을 객체로 변환 (TypeReference 사용)
     * 예: List<User>, Map<String, User> 등
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * JSON 바이트 배열을 객체로 변환
     */
    public static <T> T fromJson(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON bytes to object", e);
        }
    }

    // ===== JSON → Map/List =====

    /**
     * JSON 문자열을 Map으로 변환
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * JSON 문자열을 Map으로 변환 (예외 무시)
     */
    public static Map<String, Object> toMapQuietly(String json) {
        try {
            return toMap(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON 문자열을 List로 변환
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to list", e);
        }
    }

    /**
     * JSON 문자열을 Object List로 변환
     */
    public static List<Object> toList(String json) {
        return fromJson(json, new TypeReference<List<Object>>() {});
    }

    // ===== 파일 입출력 =====

    /**
     * 객체를 JSON 파일로 저장
     */
    public static void toJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            throw new IllegalArgumentException("Object and file must not be null");
        }
        try {
            OBJECT_MAPPER.writeValue(file, obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to file", e);
        }
    }

    /**
     * 객체를 보기 좋은 JSON 파일로 저장
     */
    public static void toPrettyJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            throw new IllegalArgumentException("Object and file must not be null");
        }
        try {
            PRETTY_MAPPER.writeValue(file, obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write pretty JSON to file", e);
        }
    }

    /**
     * JSON 파일에서 객체 읽기
     */
    public static <T> T fromJsonFile(File file, Class<T> clazz) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from file", e);
        }
    }

    /**
     * InputStream에서 객체 읽기
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from InputStream", e);
        }
    }

    // ===== 검증 =====

    /**
     * 유효한 JSON 문자열인지 확인
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * JSON 객체(Object)인지 확인
     */
    public static boolean isJsonObject(String json) {
        if (!isValidJson(json)) {
            return false;
        }
        try {
            return OBJECT_MAPPER.readTree(json).isObject();
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * JSON 배열(Array)인지 확인
     */
    public static boolean isJsonArray(String json) {
        if (!isValidJson(json)) {
            return false;
        }
        try {
            return OBJECT_MAPPER.readTree(json).isArray();
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    // ===== 변환 (객체 → 객체) =====

    /**
     * 객체를 다른 타입으로 변환 (JSON을 거쳐서)
     * 예: Map을 DTO로, DTO를 다른 DTO로
     */
    public static <T> T convertValue(Object obj, Class<T> toClass) {
        if (obj == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(obj, toClass);
    }

    /**
     * 객체를 다른 타입으로 변환 (TypeReference 사용)
     */
    public static <T> T convertValue(Object obj, TypeReference<T> toTypeRef) {
        if (obj == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(obj, toTypeRef);
    }

    /**
     * Map을 객체로 변환
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        return convertValue(map, clazz);
    }

    /**
     * 객체를 Map으로 변환
     */
    public static Map<String, Object> objectToMap(Object obj) {
        return convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    // ===== 병합 =====

    /**
     * 두 JSON 문자열을 병합 (target에 source 덮어쓰기)
     */
    public static String merge(String targetJson, String sourceJson) {
        try {
            var target = OBJECT_MAPPER.readTree(targetJson);
            var source = OBJECT_MAPPER.readTree(sourceJson);
            return OBJECT_MAPPER.writeValueAsString(
                    OBJECT_MAPPER.readerForUpdating(target).readValue(source)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge JSON", e);
        }
    }

    // ===== ObjectMapper 접근 =====

    /**
     * 기본 ObjectMapper 반환 (직접 사용 시)
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Pretty Print용 ObjectMapper 반환
     */
    public static ObjectMapper getPrettyObjectMapper() {
        return PRETTY_MAPPER;
    }

    /**
     * 커스텀 ObjectMapper 생성
     */
    public static ObjectMapper createCustomObjectMapper() {
        return createDefaultObjectMapper();
    }

    // ===== 유틸리티 =====

    /**
     * JSON 문자열을 압축 (공백 제거)
     */
    public static String minify(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    /**
     * JSON 문자열을 보기 좋게 포맷팅
     */
    public static String prettify(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return PRETTY_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    /**
     * JSON 객체 복사 (깊은 복사)
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        return convertValue(obj, clazz);
    }
}
