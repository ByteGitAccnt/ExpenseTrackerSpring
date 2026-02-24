package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class RequiredException extends BusinessException {
    public RequiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
