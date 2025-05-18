package com.example.chatservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final WebSocketAuthenticationHandler webSocketAuthenticationHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        logger.info("### STOMP endpoints registered: /ws ###");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthenticationHandler);
        
        // Add custom interceptor for logging
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    String sessionId = accessor.getSessionId();
                    
                    if (command != null) {
                        switch (command) {
                            case CONNECT:
                                logger.info("### STOMP CONNECT: Session ID={} ###", sessionId);
                                break;
                            case SUBSCRIBE:
                                String destination = accessor.getDestination();
                                logger.info("### STOMP SUBSCRIBE: Session ID={}, Destination={} ###", 
                                    sessionId, destination);
                                break;
                            case SEND:
                                String sendDestination = accessor.getDestination();
                                logger.info("### STOMP SEND: Session ID={}, Destination={} ###", 
                                    sessionId, sendDestination);
                                break;
                            case DISCONNECT:
                                logger.info("### STOMP DISCONNECT: Session ID={} ###", sessionId);
                                break;
                            default:
                                logger.info("### STOMP {}: Session ID={} ###", 
                                    command, sessionId);
                        }
                    }
                }
                
                return message;
            }
        });
    }
} 