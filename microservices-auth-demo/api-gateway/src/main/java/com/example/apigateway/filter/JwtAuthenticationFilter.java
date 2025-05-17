package com.example.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.header-name:Authorization}")
    private String headerName;

    @Value("${security.jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    // Define a list of paths that don't need authentication
    private final List<String> openApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/authenticate",
            "/api/auth/check-username",
            "/api/auth/check-email",
            "/h2-console",
            "/eureka"
    );

    // Define a list of paths that should allow public access
    private final List<String> publicEndpoints = List.of(
            "/api/chat/public",
            "/api/chat/public/health",
            "/api/chat/public/test",
            "/api/chat/public/status",
            "/api/chat/public/setup-test"
    );

    private Predicate<ServerHttpRequest> isOpenEndpoint = request -> 
        openApiEndpoints.stream()
            .anyMatch(uri -> request.getURI().getPath().contains(uri));

    private Predicate<ServerHttpRequest> isPublicEndpoint = request -> 
        publicEndpoints.stream()
            .anyMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Always allow OPTIONS requests for CORS preflight
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        
        // Skip filter for open endpoints
        if (isOpenEndpoint.test(request)) {
            return chain.filter(exchange);
        }

        // Allow public endpoints without token
        if (isPublicEndpoint.test(request)) {
            return chain.filter(exchange);
        }

        // Check for token presence
        if (!request.getHeaders().containsKey(headerName)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(headerName);
        String token = authHeader != null && authHeader.startsWith(tokenPrefix) 
                ? authHeader.substring(tokenPrefix.length()) 
                : null;

        if (token == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        try {
            // Validate token
            Claims claims = extractAllClaims(token);
            
            // Add user info to request headers for downstream services
            // Important: Keep the original Authorization header
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-Username", claims.getSubject())
                    .build();
            
            // Add roles if available in the token
            if (claims.get("roles") != null) {
                modifiedRequest = modifiedRequest.mutate()
                        .header("X-Auth-Roles", claims.get("roles").toString())
                        .build();
            }

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public int getOrder() {
        return 1; // Run this filter after CORS filter (CorsWebFilter has order 0)
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
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