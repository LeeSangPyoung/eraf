package com.eraf.core.config.feature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class FeatureFlagPropertiesTest {

    @Test
    @DisplayName("기본 설정값 확인")
    void testDefaultValues() {
        FeatureFlagProperties properties = new FeatureFlagProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.isAdminEnabled());
        assertTrue(properties.getDefaults().isEmpty());
    }

    @Test
    @DisplayName("캐시 기본값")
    void testCacheDefaults() {
        FeatureFlagProperties.Cache cache = new FeatureFlagProperties.Cache();

        assertEquals(Duration.ofSeconds(5), cache.getL1Ttl());
        assertEquals(Duration.ofSeconds(60), cache.getL2Ttl());
        assertTrue(cache.isRedisEnabled());
        assertEquals("eraf:feature-flag:", cache.getKeyPrefix());
    }

    @Test
    @DisplayName("통계 기본값")
    void testStatisticsDefaults() {
        FeatureFlagProperties.Statistics stats = new FeatureFlagProperties.Statistics();

        assertTrue(stats.isEnabled());
        assertTrue(stats.isAsync());
        assertEquals(100, stats.getBatchSize());
        assertEquals(5000, stats.getFlushIntervalMs());
    }

    @Test
    @DisplayName("기본 플래그 설정")
    void testDefaultFlag() {
        FeatureFlagProperties.DefaultFlag flag = new FeatureFlagProperties.DefaultFlag();
        flag.setKey("test-flag");
        flag.setName("Test Flag");
        flag.setDescription("A test flag");
        flag.setEnabled(true);
        flag.setType(FeatureFlagEntity.FlagType.PERCENTAGE);
        flag.setTargetingRules("{\"percentage\": 50}");

        assertEquals("test-flag", flag.getKey());
        assertEquals("Test Flag", flag.getName());
        assertEquals("A test flag", flag.getDescription());
        assertTrue(flag.getEnabled());
        assertEquals(FeatureFlagEntity.FlagType.PERCENTAGE, flag.getType());
        assertEquals("{\"percentage\": 50}", flag.getTargetingRules());
    }

    @Test
    @DisplayName("캐시 TTL 설정")
    void testCacheTtlConfiguration() {
        FeatureFlagProperties.Cache cache = new FeatureFlagProperties.Cache();
        cache.setL1Ttl(Duration.ofSeconds(10));
        cache.setL2Ttl(Duration.ofMinutes(5));
        cache.setRedisEnabled(false);
        cache.setKeyPrefix("custom:");

        assertEquals(Duration.ofSeconds(10), cache.getL1Ttl());
        assertEquals(Duration.ofMinutes(5), cache.getL2Ttl());
        assertFalse(cache.isRedisEnabled());
        assertEquals("custom:", cache.getKeyPrefix());
    }

    @Test
    @DisplayName("통계 비활성화")
    void testDisableStatistics() {
        FeatureFlagProperties.Statistics stats = new FeatureFlagProperties.Statistics();
        stats.setEnabled(false);
        stats.setAsync(false);

        assertFalse(stats.isEnabled());
        assertFalse(stats.isAsync());
    }
}
