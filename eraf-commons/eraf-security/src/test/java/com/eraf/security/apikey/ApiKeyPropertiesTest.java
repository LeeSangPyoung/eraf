package com.eraf.security.apikey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiKeyProperties 테스트
 */
class ApiKeyPropertiesTest {

    private ApiKeyProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ApiKeyProperties();
    }

    @Test
    @DisplayName("기본 헤더명은 X-API-KEY")
    void shouldHaveDefaultHeaderName() {
        assertEquals("X-API-KEY", properties.getHeaderName());
    }

    @Test
    @DisplayName("커스텀 헤더명 설정")
    void shouldSetCustomHeaderName() {
        properties.setHeaderName("Authorization");

        assertEquals("Authorization", properties.getHeaderName());
    }

    @Test
    @DisplayName("API Key 엔트리 추가")
    void shouldAddApiKeyEntries() {
        Map<String, ApiKeyProperties.ApiKeyEntry> keys = new HashMap<>();

        ApiKeyProperties.ApiKeyEntry entry1 = new ApiKeyProperties.ApiKeyEntry();
        entry1.setKey("api-key-123");
        entry1.setName("Service A");
        entry1.setRoles(Arrays.asList("ROLE_API", "ROLE_READ"));

        keys.put("service-a", entry1);
        properties.setKeys(keys);

        assertEquals(1, properties.getKeys().size());
        assertEquals("api-key-123", properties.getKeys().get("service-a").getKey());
    }

    @Test
    @DisplayName("ApiKeyEntry 필드 설정")
    void shouldSetApiKeyEntryFields() {
        ApiKeyProperties.ApiKeyEntry entry = new ApiKeyProperties.ApiKeyEntry();

        entry.setKey("secret-key-456");
        entry.setName("Payment Service");
        entry.setRoles(Arrays.asList("ROLE_PAYMENT", "ROLE_ADMIN"));
        entry.setAllowedIps(Arrays.asList("10.0.0.1", "10.0.0.2"));
        entry.setAllowedPatterns(Arrays.asList("/api/payments/**", "/api/refunds/**"));

        assertEquals("secret-key-456", entry.getKey());
        assertEquals("Payment Service", entry.getName());
        assertEquals(2, entry.getRoles().size());
        assertEquals(2, entry.getAllowedIps().size());
        assertEquals(2, entry.getAllowedPatterns().size());
    }

    @Test
    @DisplayName("여러 API Key 설정")
    void shouldSupportMultipleApiKeys() {
        Map<String, ApiKeyProperties.ApiKeyEntry> keys = new HashMap<>();

        ApiKeyProperties.ApiKeyEntry entry1 = new ApiKeyProperties.ApiKeyEntry();
        entry1.setKey("key-1");
        entry1.setName("Service 1");

        ApiKeyProperties.ApiKeyEntry entry2 = new ApiKeyProperties.ApiKeyEntry();
        entry2.setKey("key-2");
        entry2.setName("Service 2");

        ApiKeyProperties.ApiKeyEntry entry3 = new ApiKeyProperties.ApiKeyEntry();
        entry3.setKey("key-3");
        entry3.setName("Service 3");

        keys.put("svc1", entry1);
        keys.put("svc2", entry2);
        keys.put("svc3", entry3);

        properties.setKeys(keys);

        assertEquals(3, properties.getKeys().size());
        assertEquals("Service 1", properties.getKeys().get("svc1").getName());
        assertEquals("Service 2", properties.getKeys().get("svc2").getName());
        assertEquals("Service 3", properties.getKeys().get("svc3").getName());
    }

    @Test
    @DisplayName("빈 AllowedIps는 모든 IP 허용 의미")
    void shouldAllowAllIpsWhenEmpty() {
        ApiKeyProperties.ApiKeyEntry entry = new ApiKeyProperties.ApiKeyEntry();
        entry.setKey("key-123");
        entry.setAllowedIps(List.of());

        assertTrue(entry.getAllowedIps().isEmpty());
    }

    @Test
    @DisplayName("빈 AllowedPatterns는 모든 패턴 허용 의미")
    void shouldAllowAllPatternsWhenEmpty() {
        ApiKeyProperties.ApiKeyEntry entry = new ApiKeyProperties.ApiKeyEntry();
        entry.setKey("key-123");
        entry.setAllowedPatterns(List.of());

        assertTrue(entry.getAllowedPatterns().isEmpty());
    }
}
