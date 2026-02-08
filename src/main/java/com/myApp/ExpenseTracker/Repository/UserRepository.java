package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.balance = u.balance + :amount
        WHERE u.id = :userId
    """)
    int increaseBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.balance = u.balance - :amount
        WHERE u.id = :userId
          AND u.balance >= :amount
    """)
    int decreaseBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}

