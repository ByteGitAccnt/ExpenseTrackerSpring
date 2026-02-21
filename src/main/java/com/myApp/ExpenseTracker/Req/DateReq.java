package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateReq {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
}
