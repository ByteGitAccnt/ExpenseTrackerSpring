package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Dto.ReservedResponse;
import com.myApp.ExpenseTracker.Dto.TransactionResponse;
import com.myApp.ExpenseTracker.Model.ReportData;
import com.myApp.ExpenseTracker.Model.Transaction;
import com.myApp.ExpenseTracker.Utils.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {
    private final UserService userService;
    private final ReservedService reservedService;
    private final ExpenseService expenseService;
    private final TransactionService transactionService;
    private final AuditService auditService;
    private final DateTimeFormatter formatter ;
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    public ReportService(UserService userService, ReservedService reservedService, ExpenseService expenseService, TransactionService transactionService, AuditService auditService) {
        this.userService = userService;
        this.reservedService = reservedService;
        this.expenseService = expenseService;
        this.transactionService = transactionService;
        this.auditService = auditService;
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    public byte[] generateReport(Long userid , LocalDate fromDate , LocalDate toDate) {
        logger.info("Generating report for user: {}, from: {}, to: {}", userid, fromDate, toDate);
        BigDecimal totalBalance = userService.getBalance(userid);
        BigDecimal totalReserved = reservedService.getTotalReserved(userid) != null
                ? reservedService.getTotalReserved(userid) : BigDecimal.ZERO;
        BigDecimal availableBalance = totalBalance.subtract(totalReserved);
        List<ReservedResponse> reserves = reservedService.listReserved(userid);
        List<ExpenseResponse> expenses = expenseService.listExpenseDate(userid, fromDate, toDate);
        List<Transaction> incomes = transactionService.getIncomeTransactions(
                userid,
                fromDate,
                toDate);
        List<TransactionResponse> incomeResponses = incomes.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getUser().getId(),
                        transaction.getAmount().doubleValue(),
                        transaction.getType().toString(),
                        transaction.getAction().toString(),
                        transaction.getDescription(),
                        transaction.getCreatedAt().format(formatter)
                ))
                .toList();
        ReportData reportData = ReportData.builder()
                .totalBalance(totalBalance)
                .totalReserved(totalReserved)
                .availableBalance(availableBalance)
                .reserves(reserves)
                .expenses(expenses)
                .incomes(incomeResponses)
                .build();
        auditService.logSuccess(userid , EntityType.REPORT ,null ,"Report generated successfully");
        return PdfGenerator.generate(reportData);
    }
}
