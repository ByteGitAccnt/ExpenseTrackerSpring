package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class DuplicateCategoryNameException extends BusinessException  {
    public DuplicateCategoryNameException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
