package com.myApp.ExpenseTracker.Dto;

import java.math.BigDecimal;

public record ReservedResponseList(
        Long id,
        String label,
        BigDecimal amount,
        String username,
        String note
) {
}
