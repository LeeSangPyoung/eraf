package com.eraf.featureflag;

import com.eraf.featureflag.feature.*;
import com.eraf.featureflag.feature.dto.FeatureFlagRequest;
import com.eraf.featureflag.feature.dto.FeatureFlagResponse;
import com.eraf.featureflag.feature.dto.FeatureFlagStats;
import com.eraf.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagAdminServiceTest {

    @Mock
    private FeatureFlagRepository repository;

    @Mock
    private FeatureFlagService service;

    private FeatureFlagMapper mapper;
    private FeatureFlagAdminService adminService;

    @BeforeEach
    void setUp() {
        mapper = new FeatureFlagMapper();
        adminService = new FeatureFlagAdminService(repository, service, mapper);
    }

    @Test
    @DisplayName("플래그 생성")
    void testCreate() {
        FeatureFlagRequest request = new FeatureFlagRequest(
                "new-feature", "New Feature", "desc", true, FeatureFlagEntity.FlagType.SIMPLE);

        when(repository.existsByFlagKey("new-feature")).thenReturn(false);
        when(repository.save(any(FeatureFlagEntity.class))).thenAnswer(invocation -> {
            FeatureFlagEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        FeatureFlagResponse response = adminService.create(request);

        assertNotNull(response);
        assertEquals("new-feature", response.getFlagKey());
        assertTrue(response.getEnabled());
        verify(service).evictCache("new-feature");
    }

    @Test
    @DisplayName("중복 키 생성 시 예외")
    void testCreateDuplicate() {
        FeatureFlagRequest request = new FeatureFlagRequest(
                "existing", "Existing", null, true, FeatureFlagEntity.FlagType.SIMPLE);

        when(repository.existsByFlagKey("existing")).thenReturn(true);

        assertThrows(BusinessException.class, () -> adminService.create(request));
    }

    @Test
    @DisplayName("플래그 수정")
    void testUpdate() {
        FeatureFlagEntity existing = createEntity(1L, "feature", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(FeatureFlagEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeatureFlagRequest request = new FeatureFlagRequest(
                "feature", "Updated Name", "updated desc", false, FeatureFlagEntity.FlagType.SIMPLE);

        FeatureFlagResponse response = adminService.update(1L, request);

        assertEquals("Updated Name", response.getName());
        assertFalse(response.getEnabled());
        verify(service).evictCache("feature");
    }

    @Test
    @DisplayName("존재하지 않는 플래그 수정 시 예외")
    void testUpdateNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        FeatureFlagRequest request = new FeatureFlagRequest(
                "unknown", "Unknown", null, true, FeatureFlagEntity.FlagType.SIMPLE);

        assertThrows(BusinessException.class, () -> adminService.update(99L, request));
    }

    @Test
    @DisplayName("플래그 삭제")
    void testDelete() {
        FeatureFlagEntity entity = createEntity(1L, "to-delete", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        adminService.delete(1L);

        verify(repository).deleteById(1L);
        verify(service).evictCache("to-delete");
    }

    @Test
    @DisplayName("플래그 토글")
    void testToggle() {
        FeatureFlagEntity entity = createEntity(1L, "toggle-me", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(FeatureFlagEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeatureFlagResponse response = adminService.toggle(1L);

        assertFalse(response.getEnabled()); // toggled from true to false
        verify(service).evictCache("toggle-me");
    }

    @Test
    @DisplayName("ID로 조회")
    void testGetById() {
        FeatureFlagEntity entity = createEntity(1L, "feature", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        FeatureFlagResponse response = adminService.getById(1L);

        assertEquals("feature", response.getFlagKey());
    }

    @Test
    @DisplayName("키로 조회")
    void testGetByKey() {
        FeatureFlagEntity entity = createEntity(1L, "feature", true);
        when(repository.findByFlagKey("feature")).thenReturn(Optional.of(entity));

        FeatureFlagResponse response = adminService.getByKey("feature");

        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("전체 조회")
    void testGetAll() {
        when(repository.findAll()).thenReturn(List.of(
                createEntity(1L, "flag1", true),
                createEntity(2L, "flag2", false)));

        List<FeatureFlagResponse> responses = adminService.getAll();

        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("통계 조회")
    void testGetStats() {
        when(repository.count()).thenReturn(10L);
        when(repository.countByEnabledTrue()).thenReturn(7L);
        when(repository.countByFlagType(FeatureFlagEntity.FlagType.SIMPLE)).thenReturn(5L);
        when(repository.countByFlagType(FeatureFlagEntity.FlagType.USER_BASED)).thenReturn(2L);
        when(repository.countByFlagType(FeatureFlagEntity.FlagType.PERCENTAGE)).thenReturn(1L);
        when(repository.countByFlagType(FeatureFlagEntity.FlagType.TIME_WINDOW)).thenReturn(1L);
        when(repository.countByFlagType(FeatureFlagEntity.FlagType.CUSTOM)).thenReturn(1L);

        FeatureFlagEntity flag1 = createEntity(1L, "f1", true);
        flag1.setCheckCount(100L);
        FeatureFlagEntity flag2 = createEntity(2L, "f2", true);
        flag2.setCheckCount(50L);
        when(repository.findAll()).thenReturn(List.of(flag1, flag2));

        FeatureFlagStats stats = adminService.getStats();

        assertEquals(10L, stats.getTotalCount());
        assertEquals(7L, stats.getEnabledCount());
        assertEquals(3L, stats.getDisabledCount());
        assertEquals(5L, stats.getSimpleCount());
        assertEquals(150L, stats.getTotalChecks());
    }

    @Test
    @DisplayName("캐시 리로드")
    void testReloadCache() {
        adminService.reloadCache();

        verify(service).evictAllCaches();
    }

    @Test
    @DisplayName("평가 테스트")
    void testEvaluate() {
        when(service.isEnabled("test-flag", null)).thenReturn(true);

        FeatureFlagEntity entity = createEntity(1L, "test-flag", true);
        when(repository.findByFlagKey("test-flag")).thenReturn(Optional.of(entity));

        Map<String, Object> result = adminService.evaluate("test-flag", null);

        assertTrue((Boolean) result.get("enabled"));
        assertTrue((Boolean) result.get("exists"));
    }

    @Test
    @DisplayName("잘못된 타겟팅 규칙 시 예외")
    void testInvalidTargetingRules() {
        FeatureFlagRequest request = new FeatureFlagRequest(
                "bad-rules", "Bad Rules", null, true, FeatureFlagEntity.FlagType.PERCENTAGE);
        request.setTargetingRules("not-json");

        when(repository.existsByFlagKey("bad-rules")).thenReturn(false);

        assertThrows(BusinessException.class, () -> adminService.create(request));
    }

    private FeatureFlagEntity createEntity(Long id, String flagKey, boolean enabled) {
        FeatureFlagEntity entity = new FeatureFlagEntity();
        entity.setId(id);
        entity.setFlagKey(flagKey);
        entity.setName(flagKey);
        entity.setEnabled(enabled);
        entity.setFlagType(FeatureFlagEntity.FlagType.SIMPLE);
        entity.setCheckCount(0L);
        return entity;
    }
}
