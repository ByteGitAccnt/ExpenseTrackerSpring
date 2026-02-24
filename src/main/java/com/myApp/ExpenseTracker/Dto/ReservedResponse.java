package com.myApp.ExpenseTracker.Dto;

import java.math.BigDecimal;

public record ReservedResponse(
        Long id,
        String label,
        BigDecimal amount,
        String note
) {
}
