package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExists extends BusinessException {
    public ResourceAlreadyExists(String message) {
        super(message , HttpStatus.CONFLICT);
    }
}
