package com.eraf.websocket;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * WebSocket Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.websocket.auth.enabled", havingValue = "true")
    public WebSocketAuthInterceptor webSocketAuthInterceptor(
            WebSocketProperties properties,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            WebSocketTokenValidator tokenValidator) {
        WebSocketAuthInterceptor interceptor = new WebSocketAuthInterceptor(
                properties.getAuth().getTokenParam());
        if (tokenValidator != null) {
            interceptor.setTokenValidator(tokenValidator);
        }
        return interceptor;
    }
}
