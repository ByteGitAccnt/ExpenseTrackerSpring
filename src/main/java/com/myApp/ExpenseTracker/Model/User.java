package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {
    public User(String name, String username, String password, String email) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = BigDecimal.ZERO;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;


    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(  name = "CREATED_AT",
            nullable = false,
            updatable = false,
            insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "BALANCE" , nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setBalance(BigDecimal balance){
        this.balance = balance;
    }
    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
