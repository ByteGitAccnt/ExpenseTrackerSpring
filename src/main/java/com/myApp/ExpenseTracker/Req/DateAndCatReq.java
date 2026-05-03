package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DateAndCatReq {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @Positive
    private Long catid;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}
