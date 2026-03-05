package com.myApp.ExpenseTracker.Dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {
}
