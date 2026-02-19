package com.eraf.featureflag;

import com.eraf.featureflag.feature.*;
import com.eraf.featureflag.feature.evaluator.FeatureFlagEvaluator;
import com.eraf.featureflag.feature.evaluator.SimpleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository repository;

    @Mock
    private FeatureFlagCacheManager cacheManager;

    private FeatureFlagProperties properties;
    private FeatureFlagService service;

    @BeforeEach
    void setUp() {
        properties = new FeatureFlagProperties();
        properties.getCache().setL1Ttl(Duration.ofSeconds(5));
        properties.getStatistics().setEnabled(false); // disable async stats for tests

        List<FeatureFlagEvaluator> evaluators = List.of(new SimpleEvaluator());
        service = new FeatureFlagService(repository, cacheManager, properties, evaluators);
    }

    @Test
    @DisplayName("L2 캐시 히트 시 DB 조회 안함")
    void testL2CacheHit() {
        when(cacheManager.getEnabled("test-feature")).thenReturn(true);

        boolean result = service.isEnabled("test-feature");

        assertTrue(result);
        verify(repository, never()).findByFlagKey(anyString());
    }

    @Test
    @DisplayName("L2 캐시 미스 시 DB 조회 후 캐시 저장")
    void testL2CacheMissAndDbHit() {
        when(cacheManager.getEnabled("test-feature")).thenReturn(null);

        FeatureFlagEntity entity = createFlag("test-feature", true, FeatureFlagEntity.FlagType.SIMPLE);
        when(repository.findByFlagKey("test-feature")).thenReturn(Optional.of(entity));

        boolean result = service.isEnabled("test-feature");

        assertTrue(result);
        verify(cacheManager).putEnabled("test-feature", true);
    }

    @Test
    @DisplayName("플래그 미존재 시 false 반환")
    void testFlagNotFound() {
        when(cacheManager.getEnabled("unknown")).thenReturn(null);
        when(repository.findByFlagKey("unknown")).thenReturn(Optional.empty());

        boolean result = service.isEnabled("unknown");

        assertFalse(result);
    }

    @Test
    @DisplayName("비활성 플래그 false 반환")
    void testDisabledFlag() {
        when(cacheManager.getEnabled("disabled-feature")).thenReturn(null);

        FeatureFlagEntity entity = createFlag("disabled-feature", false, FeatureFlagEntity.FlagType.SIMPLE);
        when(repository.findByFlagKey("disabled-feature")).thenReturn(Optional.of(entity));

        boolean result = service.isEnabled("disabled-feature");

        assertFalse(result);
    }

    @Test
    @DisplayName("예외 발생 시 false 반환 (fail closed)")
    void testExceptionReturnsDefault() {
        when(cacheManager.getEnabled("error-feature")).thenThrow(new RuntimeException("Redis error"));

        boolean result = service.isEnabled("error-feature");

        assertFalse(result);
    }

    @Test
    @DisplayName("캐시 삭제")
    void testEvictCache() {
        service.evictCache("test-feature");

        verify(cacheManager).evict("test-feature");
    }

    @Test
    @DisplayName("전체 캐시 삭제")
    void testEvictAllCaches() {
        service.evictAllCaches();

        verify(cacheManager).evictAll();
    }

    @Test
    @DisplayName("findByKey 위임")
    void testFindByKey() {
        FeatureFlagEntity entity = createFlag("test", true, FeatureFlagEntity.FlagType.SIMPLE);
        when(repository.findByFlagKey("test")).thenReturn(Optional.of(entity));

        Optional<FeatureFlagEntity> result = service.findByKey("test");

        assertTrue(result.isPresent());
        assertEquals("test", result.get().getFlagKey());
    }

    @Test
    @DisplayName("findAll 위임")
    void testFindAll() {
        FeatureFlagEntity flag1 = createFlag("flag1", true, FeatureFlagEntity.FlagType.SIMPLE);
        FeatureFlagEntity flag2 = createFlag("flag2", false, FeatureFlagEntity.FlagType.SIMPLE);
        when(repository.findAll()).thenReturn(List.of(flag1, flag2));

        List<FeatureFlagEntity> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("컨텍스트 없는 isEnabled는 null context로 호출")
    void testIsEnabledWithoutContext() {
        when(cacheManager.getEnabled("simple")).thenReturn(null);
        FeatureFlagEntity entity = createFlag("simple", true, FeatureFlagEntity.FlagType.SIMPLE);
        when(repository.findByFlagKey("simple")).thenReturn(Optional.of(entity));

        boolean result = service.isEnabled("simple");

        assertTrue(result);
    }

    private FeatureFlagEntity createFlag(String key, boolean enabled, FeatureFlagEntity.FlagType type) {
        FeatureFlagEntity entity = new FeatureFlagEntity();
        entity.setId(1L);
        entity.setFlagKey(key);
        entity.setName(key);
        entity.setEnabled(enabled);
        entity.setFlagType(type);
        entity.setCheckCount(0L);
        return entity;
    }
}
