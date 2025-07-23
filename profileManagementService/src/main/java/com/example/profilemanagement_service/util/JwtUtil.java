package com.example.profilemanagement_service.util;

import com.example.profilemanagement_service.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private String secretKey="supersecretkeythatisverylong123456";

    public String generateToken(String username, Role role, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", role.name())
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)

                .compact();
    }
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("roles", String.class);
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class).longValue(); // or Long.class
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) &&
                extractClaims(token).getExpiration().after(new Date());
    }
}
