package com.myApp.ExpenseTracker.Dto;

import java.math.BigDecimal;

public record UserResponse(
        Long userid ,
        String username,
        String email,
        BigDecimal amount
) {
}
