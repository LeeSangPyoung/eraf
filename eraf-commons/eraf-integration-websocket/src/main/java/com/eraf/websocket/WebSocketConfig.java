package com.eraf.websocket;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket STOMP 설정
 */
@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty(name = "eraf.websocket.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketProperties properties;
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketProperties properties,
                           @org.springframework.beans.factory.annotation.Autowired(required = false)
                           WebSocketAuthInterceptor authInterceptor) {
        this.properties = properties;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(properties.getBrokerPrefixes());
        config.setApplicationDestinationPrefixes(properties.getAppDestinationPrefix());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        StompWebSocketEndpointRegistration endpoint = registry.addEndpoint(properties.getEndpoint())
            .setAllowedOriginPatterns(properties.getAllowedOrigins());

        if (authInterceptor != null) {
            endpoint.addInterceptors(authInterceptor);
        }

        endpoint.withSockJS();
    }
}
