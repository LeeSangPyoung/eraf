package com.eraf.starter.statemachine;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF StateMachine 설정
 */
@ConfigurationProperties(prefix = "eraf.statemachine")
public class ErafStateMachineProperties {

    /**
     * 상태 머신 활성화
     */
    private boolean enabled = true;

    /**
     * 상태 저장소 타입 (memory, redis, jdbc)
     */
    private StoreType storeType = StoreType.MEMORY;

    /**
     * 상태 TTL (Redis 사용 시)
     */
    private Duration stateTtl = Duration.ofDays(7);

    /**
     * 테이블 자동 생성 (JDBC 사용 시)
     */
    private boolean autoCreateTable = true;

    /**
     * 상태 변경 이벤트 발행
     */
    private boolean eventPublishingEnabled = true;

    public enum StoreType {
        MEMORY, REDIS, JDBC
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public Duration getStateTtl() {
        return stateTtl;
    }

    public void setStateTtl(Duration stateTtl) {
        this.stateTtl = stateTtl;
    }

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }

    public boolean isEventPublishingEnabled() {
        return eventPublishingEnabled;
    }

    public void setEventPublishingEnabled(boolean eventPublishingEnabled) {
        this.eventPublishingEnabled = eventPublishingEnabled;
    }
}
