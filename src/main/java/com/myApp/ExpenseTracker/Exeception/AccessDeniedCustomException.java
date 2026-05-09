package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class AccessDeniedCustomException  extends AuthException {
    public AccessDeniedCustomException() {
        super("You don't have permission to access this resource", HttpStatus.FORBIDDEN);
    }
}