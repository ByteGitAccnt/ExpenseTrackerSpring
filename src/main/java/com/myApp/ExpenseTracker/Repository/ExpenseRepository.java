package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findByUser_Id(Long userId);
    List<Expense> findByUser_IdAndExpenseDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<Expense> findByUser_IdAndCategory_IdAndExpenseDateBetween(
            Long userId,
            Long categoryId,
            LocalDate start,
            LocalDate end
    );
    Optional<Expense> findByIdAndUser_Id(Long expId, Long userId);
}
