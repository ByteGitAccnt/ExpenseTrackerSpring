package com.myApp.ExpenseTracker.Dto;

import java.time.Instant;
import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        Instant expiresAt ) {
    public AuthResponse(String accessToken, String refreshToken, Long userId,   Instant expiresAt) {
        this(accessToken, refreshToken, "Bearer", userId,  expiresAt);
    }
}