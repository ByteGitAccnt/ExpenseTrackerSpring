package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Transaction;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Utils.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findByUser_IdAndTypeAndCreatedAtBetween(
            Long user,
            TransactionType type,
            LocalDateTime from,
            LocalDateTime to
    );
}
