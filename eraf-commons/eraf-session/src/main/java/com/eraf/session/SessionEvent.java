package com.eraf.session;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 세션 라이프사이클 이벤트
 * Spring ApplicationEvent를 확장하여 ApplicationEventPublisher로 발행 가능
 */
public class SessionEvent extends ApplicationEvent {

    public enum Type {
        CREATED, ACCESSED, EXPIRED, DESTROYED
    }

    private final Type type;
    private final String sessionId;
    private final String userId;
    private final LocalDateTime eventTime;

    public SessionEvent(Object source, Type type, String sessionId, String userId) {
        super(source);
        this.type = type;
        this.sessionId = sessionId;
        this.userId = userId;
        this.eventTime = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "SessionEvent{" +
                "type=" + type +
                ", sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
