package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "RESERVED")
public class Reserved {

    public Reserved(User user, String label , String note , BigDecimal amount){
        this.amount = amount;
        this.label = label.toLowerCase().trim();
        this.note = note;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
