package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ReservedRequest {
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    private String label;
    @NotNull
    private String note;
}
