package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class AddExpenseReq {
    @Positive
    private BigDecimal amount;
    @NotNull
    private LocalDate expenseDate;
    @NotBlank
    private String category;
    @NotBlank
    private String note;
    @NotNull
    private Boolean isReserved;
    private String label;
}
