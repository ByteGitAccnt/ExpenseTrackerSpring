package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "my-very-strong-secret-key-that-is-at-least-32-bytes";
    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(Authentication authentication) {
        // do i handler refresh token here ?
        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))// 60,000 = 1min, 86400000 = 1day
                .signWith(key)
                .compact();
    }
    public String refresh(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))// 60,000 = 1min, 86400000 = 1day
                .signWith(key)
                .compact();
    }
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername());
    }
}