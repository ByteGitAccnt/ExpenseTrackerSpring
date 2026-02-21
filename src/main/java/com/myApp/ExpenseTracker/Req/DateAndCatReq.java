package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateAndCatReq {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @Positive
    private Long catid;
}
