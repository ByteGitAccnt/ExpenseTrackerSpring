package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DateReq {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}
