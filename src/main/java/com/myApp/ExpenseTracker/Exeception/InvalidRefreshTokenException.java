package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
