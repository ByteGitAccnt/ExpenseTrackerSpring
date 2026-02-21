package com.myApp.ExpenseTracker.Req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryUpdateRequest {
    @NotBlank
    private String old_name;
    @NotBlank
    private String new_name;
}
