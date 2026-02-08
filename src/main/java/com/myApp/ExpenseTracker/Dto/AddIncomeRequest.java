package com.myApp.ExpenseTracker.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AddIncomeRequest {
    @NotNull
    @Positive
    private Long userid;
    @Positive
    @NotNull
    private BigDecimal amount;
}
