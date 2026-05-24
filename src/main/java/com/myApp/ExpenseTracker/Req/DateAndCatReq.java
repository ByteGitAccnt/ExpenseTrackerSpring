package com.myApp.ExpenseTracker.Req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "page must be at least 0")
    @JsonProperty(defaultValue = "1")
    @Builder.Default
    private int page = 0;
    @Min(value = 1, message = "size must be at least 1")
    @JsonProperty(defaultValue = "10")
    @Builder.Default
    private int size = 10;
}
