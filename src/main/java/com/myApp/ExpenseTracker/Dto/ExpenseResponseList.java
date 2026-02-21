package com.myApp.ExpenseTracker.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseList(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        String note,
        String categoryName

) {}
