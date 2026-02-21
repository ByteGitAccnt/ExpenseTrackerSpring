package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateReserveRequest {
    @NotBlank
    private String old_label ;
    private String new_label;
    private String note;
}
