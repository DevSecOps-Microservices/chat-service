package com.gestion.incidents.chatservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les messages envoyés au client
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés au serveur
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket avec SockJS fallback
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();

        // Endpoint WebSocket natif (sans SockJS)
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*");
    }
}
