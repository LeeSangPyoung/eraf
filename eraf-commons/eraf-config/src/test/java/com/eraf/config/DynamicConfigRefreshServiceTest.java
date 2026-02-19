package com.eraf.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicConfigRefreshService 단위 테스트")
class DynamicConfigRefreshServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    private DynamicConfigRefreshService service;

    @BeforeEach
    void setUp() {
        service = new DynamicConfigRefreshService(applicationContext);
    }

    // ──────────────────────────────────────────────
    // setProperty / getProperty
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("setProperty - 설정값을 저장한다")
    void setProperty_storesValue() {
        // when
        service.setProperty("app.timeout", "3000");

        // then
        assertThat(service.getProperty("app.timeout")).isEqualTo("3000");
    }

    @Test
    @DisplayName("getProperty - 저장된 설정값을 반환한다")
    void getProperty_returnsStoredValue() {
        // given
        service.setProperty("db.url", "jdbc:h2:mem:test");

        // when
        String value = service.getProperty("db.url");

        // then
        assertThat(value).isEqualTo("jdbc:h2:mem:test");
    }

    @Test
    @DisplayName("getProperty(기본값) - 키가 없을 때 기본값을 반환한다")
    void getProperty_withDefault_returnsDefaultWhenKeyMissing() {
        // when
        String value = service.getProperty("non.existent.key", "fallback");

        // then
        assertThat(value).isEqualTo("fallback");
    }

    // ──────────────────────────────────────────────
    // removeProperty
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("removeProperty - 저장된 설정값을 제거한다")
    void removeProperty_removesValue() {
        // given
        service.setProperty("cache.ttl", "60");

        // when
        service.removeProperty("cache.ttl");

        // then
        assertThat(service.getProperty("cache.ttl")).isNull();
    }

    @Test
    @DisplayName("removeProperty - 존재하지 않는 키를 제거해도 예외가 발생하지 않는다")
    void removeProperty_nonExistentKey_doesNothing() {
        // when / then
        assertThatCode(() -> service.removeProperty("no.such.key"))
                .doesNotThrowAnyException();
    }

    // ──────────────────────────────────────────────
    // getAllOverrides / clearOverrides
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("getAllOverrides - 변경 불가능한 복사본을 반환한다")
    void getAllOverrides_returnsUnmodifiableCopy() {
        // given
        service.setProperty("a", "1");
        service.setProperty("b", "2");

        // when
        Map<String, String> overrides = service.getAllOverrides();

        // then
        assertThat(overrides).containsExactlyInAnyOrderEntriesOf(Map.of("a", "1", "b", "2"));

        // 반환된 맵은 수정 불가
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> overrides.put("c", "3"));
    }

    @Test
    @DisplayName("clearOverrides - 모든 오버라이드를 초기화한다")
    void clearOverrides_clearsAll() {
        // given
        service.setProperty("x", "10");
        service.setProperty("y", "20");

        // when
        service.clearOverrides();

        // then
        assertThat(service.getAllOverrides()).isEmpty();
    }

    // ──────────────────────────────────────────────
    // Listener
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("리스너 - setProperty 호출 시 리스너에게 알림이 전달된다")
    void listener_isNotifiedOnSet() {
        // given
        DynamicConfigRefreshService.ConfigChangeListener listener = mock(
                DynamicConfigRefreshService.ConfigChangeListener.class);
        service.addListener("feature.flag", listener);

        // when
        service.setProperty("feature.flag", "true");

        // then
        verify(listener).onConfigChange("feature.flag", null, "true");
    }

    @Test
    @DisplayName("리스너 - removeProperty 호출 시 리스너에게 알림이 전달된다")
    void listener_isNotifiedOnRemove() {
        // given
        DynamicConfigRefreshService.ConfigChangeListener listener = mock(
                DynamicConfigRefreshService.ConfigChangeListener.class);
        service.addListener("feature.flag", listener);
        service.setProperty("feature.flag", "true");
        reset(listener);

        // when
        service.removeProperty("feature.flag");

        // then
        verify(listener).onConfigChange("feature.flag", "true", null);
    }

    @Test
    @DisplayName("리스너 - 리스너에서 예외가 발생해도 전파되지 않는다")
    void listener_exceptionIsCaught() {
        // given
        DynamicConfigRefreshService.ConfigChangeListener listener = mock(
                DynamicConfigRefreshService.ConfigChangeListener.class);
        doThrow(new RuntimeException("listener boom"))
                .when(listener).onConfigChange(anyString(), any(), anyString());
        service.addListener("danger.key", listener);

        // when / then
        assertThatCode(() -> service.setProperty("danger.key", "value"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("removeListener - 리스너를 해제하면 더 이상 알림이 전달되지 않는다")
    void removeListener_stopsNotifications() {
        // given
        DynamicConfigRefreshService.ConfigChangeListener listener = mock(
                DynamicConfigRefreshService.ConfigChangeListener.class);
        service.addListener("key1", listener);
        service.removeListener("key1");

        // when
        service.setProperty("key1", "val");

        // then
        verifyNoInteractions(listener);
    }

    // ──────────────────────────────────────────────
    // refreshScope
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("refreshScope - RefreshScope 빈이 없으면 빈 Set을 반환한다")
    void refreshScope_returnsEmptyWhenNoRefreshScopeBean() {
        // given
        when(applicationContext.getBean("refreshScope"))
                .thenThrow(new org.springframework.beans.factory.NoSuchBeanDefinitionException("refreshScope"));

        // when
        Set<String> result = service.refreshScope();

        // then
        assertThat(result).isEmpty();
    }
}
