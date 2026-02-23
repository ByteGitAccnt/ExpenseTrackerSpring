package com.myApp.ExpenseTracker.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        String note,
        String categoryName

) {}
