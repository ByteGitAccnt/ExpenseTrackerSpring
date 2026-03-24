package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Model.CustomUserDetails;
import com.myApp.ExpenseTracker.Model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;// we injected from properties file , not yet set the env variable so plain injection
    private SecretKey key;
    //postCunstruct is used if we gave the plain private final SecretKey key =
    //            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // it the SECRET won't get injected for the key generation on time , so null point exception. Now only after spring initialized
    // and loaded the SECRET from .properties file , we start the initialization
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("userid", userDetails.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600000))// 60,000 = 1min, 86400000 = 1day 600,000 10min
                .signWith(key)
                .compact();
    }
    public String refresh(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userid", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600000))// 60,000 = 1min, 86400000 = 1day
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
    public Long extractUserid(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userid", Long.class);
    }
}