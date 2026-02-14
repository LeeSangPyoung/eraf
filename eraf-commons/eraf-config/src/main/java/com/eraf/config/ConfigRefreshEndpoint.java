package com.eraf.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Map;
import java.util.Set;

/**
 * 설정 갱신 Actuator 엔드포인트
 *
 * /actuator/eraf-config 에서 접근 가능합니다.
 */
@Endpoint(id = "eraf-config")
public class ConfigRefreshEndpoint {

    private final DynamicConfigRefreshService refreshService;

    public ConfigRefreshEndpoint(DynamicConfigRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    /**
     * 현재 오버라이드 조회
     */
    @ReadOperation
    public Map<String, String> getOverrides() {
        return refreshService.getAllOverrides();
    }

    /**
     * 설정 갱신 및 RefreshScope 리프레시
     */
    @WriteOperation
    public Map<String, Object> refresh() {
        Set<String> refreshed = refreshService.refreshScope();
        return Map.of(
                "refreshed", refreshed,
                "overrides", refreshService.getAllOverrides()
        );
    }
}
