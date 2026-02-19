package com.eraf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.cloud.endpoint.event.RefreshEvent;

/**
 * Config Refresh Listener
 * 설정 변경 이벤트를 수신하고 @RefreshScope 빈을 갱신합니다.
 */
public class ConfigRefreshListener implements ApplicationListener<RefreshEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConfigRefreshListener.class);

    @Override
    public void onApplicationEvent(RefreshEvent event) {
        log.info("Configuration refresh event received");
        log.info("Event source: {}", event.getSource());
    }
}
