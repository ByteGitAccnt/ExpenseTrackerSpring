package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    Page<Expense> findByUser_Id(Long userId, Pageable pageable);

    Page<Expense> findByUser_IdAndExpenseDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<Expense> findByUser_IdAndCategory_IdAndExpenseDateBetween(
            Long userId,
            Long categoryId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );
    Optional<Expense> findByIdAndUser_Id(Long expId, Long userId);
}
