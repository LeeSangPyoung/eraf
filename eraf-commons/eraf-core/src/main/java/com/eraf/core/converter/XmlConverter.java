package com.eraf.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * XML 변환 유틸리티 (Jackson XML 래핑)
 */
public final class XmlConverter {

    private static final XmlMapper MAPPER;

    static {
        MAPPER = new XmlMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private XmlConverter() {
    }

    /**
     * 객체를 XML 문자열로 변환
     */
    public static String toXml(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("XML 변환 실패", e);
        }
    }

    /**
     * 객체를 XML 문자열로 변환 (pretty print)
     */
    public static String toXmlPretty(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("XML 변환 실패", e);
        }
    }

    /**
     * XML 문자열을 객체로 변환
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(xml, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("XML 파싱 실패", e);
        }
    }

    /**
     * XmlMapper 인스턴스 반환 (커스터마이징용)
     */
    public static XmlMapper getMapper() {
        return MAPPER;
    }
}
