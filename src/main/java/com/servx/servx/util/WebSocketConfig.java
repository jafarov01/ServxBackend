package com.servx.servx.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Import necessary classes (org.springframework.context.annotation.Configuration, org.springframework.messaging.simp.config...)
// Make sure AuthChannelInterceptor is also imported

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor // Use Lombok for injecting the interceptor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Inject your custom interceptor
    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        // Keep SockJS commented out for websocat testing
    }

    // *** ADD THIS METHOD ***
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add your interceptor to the channel processing incoming messages from clients
        registration.interceptors(authChannelInterceptor);
    }
}