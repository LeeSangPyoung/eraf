package com.eraf.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 세션 이벤트 퍼블리셔
 * Spring ApplicationEventPublisher를 통해 세션 라이프사이클 이벤트 발행
 */
public class SessionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SessionEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public SessionEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 세션 생성 이벤트 발행
     */
    public void publishSessionCreated(String sessionId, String userId) {
        SessionEvent event = new SessionEvent(this, SessionEvent.Type.CREATED, sessionId, userId);
        log.debug("Publishing session created event - sessionId: {}, userId: {}", sessionId, userId);
        eventPublisher.publishEvent(event);
    }

    /**
     * 세션 접근 이벤트 발행
     */
    public void publishSessionAccessed(String sessionId, String userId) {
        SessionEvent event = new SessionEvent(this, SessionEvent.Type.ACCESSED, sessionId, userId);
        log.trace("Publishing session accessed event - sessionId: {}, userId: {}", sessionId, userId);
        eventPublisher.publishEvent(event);
    }

    /**
     * 세션 만료 이벤트 발행
     */
    public void publishSessionExpired(String sessionId, String userId) {
        SessionEvent event = new SessionEvent(this, SessionEvent.Type.EXPIRED, sessionId, userId);
        log.debug("Publishing session expired event - sessionId: {}, userId: {}", sessionId, userId);
        eventPublisher.publishEvent(event);
    }

    /**
     * 세션 종료 이벤트 발행
     */
    public void publishSessionDestroyed(String sessionId, String userId) {
        SessionEvent event = new SessionEvent(this, SessionEvent.Type.DESTROYED, sessionId, userId);
        log.debug("Publishing session destroyed event - sessionId: {}, userId: {}", sessionId, userId);
        eventPublisher.publishEvent(event);
    }
}
