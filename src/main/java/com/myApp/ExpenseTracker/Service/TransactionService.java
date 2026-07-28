package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Model.Transaction;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.TransactionRepository;
import com.myApp.ExpenseTracker.Utils.TransactionAction;
import com.myApp.ExpenseTracker.Utils.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepo;
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepo = transactionRepository;
    }
    public Transaction recordIncome(User user , BigDecimal amount, String description) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.INCOME)
                .action(TransactionAction.CREDIT)
                .amount(amount)
                .description(description)
                .build();
        transactionRepo.save(transaction);
        return transaction;
    }
    public List<Transaction> getIncomeTransactions(Long currentUser , LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return transactionRepo.findByUser_IdAndTypeAndCreatedAtBetween(currentUser, TransactionType.INCOME, start, end);
    }
}
