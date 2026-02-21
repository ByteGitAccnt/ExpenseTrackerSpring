package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "CATEGORY")
public class Category {
    protected Category(){}
    public Category(String name , User user){
        this.user = user;
        this.name = name.toLowerCase();
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    public void setName(String name) {
        this.name = name.toLowerCase();
    }
}
