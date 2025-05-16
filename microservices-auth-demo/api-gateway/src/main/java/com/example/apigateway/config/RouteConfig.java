package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))
                
                // Chat Service Routes
                .route("chat-service", r -> r
                        .path("/api/chat/**")
                        .uri("lb://chat-service"))
                
                // HRMS Service Routes
                .route("hrms-service", r -> r
                        .path("/api/hrms/**")
                        .uri("lb://hrms-service"))
                
                .build();
    }
} 