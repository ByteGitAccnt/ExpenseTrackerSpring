package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "CATEGORY")
public class Category {
    protected Category(){}
    public Category(String name , Long user_id){
        this.userId = user_id;
        this.name = name.toLowerCase();
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "USER_ID")
    private Long userId;

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public void setUser_id(Long user_id) {
        this.userId = user_id;
    }
}
