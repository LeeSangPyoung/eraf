package com.eraf.core.config.feature;

import com.eraf.core.config.FeatureToggle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureToggleTest {

    @Mock
    private FeatureFlagService featureFlagService;

    @Test
    @DisplayName("메모리 오버라이드가 DB보다 우선")
    void testMemoryOverridePriority() {
        FeatureToggle toggle = new FeatureToggle(featureFlagService);
        toggle.enable("feature");

        assertTrue(toggle.isEnabled("feature"));
        verify(featureFlagService, never()).isEnabled("feature");
    }

    @Test
    @DisplayName("메모리 오버라이드 없으면 FeatureFlagService 위임")
    void testDelegatesToService() {
        when(featureFlagService.isEnabled("db-feature")).thenReturn(true);

        FeatureToggle toggle = new FeatureToggle(featureFlagService);

        assertTrue(toggle.isEnabled("db-feature"));
        verify(featureFlagService).isEnabled("db-feature");
    }

    @Test
    @DisplayName("서비스 없는 메모리 전용 모드")
    void testMemoryOnlyMode() {
        FeatureToggle toggle = new FeatureToggle();

        assertFalse(toggle.isEnabled("feature"));

        toggle.enable("feature");
        assertTrue(toggle.isEnabled("feature"));

        toggle.disable("feature");
        assertFalse(toggle.isEnabled("feature"));
    }

    @Test
    @DisplayName("toggle 동작")
    void testToggle() {
        FeatureToggle toggle = new FeatureToggle();

        toggle.enable("feature");
        assertTrue(toggle.isEnabled("feature"));

        toggle.toggle("feature");
        assertFalse(toggle.isEnabled("feature"));

        toggle.toggle("feature");
        assertTrue(toggle.isEnabled("feature"));
    }

    @Test
    @DisplayName("set 동작")
    void testSet() {
        FeatureToggle toggle = new FeatureToggle();

        toggle.set("feature", true);
        assertTrue(toggle.isEnabled("feature"));

        toggle.set("feature", false);
        assertFalse(toggle.isEnabled("feature"));
    }

    @Test
    @DisplayName("getAll은 메모리 오버라이드만 반환")
    void testGetAll() {
        FeatureToggle toggle = new FeatureToggle();
        toggle.enable("a");
        toggle.disable("b");

        Map<String, Boolean> all = toggle.getAll();
        assertEquals(2, all.size());
        assertTrue(all.get("a"));
        assertFalse(all.get("b"));
    }

    @Test
    @DisplayName("clear 동작")
    void testClear() {
        FeatureToggle toggle = new FeatureToggle();
        toggle.enable("a");
        toggle.enable("b");

        toggle.clear();
        assertTrue(toggle.getAll().isEmpty());
    }

    @Test
    @DisplayName("clearOverride 후 서비스 폴백")
    void testClearOverride() {
        when(featureFlagService.isEnabled("feature")).thenReturn(true);

        FeatureToggle toggle = new FeatureToggle(featureFlagService);
        toggle.disable("feature"); // memory override = false

        assertFalse(toggle.isEnabled("feature")); // memory override

        toggle.clearOverride("feature"); // clear override

        assertTrue(toggle.isEnabled("feature")); // fallback to service = true
    }

    @Test
    @DisplayName("getAllIncludingService는 서비스 + 메모리 합산")
    void testGetAllIncludingService() {
        FeatureFlagEntity flag1 = new FeatureFlagEntity();
        flag1.setFlagKey("db-flag");
        flag1.setEnabled(true);

        FeatureFlagEntity flag2 = new FeatureFlagEntity();
        flag2.setFlagKey("override-flag");
        flag2.setEnabled(true);

        when(featureFlagService.findAll()).thenReturn(java.util.List.of(flag1, flag2));

        FeatureToggle toggle = new FeatureToggle(featureFlagService);
        toggle.disable("override-flag"); // override to false

        Map<String, Boolean> all = toggle.getAllIncludingService();
        assertTrue(all.get("db-flag")); // from service
        assertFalse(all.get("override-flag")); // memory override wins
    }
}
