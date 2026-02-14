package com.eraf.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ErafCachePropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // Then
        assertThat(properties.getType()).isEqualTo(ErafCacheProperties.CacheType.SIMPLE);
        assertThat(properties.getDefaultTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(properties.getKeyPrefix()).isEqualTo("eraf:cache:");
        assertThat(properties.isStatisticsEnabled()).isFalse();
    }

    @Test
    @DisplayName("값 설정")
    void testSetValues() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // When
        properties.setType(ErafCacheProperties.CacheType.CAFFEINE);
        properties.setDefaultTtl(Duration.ofHours(1));
        properties.setKeyPrefix("myapp:cache:");
        properties.setStatisticsEnabled(true);

        // Then
        assertThat(properties.getType()).isEqualTo(ErafCacheProperties.CacheType.CAFFEINE);
        assertThat(properties.getDefaultTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.getKeyPrefix()).isEqualTo("myapp:cache:");
        assertThat(properties.isStatisticsEnabled()).isTrue();
    }

    @Test
    @DisplayName("Caffeine 설정 확인")
    void testCaffeineSettings() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // When
        properties.getCaffeine().setMaxSize(5000);
        properties.getCaffeine().setExpireAfterWrite(Duration.ofMinutes(15));
        properties.getCaffeine().setRecordStats(true);

        // Then
        assertThat(properties.getCaffeine().getMaxSize()).isEqualTo(5000);
        assertThat(properties.getCaffeine().getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(15));
        assertThat(properties.getCaffeine().isRecordStats()).isTrue();
    }

    @Test
    @DisplayName("Named Cache 설정")
    void testNamedCacheSpec() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();
        ErafCacheProperties.CacheSpec spec = new ErafCacheProperties.CacheSpec();

        // When
        spec.setTtl(Duration.ofMinutes(10));
        spec.setMaxSize(2000);
        properties.getCaches().put("userCache", spec);

        // Then
        assertThat(properties.getCaches()).hasSize(1);
        assertThat(properties.getCaches().get("userCache").getTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(properties.getCaches().get("userCache").getMaxSize()).isEqualTo(2000);
    }

    @Test
    @DisplayName("Redis 설정 확인")
    void testRedisSettings() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // When
        properties.getRedis().setEnableNullValues(false);
        properties.getRedis().setUseKeyPrefix(true);
        properties.getRedis().setTtl(Duration.ofHours(2));

        // Then
        assertThat(properties.getRedis().isEnableNullValues()).isFalse();
        assertThat(properties.getRedis().isUseKeyPrefix()).isTrue();
        assertThat(properties.getRedis().getTtl()).isEqualTo(Duration.ofHours(2));
    }
}
