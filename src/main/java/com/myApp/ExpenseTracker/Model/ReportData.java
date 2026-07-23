package com.myApp.ExpenseTracker.Model;

import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Dto.ReservedResponse;
import com.myApp.ExpenseTracker.Dto.TransactionResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {
    private BigDecimal totalBalance;

    private BigDecimal totalReserved;

    private BigDecimal availableBalance;

    private List<ReservedResponse> reserves;

    private List<ExpenseResponse> expenses;

    private List<TransactionResponse> incomes;

}
