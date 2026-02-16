package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Reserved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReservedRepository extends JpaRepository<Reserved,Long> {
    Optional<Reserved> findByUserIdAndLabel(Long userId , String label);
    List<Reserved> findByUserId(Long userid);
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Reserved r WHERE r.userId = :userid")
    BigDecimal sumReservedByUserId(Long userid);
}
