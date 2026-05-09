package com.myApp.ExpenseTracker.Exeception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AuthException {
  public InvalidTokenException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}