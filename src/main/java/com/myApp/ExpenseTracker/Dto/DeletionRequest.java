package com.myApp.ExpenseTracker.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DeletionRequest {
    @NotBlank
    private String label;
}
