package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "expense")
public class Expense {
    public Expense(BigDecimal amount,LocalDate date,String note, Category category,User user){
        this.amount = amount;
        this.note = note;
        this.expenseDate = date;
        this.category = category;
        this.user = user;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exp_id")
    private Long id;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at",  nullable = false,
            updatable = false,
            insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void setExpenseDate(LocalDate date){
        this.expenseDate = date;
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}