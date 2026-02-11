package com.eraf.openapi.features.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Route Refresh Service
 * 라우트 동적 리로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteRefreshService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 라우트 갱신 이벤트 발행
     * Admin API에서 라우트 변경 시 호출하여 즉시 반영
     */
    public void refreshRoutes() {
        log.info("Publishing route refresh event");
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("Route refresh event published successfully");
    }
}
