package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.RefreshToken;
import com.myApp.ExpenseTracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByUserId(Long userId);
    void deleteByToken(String token);
}