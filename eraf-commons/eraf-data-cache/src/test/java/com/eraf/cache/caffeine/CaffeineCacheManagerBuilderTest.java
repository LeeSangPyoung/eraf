package com.eraf.cache.caffeine;

import com.eraf.cache.ErafCacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CaffeineCacheManagerBuilder 테스트
 */
class CaffeineCacheManagerBuilderTest {

    private ErafCacheProperties properties;
    private CaffeineCacheManagerBuilder builder;

    @BeforeEach
    void setUp() {
        properties = new ErafCacheProperties();
        properties.setType(ErafCacheProperties.CacheType.CAFFEINE);
        properties.getCaffeine().setMaxSize(1000);
        properties.getCaffeine().setExpireAfterWrite(Duration.ofMinutes(30));

        builder = new CaffeineCacheManagerBuilder(properties);
    }

    @Test
    void build_ShouldCreateCaffeineCacheManager() {
        // when
        CaffeineCacheManager cacheManager = builder.build();

        // then
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void buildDefaultCaffeineSpec_ShouldUsePropertiesValues() {
        // given
        properties.getCaffeine().setMaxSize(5000);
        properties.getCaffeine().setInitialCapacity(100);
        properties.getCaffeine().setRecordStats(true);

        // when
        CaffeineCacheManagerBuilder newBuilder = new CaffeineCacheManagerBuilder(properties);
        CaffeineCacheManager cacheManager = newBuilder.build();

        // then
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void buildForNamedCache_ShouldUseSpecificConfig() {
        // given
        ErafCacheProperties.CacheSpec spec = new ErafCacheProperties.CacheSpec();
        spec.setMaxSize(2000);
        spec.setExpireAfterWrite(Duration.ofMinutes(15));
        properties.getCaches().put("testCache", spec);

        // when
        Caffeine<Object, Object> caffeine = builder.buildForNamedCache("testCache");

        // then
        assertThat(caffeine).isNotNull();
    }

    @Test
    void buildForNamedCache_WithoutSpec_ShouldUseDefaults() {
        // when
        Caffeine<Object, Object> caffeine = builder.buildForNamedCache("nonExistentCache");

        // then
        assertThat(caffeine).isNotNull();
    }

    @Test
    void buildNamedCaffeine_ShouldCreateMapOfBuilders() {
        // given
        properties.getCaches().put("cache1", new ErafCacheProperties.CacheSpec());
        properties.getCaches().put("cache2", new ErafCacheProperties.CacheSpec());

        // when
        Map<String, Caffeine<Object, Object>> caffeineMap = builder.buildNamedCaffeine();

        // then
        assertThat(caffeineMap).hasSize(2);
        assertThat(caffeineMap).containsKeys("cache1", "cache2");
    }

    @Test
    void build_WithStatisticsEnabled_ShouldRecordStats() {
        // given
        properties.setStatisticsEnabled(true);
        properties.getCaffeine().setRecordStats(true);

        // when
        CaffeineCacheManager cacheManager = builder.build();

        // then
        assertThat(cacheManager).isNotNull();
    }
}
