package com.eraf.gateway.common.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway 공통 설정
 * 각 기능 모듈은 이 클래스를 확장하여 사용
 */
@Data
public class GatewayProperties {

    /**
     * Gateway 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 스토리지 타입 (memory, jpa, redis)
     */
    private StoreType storeType = StoreType.MEMORY;

    /**
     * 전역 제외 패턴
     * 이 패턴에 매칭되는 경로는 모든 필터를 우회
     */
    private List<String> globalExcludePatterns = new ArrayList<>();

    /**
     * 스토리지 타입
     */
    public enum StoreType {
        MEMORY,
        JPA,
        REDIS
    }
}
