package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ReservedMoneyRequest {
    @Positive
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String label;
}
