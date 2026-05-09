package com.myApp.ExpenseTracker.Exeception;

import com.myApp.ExpenseTracker.Dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;



@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex , HttpServletRequest req){
        logger.warn("Business error at {}: {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse response = ErrorResponse.of(ex.getStatus(),ex.getMessage(), req.getServletPath());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        logger.error("Unexpected error occurred", ex);
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                req.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult()// spring stores all the validation error at a Binding result, it's a container like
                .getFieldErrors()// get each field errors
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())// default message is what we set when giving @Notnull(default = "...")
                .collect(Collectors.joining(", "));
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                message,
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, HttpServletRequest req) {
        logger.warn("Auth error at {}: {}", req.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ex.getStatus(),
                ex.getMessage(),
                req.getServletPath()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
