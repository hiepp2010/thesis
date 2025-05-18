package com.example.chatservice.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

@Component
public class WebSocketAuthenticationHandler implements ChannelInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthenticationHandler.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            logger.debug("WebSocket CONNECT command received with Authorization: {}", authorization);
            
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    
                    try {
                        Claims claims = extractAllClaims(token);
                        String username = claims.getSubject();
                        logger.info("WebSocket connection: JWT validated for username: {}", username);
                        
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (claims.get("roles") != null) {
                            logger.debug("Roles from token: {}", claims.get("roles"));
                            if (claims.get("roles") instanceof List) {
                                ((List<?>) claims.get("roles")).forEach(role -> {
                                    authorities.add(new SimpleGrantedAuthority(role.toString()));
                                });
                            }
                        }
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                authorities
                        );
                        
                        // Set authentication in the security context (this might not persist for future messages)
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        
                        // This is crucial - set the Authentication object in the message headers
                        // so it's available in MessageMapping methods
                        accessor.setUser(auth);
                        
                        // Store username in WebSocket session
                        accessor.getSessionAttributes().put("username", username);
                        
                        logger.info("WebSocket authenticated for user: {}", username);
                    } catch (Exception e) {
                        logger.error("WebSocket connection: Invalid JWT token: {}", e.getMessage());
                        return null; // Reject the message and close the connection
                    }
                } else {
                    logger.error("WebSocket connection: Invalid Authorization header format");
                    return null; // Reject the message
                }
            } else {
                logger.error("WebSocket connection: Missing Authorization header");
                return null; // Reject the message
            }
        } else if (accessor != null && accessor.getUser() == null) {
            // For messages after connection, ensure user is attached
            // Get user from session attributes if available
            String username = accessor.getSessionAttributes() != null ? 
                              (String) accessor.getSessionAttributes().get("username") : null;
            
            if (username != null) {
                // Create authentication token and set user
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                accessor.setUser(auth);
                logger.debug("Added user authentication to message for: {}", username);
            }
        }
        
        return message;
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 