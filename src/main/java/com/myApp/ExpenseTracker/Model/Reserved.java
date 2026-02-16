package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "RESERVED")
public class Reserved {

    protected Reserved(){}
    public Reserved(Long user, String label , String note , BigDecimal amount){
        this.amount = amount;
        this.label = label.toLowerCase();
        this.note = note;
        this.userId = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name ="USER_ID" , nullable = false)
    private Long userId;

    @Column(name ="LABEL" , nullable = false)
    private String label;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "AMOUNT" , nullable = false)
    private BigDecimal amount;

    public void setLabel(String label){
        this.label = label.toLowerCase();
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setAmount(BigDecimal amnt){
        this.amount = amnt;
    }
}
