package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ExpenseUpdateReq {
    @NotNull
    @Positive
    private Long exp_id;
    private LocalDate exp_date;
    private String note;
    private String category_name;

}
