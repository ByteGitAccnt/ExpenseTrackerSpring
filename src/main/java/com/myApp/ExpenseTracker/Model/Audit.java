package com.myApp.ExpenseTracker.Model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "AUDIT")
public class Audit {
    protected Audit(){}
    public Audit(
            String action,
            String entityType,
            Long entityId,
            String details,
            Long userId

    ){
        this.action = action;
        this.entityId = entityId;
        this.entityType = entityType;
        this.details = details;
        this.userId = userId;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_ID")
    private Long id;

    @Column(name = "ACTION" , nullable = false)
    private String action;

    @Column(name = "ENTITY_TYPE" , nullable = false)
    private String entityType;

    @Column(name = "ENTITY_ID")
    private Long entityId;

    @Column(name = "DETAILS")
    private String details;

    @Column(name = "USER_ID" , nullable = false)
    private Long userId;

    @Column(  name = "CREATED_AT",
            nullable = false,
            updatable = false,
            insertable = false)
    private LocalDateTime createdAt;


}
