package com.eraf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동적 설정 갱신 서비스
 *
 * 런타임에 설정값을 변경하고 리스너에게 알림을 보냅니다.
 * Spring Cloud Config가 없는 환경에서도 동적 설정 변경을 지원합니다.
 */
public class DynamicConfigRefreshService {

    private static final Logger log = LoggerFactory.getLogger(DynamicConfigRefreshService.class);

    private final Map<String, String> overrides = new ConcurrentHashMap<>();
    private final Map<String, ConfigChangeListener> listeners = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    public DynamicConfigRefreshService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 설정값 오버라이드
     */
    public void setProperty(String key, String value) {
        String previous = overrides.put(key, value);
        log.info("Config override: {}={} (was={})", key, value, previous);
        notifyListeners(key, previous, value);
    }

    /**
     * 오버라이드된 설정값 조회
     */
    public String getProperty(String key) {
        return overrides.get(key);
    }

    /**
     * 오버라이드된 설정값 조회 (기본값 포함)
     */
    public String getProperty(String key, String defaultValue) {
        return overrides.getOrDefault(key, defaultValue);
    }

    /**
     * 오버라이드 해제
     */
    public void removeProperty(String key) {
        String previous = overrides.remove(key);
        if (previous != null) {
            log.info("Config override removed: {} (was={})", key, previous);
            notifyListeners(key, previous, null);
        }
    }

    /**
     * 전체 오버라이드 조회
     */
    public Map<String, String> getAllOverrides() {
        return Map.copyOf(overrides);
    }

    /**
     * 전체 오버라이드 초기화
     */
    public void clearOverrides() {
        overrides.clear();
        log.info("All config overrides cleared");
    }

    /**
     * 설정 변경 리스너 등록
     */
    public void addListener(String key, ConfigChangeListener listener) {
        listeners.put(key, listener);
    }

    /**
     * 설정 변경 리스너 해제
     */
    public void removeListener(String key) {
        listeners.remove(key);
    }

    /**
     * @RefreshScope 빈 갱신 (Spring Cloud Context가 있는 경우)
     */
    public Set<String> refreshScope() {
        try {
            Object refreshScope = applicationContext.getBean("refreshScope");
            var refreshMethod = refreshScope.getClass().getMethod("refreshAll");
            refreshMethod.invoke(refreshScope);
            log.info("RefreshScope beans refreshed");
            return Set.of("refreshScope");
        } catch (Exception e) {
            log.debug("RefreshScope not available: {}", e.getMessage());
            return Set.of();
        }
    }

    private void notifyListeners(String key, String oldValue, String newValue) {
        ConfigChangeListener listener = listeners.get(key);
        if (listener != null) {
            try {
                listener.onConfigChange(key, oldValue, newValue);
            } catch (Exception e) {
                log.error("Config change listener error for key: {}", key, e);
            }
        }
    }

    /**
     * 설정 변경 리스너 인터페이스
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        void onConfigChange(String key, String oldValue, String newValue);
    }
}
