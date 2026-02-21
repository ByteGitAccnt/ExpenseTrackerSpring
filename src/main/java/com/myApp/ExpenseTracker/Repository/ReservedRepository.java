package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Reserved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReservedRepository extends JpaRepository<Reserved,Long> {
    Optional<Reserved> findByUser_IdAndLabel(Long userId , String label);
    List<Reserved> findByUser_Id(Long userid);

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM Reserved r
        WHERE r.user.id = :userId
    """)
    BigDecimal sumReservedByUserId(@Param("userId") Long userId);
}
