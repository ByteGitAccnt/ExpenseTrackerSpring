package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {
}
