package com.myApp.ExpenseTracker.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryDeletionReq {
    @NotBlank
    private String name;
}
