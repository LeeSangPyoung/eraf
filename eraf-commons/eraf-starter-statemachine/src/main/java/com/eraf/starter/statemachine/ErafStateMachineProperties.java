package com.eraf.starter.statemachine;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
     * 상태 지속성 활성화
     */
    private boolean persistenceEnabled = false;

    /**
     * 상태 변경 이벤트 발행
     */
    private boolean eventPublishingEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }

    public boolean isEventPublishingEnabled() {
        return eventPublishingEnabled;
    }

    public void setEventPublishingEnabled(boolean eventPublishingEnabled) {
        this.eventPublishingEnabled = eventPublishingEnabled;
    }
}
