package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
    
    @Bean
    public CorsResponseHeaderFilter corsResponseHeaderFilter() {
        return new CorsResponseHeaderFilter();
    }
    
    private static class CorsResponseHeaderFilter implements GlobalFilter, Ordered {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                
                // Remove any CORS headers that might be set by downstream services
                headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
                headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
                headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
                headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
                
                // Ensure only one Vary header
                if (headers.containsKey(HttpHeaders.VARY)) {
                    String varyValue = String.join(", ", headers.getValuesAsList(HttpHeaders.VARY));
                    headers.set(HttpHeaders.VARY, varyValue);
                }
            }));
        }
        
        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE - 10;
        }
    }
} 