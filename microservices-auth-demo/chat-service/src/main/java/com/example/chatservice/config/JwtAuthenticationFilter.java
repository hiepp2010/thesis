package com.example.chatservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        logger.info("Request path: {}", request.getServletPath());
        
        // Debug for public endpoints
        if (request.getServletPath().contains("/api/chat/public")) {
            logger.info("This is a public endpoint that should be allowed: {}", request.getServletPath());
        }

        // Skip validation for certain paths
        if (request.getServletPath().contains("/api/chat/public") ||
                request.getServletPath().equals("/api/chat/public/health") ||
                request.getServletPath().equals("/api/chat/public/test") ||
                request.getServletPath().equals("/api/chat/ping") ||
                request.getServletPath().startsWith("/h2-console")) {
            logger.info("Skipping JWT validation for public endpoint: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No Authorization header or invalid format");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        logger.info("JWT token received: {}", jwt);
        
        try {
            Claims claims = extractAllClaims(jwt);
            username = claims.getSubject();
            logger.info("JWT validated for username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extract roles from token
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (claims.get("roles") != null) {
                    logger.info("Roles from token: {}", claims.get("roles"));
                    if (claims.get("roles") instanceof List) {
                        ((List<?>) claims.get("roles")).forEach(role -> {
                            authorities.add(new SimpleGrantedAuthority(role.toString()));
                        });
                    }
                }

                // Set authentication in context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentication set in SecurityContext");
            }
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            // Invalid token, continue without setting authentication
        }

        filterChain.doFilter(request, response);
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
