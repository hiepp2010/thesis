package com.example.chatservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Map<String, Object> extractAllClaimsAsMap(String token) {
        Claims claims = extractAllClaims(token);
        
        Map<String, Object> result = new HashMap<>(claims);
        
        // Add formatted dates for better readability
        if (claims.getIssuedAt() != null) {
            result.put("issuedAtFormatted", claims.getIssuedAt().toString());
        }
        
        if (claims.getExpiration() != null) {
            result.put("expirationFormatted", claims.getExpiration().toString());
            result.put("isExpired", claims.getExpiration().before(new Date()));
        }
        
        return result;
    }

    public Claims extractAllClaims(String token) {
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