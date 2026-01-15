package com.eraf.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;

/**
 * JSON 변환 유틸리티 (Jackson 래핑)
 */
public final class JsonConverter {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonConverter() {
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 변환 실패", e);
        }
    }

    /**
     * 객체를 JSON 문자열로 변환 (pretty print)
     */
    public static String toJsonPretty(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 변환 실패", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 파싱 실패", e);
        }
    }

    /**
     * JSON 문자열을 제네릭 타입으로 변환
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 파싱 실패", e);
        }
    }

    /**
     * JSON 문자열을 List로 변환
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elementClass) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 파싱 실패", e);
        }
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new ConversionException("JSON 파싱 실패", e);
        }
    }

    /**
     * 객체를 다른 타입으로 변환
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return MAPPER.convertValue(source, targetClass);
    }

    /**
     * 객체를 제네릭 타입으로 변환
     */
    public static <T> T convert(Object source, TypeReference<T> typeReference) {
        if (source == null) {
            return null;
        }
        return MAPPER.convertValue(source, typeReference);
    }

    /**
     * ObjectMapper 인스턴스 반환 (커스터마이징용)
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
