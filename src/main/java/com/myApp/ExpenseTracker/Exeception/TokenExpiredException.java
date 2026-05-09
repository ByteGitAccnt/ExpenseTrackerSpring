package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends AuthException {
    public TokenExpiredException() {
        super("Token has expired. Please refresh your token.", HttpStatus.UNAUTHORIZED);
    }
}