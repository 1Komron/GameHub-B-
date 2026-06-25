package com.gamehubbot.auth.infrastructure;


import com.gamehubbot.user.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
public class JwtGenerateService {
    @Value("${jwt.secret}")
    private String token;
    @Value("${jwt.ttl-seconds}")
    private long expirationDate;

    private static final String USERNAME_CLAIM = "username";


    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(
                token.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getTelegramId().toString());
        claims.put(USERNAME_CLAIM, user.getUsername());
        claims.put("firstName", user.getFirstName());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationDate))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).get(USERNAME_CLAIM).toString();
    }

    public String extractFirstName(String token) {
        return getClaims(token).get("firstName").toString();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).get("userId", String.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token).getPayload()
                .getExpiration();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
