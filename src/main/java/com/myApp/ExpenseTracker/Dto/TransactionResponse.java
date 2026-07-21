package com.myApp.ExpenseTracker.Dto;

public record TransactionResponse(
        Long transId,
        Long userid,
        Double amount,
        String entity,
        String action ,
        String desc,
        String date

) {
}
